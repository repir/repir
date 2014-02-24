package io.github.repir.Repository;

import static io.github.repir.tools.Lib.ClassTools.*;
import io.github.repir.tools.Lib.ConfTool;
import io.github.repir.tools.Content.Datafile;
import io.github.repir.tools.Content.Dir;
import io.github.repir.tools.Content.HDFSDir;
import io.github.repir.Extractor.EntityAttribute;
import io.github.repir.tools.Lib.HDTools;
import io.github.repir.tools.Lib.Log;
import static io.github.repir.tools.Lib.PrintTools.*;
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
import io.github.repir.tools.Lib.StrTools;

/**
 * The Repository is a container class for all components and information of an
 * indexed collection. The repository is commonly generated with
 *  and accessed through {@link IndexReader.IndexReader}.
 * These classes use the repository to obtain low level access to the repository
 * files. Information Retrieval applications typically start by opening the
 * Repository, which is needed to open the {@link IndexReader.IndexReader} and
 * gives general corpus information such as the number of documents or words in
 * the repository.
 * <p/>
 * The base structure for the Repository is fixed. A masterfile contains the
 * repository location, configuration and statistics for the repository (e.g.
 * Vocabulary size, number of documents in collection). Although the masterfile
 * is in plain text and editable, this is strongly discouraged (unless perhaps
 * for carefully moving the repository).
 * <p/>
 * The main configuration of the repository includes which storedfeatures are
 * extracted from the corpus.
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
   protected long tf;                // number of words in collection
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
      if (!basedir.exists()) {
         log.fatal("Directory %s does not exists, please create", basedir.toString());
      }
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
      setTF(getConfigurationLong("repository.corpustf", 0));
      documentcount = getConfigurationInt("repository.documentcount", 0);
      hashtablecapacity = getConfigurationInt("repository.hashtablecapacity", 0);
      collectionid = (DocLiteral) getFeature(getConfigurationString("repository.collectionid"));
      getStoredFeatures(getConfigurationSubStrings("repository.feature"));;
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

   public Datafile getStoredFeatureFile(int segment, StoredFeature sf) {
      return new Datafile(getFS(), basedir.getFilename(sprintf("repository/%s.%s.%04d", prefix, sf.getFileNameSuffix(), segment)));
   }

   public Datafile getStoredFeatureFile(StoredFeature sf) {
      return new Datafile(getFS(), basedir.getFilename(sprintf("repository/%s.%s", prefix, sf.getFileNameSuffix())));
   }

   public Datafile getTempFeatureFile(StoredFeature sf) {
      String filename = basedir.getFilename(sprintf("repository/temp/%s.%s", prefix, sf.getFileNameSuffix()));
      log.info("filename %s", filename);
      return new Datafile(getFS(), filename);
   }

   public Datafile getTempFeatureFile(StoredFeature sf, String suffix) {
      return new Datafile(getFS(), basedir.getFilename(sprintf("repository/temp/%s.%s.%s", prefix, sf.getFileNameSuffix(), suffix)));
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

   public long getCorpusTF() {
      return tf;
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
         ConfTool conf = new ConfTool(df);
         if (storedvalues.exists()) {
            conf.read(storedvalues);
         }
         conf.toConf(configuration);
      }
   }

   public void addConfiguration(String list) {
      HDTools.addToConfiguration(configuration, list);
   }

   public void deleteMasterFile() {
      Datafile df = getMasterFile();
      if (df.exists()) {
         df.delete();
      }
   }

   public void writeConfiguration() {
      ConfTool conf = new ConfTool();
      conf.set("repository.dir", basedir.getCanonicalPath());
      conf.set("repository.prefix", prefix);
      conf.set("repository.partitions", partitions);
      for (Map.Entry<String, StoredFeature> entry : storedfeaturesmap.entrySet()) {
         StoredFeature f = entry.getValue();
         if (entry.getValue() == getCollectionIDFeature()) {
            conf.set("repository.collectionid", f.getCanonicalName());
         } else {
            conf.setArray("repository.feature", f.getCanonicalName());
         }
      }
      conf.set("repository.vocabularysize", this.getVocabularySize());
      conf.set("repository.corpustf", this.getCorpusTF());
      conf.set("repository.documentcount", this.getDocumentCount());
      //conf.set("repository.hashtablecapacity", this.getHashTableCapacity());
      conf.write(getMasterFile());
   }

   public void writeStoredValues() {
      ConfTool conf = new ConfTool();
      Iterator<Entry<String, String>> iter = configuration.iterator();
      while (iter.hasNext()) {
         Entry<String, String> e = iter.next();
         if (e.getKey().startsWith("storedvalue.")) {
            conf.set(e.getKey(), e.getValue());
         }
      }
      conf.write(this.getStoredValuesFile());
   }

   public Collection<StoredFeature> getFeatures() {
      return storedfeaturesmap.values();
   }

   public void featuresWriteCache() {
      for (StoredFeature f : getFeatures()) {
         f.writeCache();
      }
   }

   public DocLiteral getCollectionIDFeature() {
      return collectionid;
   }

   public Feature getFeature(String canonicalname) {
      return getFeature(canonicalname, canonicalname);
   }

   public Feature getFeature(String label, String canonicalname) {
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
      return ((TermID) getFeature("TermID")).get(term);
   }

   public void setVocabularySize(int size) {
      vocabularysize = size;
      //hashtablecapacity = calculateCapacity();
   }

   public int getVocabularySize() {
      return vocabularysize;
   }

   public void setTF(long corpustf) {
      tf = corpustf;
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
      return configuration.get("repir.conf");
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
   
   public int[] tokenize(EntityAttribute attr) {
      if (vocabulary == null || !(vocabulary instanceof VocabularyToIDRAM)) {
         for (Feature f : this.getFeatures()) {
            if (f instanceof VocabularyToIDRAM) {
               vocabulary = (VocabularyToIDRAM) f;
               vocabulary.openRead();
               //log.info("VocMem opened %s", vocabulary.getCanonicalName());
            }
         }
      }
      if (vocabulary == null) {
         for (Feature f : this.getFeatures()) {
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
      return io.github.repir.tools.Lib.MathTools.Mod(docid.hashCode(), partitions);
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
      String freeparameters[] = getConfigurationSubStrings("retriever.freeparameter");
      HashSet<String> list = new HashSet<String>();
      for (int i = 0; i < freeparameters.length; i++) {
         if (freeparameters[i].indexOf('=') > 0) {
            list.add(freeparameters[i].substring(0, freeparameters[i].indexOf('=')));
         } else {
            list.add(freeparameters[i]);
         }
      }
      list.add("repir.conf");
      if (getConfigurationString("testset.crossevaluate").equalsIgnoreCase("fold")) {
         list.add("fold");
      }
      return list.toArray(new String[list.size()]);
   }

   public ArrayList<String> getFreeParameters() {
      String freeparameters[] = getConfigurationSubStrings("retriever.freeparameter");
      ArrayList<String> tuneparameters = new ArrayList<String>();
      for (String s : freeparameters) {
         if (s.indexOf('=') > 0) {
            tuneparameters.add(s);
         }
      }
      return tuneparameters;
   }
}
