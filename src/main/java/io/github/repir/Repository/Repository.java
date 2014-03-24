package io.github.repir.Repository;

import static io.github.repir.tools.Lib.ClassTools.*;
import io.github.repir.tools.Content.Datafile;
import io.github.repir.tools.Content.Dir;
import io.github.repir.tools.Content.HDFSDir;
import io.github.repir.Extractor.EntityChannel;
import io.github.repir.tools.Lib.HDTools;
import io.github.repir.tools.Lib.Log;
import io.github.repir.Strategy.Strategy;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.hadoop.fs.FileSystem;
import io.github.repir.tools.DataTypes.Configuration;
import io.github.repir.tools.Lib.MathTools;
import io.github.repir.tools.Lib.PrintTools;
import io.github.repir.tools.Lib.StrTools;

/**
 * The Repository manages all persistent data for a collection : features that 
 * were extracted from the collection, configuration settings and additional
 * data files.
 * <p/>
 * The Repository is THE central component in RepIR. Each collection is converted
 * into its own Repository of extracted features. The extraction process and the 
 * StoredFeatures can be tailor made, programs can obtain low-level access to 
 * the stored data, new features can be added and StoredDynamicFeatures can be used
 * as small size tables to store data that can be modified.
 * <p/>
 * The configuration of a Repository, and the communication of settings for tasks
 * is done through an extension of Hadoop's Configuration class, of which a 
 * single instance resides in the Repository, that can be accessed using
 * {@link #getConfiguration()}, {@link #getConfigurationString(java.lang.String)}, etc.
 * The Configuration settings are usually seeded through configuration files, but 
 * can also be added through the command line or from code.
 * <p/>
 * For low level access to a {@link StoredFeature}, you should obtain the feature
 * through the Repository with {@link #getFeature(java.lang.Class, java.lang.String[]) }
 * using the feature's Class, and optional parameters.
 */
public class Repository {

   public static Log log = new Log(Repository.class);
   protected HDFSDir basedir;        // dir on HDFS containing the repository files
   protected String prefix;          // prefix for every file, usually configname
   protected FileSystem fs = null;   // leave null for local FS, otherwise use HDFS
   protected static final String MASTERFILE_EXTENSION = ".master";
   protected int documentcount;      // number of documents in collection
   public static final double DEFAULT_LOAD_FACTOR = 0.75;
   protected int hashtablecapacity;  // for the vocabulary hashtable
   protected int vocabularysize;     // number of words in vocabulary
   protected long cf;                // number of words in collection
   protected VocabularyToID vocabulary;
   protected int partitions = 1;     // number of partitions the repository is divided in
   protected PartitionLocation partitionlocation; // gives fast access to the man location of each patition
   public HashMap<String, StoredFeature> storedfeaturesmap = new HashMap<String, StoredFeature>();
   private DocLiteral collectionid;
   protected Configuration configuration = new Configuration();

   /**
    * Constructor for the creation of a new Repository with
    * {@link VocabularyBuilder}.
    * <p/>
    * @param basedirname directory where the repository is stored
    * @param prefix prefix for all repository filenames (usually the repository
    * name)
    */
   public Repository(HDFSDir basedirname, String prefix) {
      basedir = basedirname;
      this.prefix = prefix;
      if (!basedir.exists()) {
         log.fatal("Directory %s does not exists, please create", basedir.toString());
      }
   }

   /**
    * Constructor to open an existing Repository for use with
    * {@link Retriever}
    * <p/>
    * @param conf
    */
   public Repository(Configuration conf) {
      this(new HDFSDir(conf, conf.getSubString("repository.dir", "")), conf.getSubString("repository.prefix", ""));
      useConfiguration(conf);
      readConfiguration();
      readSettings();
   }

   public Repository(org.apache.hadoop.conf.Configuration conf) {
      this(Configuration.convert(conf));
   }

   public void changeName(String newIndex) {
      String dir = configuration.get("repository.dir").replaceAll(prefix, newIndex);
      configuration.set("repository.dir", dir);
      configuration.set("repository.prefix", newIndex);
      basedir = new HDFSDir(configuration, configuration.getSubString("repository.dir", ""));
      prefix = newIndex;
   }
   
   public boolean exists() {
      return basedir.exists();
   }

   public String getPrefix() {
      return prefix;
   }

   public PartitionLocation getPartitionLocation() {
      if (partitionlocation == null) {
         partitionlocation = new PartitionLocation(this);
      }
      return partitionlocation;
   }

   public String[] getPartitionLocation(int partition) {
      return getPartitionLocation().read(partition);
   }

   public Repository(String conffile) {
      this(HDTools.readConfig(conffile));
   }

   protected void useConfiguration(Configuration conf) {
      this.configuration = conf;
      conf.setBoolean("fs.hdfs.impl.disable.cache", false);
      setFileSystem(HDFSDir.getFS(conf));
   }

   protected void readSettings() {
      partitions = getConfigurationInt("repository.partitions", 1);
      setVocabularySize(getConfigurationInt("repository.vocabularysize", 0));
      setCF(getConfigurationLong("repository.corpustf", 0));
      documentcount = getConfigurationInt("repository.documentcount", 0);
      hashtablecapacity = getConfigurationInt("repository.hashtablecapacity", 0);
      collectionid = (DocLiteral) getFeature(getConfigurationString("repository.collectionid"));
   }

   protected void getStoredFeatures(String features[]) {
      for (String s : features) {
         StoredFeature f = (StoredFeature) getFeature(s);
      }
   }

   public void setFileSystem(FileSystem fs) {
      this.fs = fs;
   }

   public FileSystem getFS() {
      return fs;
   }

   public HDFSDir getBaseDir() {
      return basedir;
   }

   public Dir getIndexDir() {
      return (HDFSDir) getBaseDir().getSubdir("repository");
   }
   static Pattern pattern = Pattern.compile("(\\d{4})"); // segments limited to 10000

   public static int getSegmentFromFilename(String filename) {
      filename = filename.substring(filename.lastIndexOf('/') + 1);
      Matcher matcher = pattern.matcher(filename);
      if (matcher.find()) {
         return Integer.parseInt(matcher.group());
      }
      return -1;
   }

   public String getFilename(String extension) {
      return basedir.getFilename(prefix + extension);
   }

   protected Datafile getMasterFile() {
      return new Datafile(getFS(), basedir.getFilename(prefix + MASTERFILE_EXTENSION));
   }

   public String getTestsetName() {
      return this.getConfigurationString("testset.name");
   }

   protected Datafile getStoredValuesFile() {
      return new Datafile(getFS(), basedir.getFilename(prefix + ".storedvalues"));
   }

   public long getCF() {
      return cf;
   }

   public int getDocumentCount() {
      return documentcount;
   }

   public void setDocumentCount(int documentcount) {
      this.documentcount = documentcount;
   }

   public void setPartitions(int nodes) {
      this.partitions = nodes;
   }

   public int getPartitions() {
      return partitions;
   }

   /**
    * stores the Repository data in a masterfile, so that it can be reopened
    * with Repository.get()
    */
   public void readConfiguration() {
      Datafile df = getMasterFile();
      Datafile storedvalues = this.getStoredValuesFile();
      if (basedir.exists() && df.exists()) {
         configuration.read(df);
         if (storedvalues.exists()) {
            configuration.read(storedvalues);
         }
      }
   }

   /**
    * The configurationstring can contain settings 
    * @param configurationstring
    */
   public void addConfiguration(String configurationstring) {
      if (configurationstring != null) {
         if (configurationstring.contains(","))
            for (String s : configurationstring.split(","))
               configuration.read(s);
         else
            configuration.read(configurationstring);
      }
   }

   public void deleteMasterFile() {
      Datafile df = getMasterFile();
      if (df.exists()) {
         df.delete();
      }
   }

   private void initConfiguration() {
      configuration.set("repository.dir", basedir.getCanonicalPath());
      configuration.set("repository.prefix", prefix);
      configuration.setInt("repository.partitions", partitions);
      for (Map.Entry<String, StoredFeature> entry : storedfeaturesmap.entrySet()) {
         StoredFeature f = entry.getValue();
         if (entry.getValue() == getCollectionIDFeature()) {
            configuration.set("repository.collectionid", f.getCanonicalName());
         } else {
            configuration.addArray("repository.feature", f.getCanonicalName());
         }
      }
      configuration.setLong("repository.vocabularysize", this.getVocabularySize());
      configuration.setLong("repository.corpustf", this.getCF());
      configuration.setLong("repository.documentcount", this.getDocumentCount());
   }
   
   public void writeConfiguration() {
      initConfiguration();
      Datafile masterfile = getMasterFile();
      masterfile.openWrite();
      configuration.writeString(masterfile, "repository.dir");
      configuration.writeString(masterfile, "repository.prefix");
      configuration.writeInt(masterfile, "repository.partitions");
      configuration.writeString(masterfile, "repository.collectionid");
      for (Map.Entry<String, StoredFeature> entry : storedfeaturesmap.entrySet()) {
         StoredFeature f = entry.getValue();
         if (entry.getValue() != getCollectionIDFeature()) {
            configuration.addArray("repository.feature", f.getCanonicalName());
         }
      }
      configuration.writeStrings(masterfile, "repository.feature");
      configuration.writeLong(masterfile, "repository.vocabularysize");
      configuration.writeLong(masterfile, "repository.corpustf");
      configuration.writeLong(masterfile, "repository.documentcount");
      masterfile.closeWrite();
   }

   public Collection<StoredFeature> getConfiguredFeatures() {
      getStoredFeatures(getConfigurationSubStrings("repository.feature"));
      return storedfeaturesmap.values();
   }

   public void featuresWriteCache() {
      for (StoredFeature f : getConfiguredFeatures()) {
         f.writeCache();
      }
   }

   public DocLiteral getCollectionIDFeature() {
      return collectionid;
   }

   /**
    * Use this method to obtain access to StoredFeatures, which allows
    * the system to reuse single instances of the exact same feature.
    * @param canonicalname
    * @return a Feature instance identified by the canonicalname 
    */
   public Feature getFeature(String canonicalname) {
      return getFeature(canonicalname, canonicalname);
   }
   
   public Feature getFeature(Class featureclass, String ... parameter) {
      String feature = Feature.canonicalName(featureclass, parameter);
      return getFeature(feature, feature);
   }
   
   /**
    * @param featureclass must be a class with a unique simplename!
    * @return a Feature instance of the featureclass 
    */
   public Feature getFeature(Class featureclass) {
      String feature = Feature.canonicalName(featureclass);
      return getFeature(feature, feature);
   }

   private Feature getFeature(String label, String canonicalname) {
      Feature f = storedfeaturesmap.get(label);
      if (f != null) {
         return f;
      }
      String classname, field = null;
      int pos = canonicalname.indexOf(':');
      if (pos > 0) {
         classname = canonicalname.substring(0, pos).trim();
         field = canonicalname.substring(pos + 1).trim();
      } else {
         classname = canonicalname.trim();
      }
      Class clazz = tryToClass(classname, getClass().getPackage().getName(), Strategy.class.getPackage().getName());
      if (clazz != null) {
         f = storedfeaturesmap.get(clazz.getCanonicalName());
         if (f != null) {
            return f;
         }
         if (field != null) {
            Constructor cons = getAssignableConstructor(clazz, Feature.class, Repository.class, String.class);
            f = (Feature) io.github.repir.tools.Lib.ClassTools.construct(cons, this, field);
            if (f instanceof StoredFeature) {
               storedfeaturesmap.put(f.getLabel(), (StoredFeature) f);
            }
         } else {
            Constructor cons = getAssignableConstructor(clazz, Feature.class, Repository.class);
            f = (Feature) io.github.repir.tools.Lib.ClassTools.construct(cons, this);
            if (f instanceof StoredFeature) {
               storedfeaturesmap.put(f.getLabel(), (StoredFeature) f);
            }
         }
         return f;
      }
      return null;
   }

   public void unloadStoredDynamicFeatures() {
      Iterator<Entry<String, StoredFeature>> iter = storedfeaturesmap.entrySet().iterator();
      while (iter.hasNext()) {
         Entry<String, StoredFeature> entry = iter.next();
         if (entry.getValue() instanceof StoredDynamicFeature) {
            iter.remove();
         }
      }
   }

   public void unloadTermDocumentFeatures() {
      Iterator<Entry<String, StoredFeature>> iter = storedfeaturesmap.entrySet().iterator();
      while (iter.hasNext()) {
         Entry<String, StoredFeature> entry = iter.next();
         if (entry.getValue() instanceof TermDocumentFeature) {
            iter.remove();
         }
      }
   }

   public void unloadStoredDynamicFeature(Set<StoredDynamicFeature> sdf) {
      for (StoredDynamicFeature f : sdf) {
         unloadStoredDynamicFeature(f);
      }
   }

   public void unloadStoredDynamicFeature(StoredDynamicFeature sdf) {
      Iterator<Entry<String, StoredFeature>> iter = storedfeaturesmap.entrySet().iterator();
      while (iter.hasNext()) {
         Entry<String, StoredFeature> entry = iter.next();
         if (entry.getValue() instanceof StoredDynamicFeature) {
            if (entry.getValue() == sdf) {
               iter.remove();
               break;
            }
         }
      }
   }

   public Integer termToID(String term) {
      return ((TermID) getFeature(TermID.class)).get(term);
   }

   public void setVocabularySize(int size) {
      vocabularysize = size;
      //hashtablecapacity = calculateCapacity();
   }

   public int getVocabularySize() {
      return vocabularysize;
   }

   public void setCF(long cf) {
      this.cf = cf;
   }

   /**
    * @return the Hadoop Configuration container that is used to maintain and
    * communicate all settings for the repository
    */
   public Configuration getConfiguration() {
      return this.configuration;
   }

   /**
    * @return the Hadoop Configuration container that is used to maintain and
    * communicate all settings for the repository
    */
   public String[] getConfigurationSubStrings(String key) {
      return configuration.getSubStrings(key);
   }

   public ArrayList<String> getConfigurationList(String key) {
      return configuration.getStringList(key);
   }

   public ArrayList<Integer> getConfigurationIntList(String key) {
      return configuration.getIntList(key);
   }

   public ArrayList<Long> getConfigurationLongList(String key) {
      return configuration.getLongList(key);
   }

   public String getConfigurationString(String key) {
      return configuration.getSubString(key);
   }

   public String getConfigurationName() {
      return configuration.get("rr.conf");
   }

   public String getConfigurationString(String key, String defaultvalue) {
      return configuration.getSubString(key, defaultvalue);
   }

   public int getConfigurationInt(String key, int defaultvalue) {
      return configuration.getInt(key, defaultvalue);
   }

   public long getConfigurationLong(String key, long defaultvalue) {
      return configuration.getLong(key, defaultvalue);
   }

   /**
    * @return the Hadoop Configuration container that is used to maintain and
    * communicate all settings for the repository
    */
   public boolean getConfigurationBoolean(String key, boolean defaultvalue) {
      return this.configuration.getBoolean(key, defaultvalue);
   }

   /**
    * Note: Hadoop 0.20 does not support double, so these are stored as strings,
    * if the value is not empty or a valid double a fatal exception is the
    * result
    * <p/>
    * @return the double value of the key.
    */
   public double getConfigurationDouble(String key, double defaultvalue) {
      double d = defaultvalue;
      String value = this.configuration.get(key);
      try {
         if (value != null && value.length() > 0) {
            d = Double.parseDouble(value);
         }
      } catch (NumberFormatException ex) {
         log.fatalexception(ex, "Configuration setting '%s' does not contain a valid double '%s'", key, value);
      }
      return d;
   }

   public String getParameterFile() {
      return getConfigurationString("testset.queryparameters");
   }
   
   public int[] tokenize(EntityChannel attr) {
      if (vocabulary == null || !(vocabulary instanceof VocabularyToIDRAM)) {
         for (Feature f : this.getConfiguredFeatures()) {
            if (f instanceof VocabularyToIDRAM) {
               vocabulary = (VocabularyToIDRAM) f;
               vocabulary.openRead();
               //log.info("VocMem opened %s", vocabulary.getCanonicalName());
            }
         }
      }
      if (vocabulary == null) {
         for (Feature f : this.getConfiguredFeatures()) {
            if (f instanceof VocabularyToID) {
               vocabulary = (VocabularyToID) f;
               vocabulary.openRead();
               //log.info("Voc opened %s", vocabulary.getCanonicalName());
            }
         }
      }
      if (vocabulary == null) {
         log.fatal("you cannot tokenize if there is no Vocabulary in the repository");
      }
      return vocabulary.getContent(attr);
   }

   public static int partition(String docid, int partitions) {
      return MathTools.Mod(docid.hashCode(), partitions);
   }

   public Repository[] getTuneRepositories() {
      //log.info("crossevaluate %s", getConfigurationString("testset.crossevaluate"));
      String cross[] = StrTools.split(getConfigurationString("testset.crossevaluate"), ",");
      if (cross.length == 1 && cross[0].equalsIgnoreCase("fold")) {
         return new Repository[]{this};
      }
      Repository r[] = new Repository[cross.length + 1];
      for (int i = 0; i < cross.length; i++) {
         r[i + 1] = new Repository(cross[i]);
      }
      r[0] = this;
      return r;
   }

   public String[] getStoredFreeParameters() {
      String freeparameters[] = getConfigurationSubStrings("strategy.freeparameter");
      HashSet<String> list = new HashSet<String>();
      for (int i = 0; i < freeparameters.length; i++) {
         if (freeparameters[i].indexOf('=') > 0) {
            list.add(freeparameters[i].substring(0, freeparameters[i].indexOf('=')).trim());
         } else {
            list.add(freeparameters[i].trim());
         }
      }
      list.add("rr.conf");
      if (getConfigurationString("testset.crossevaluate").equalsIgnoreCase("fold")) {
         list.add("fold");
      }
      return list.toArray(new String[list.size()]);
   }

   public Map<String,String> getFreeParameters() {
      String freeparameters[] = getConfigurationSubStrings("strategy.freeparameter");
      HashMap<String, String> tuneparameters = new HashMap<String, String>();
      for (String s : freeparameters) {
         if (s.indexOf('=') > 0) {
            String parameter = s.substring(0, s.indexOf('=')).trim();
            String value = s.substring(s.indexOf('=')+1).trim();
            tuneparameters.put(parameter, value);
         } else {
            String value = getConfigurationString(s);
            tuneparameters.put(s, PrintTools.sprintf("%s..%s..%d", value, value, 1));
         }
      }
      return tuneparameters;
   }
}
