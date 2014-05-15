package io.github.repir.Repository;

import io.github.repir.Extractor.EntityChannel;
import io.github.repir.Repository.Stopwords.StopWords;
import io.github.repir.Repository.Stopwords.StopwordsCache;
import io.github.repir.Strategy.Strategy;
import io.github.repir.tools.Content.Datafile;
import io.github.repir.tools.Content.Dir;
import io.github.repir.tools.Content.HDFSDir;
import io.github.repir.tools.Lib.ArrayTools;
import static io.github.repir.tools.Lib.ClassTools.*;
import io.github.repir.tools.Lib.Log;
import io.github.repir.tools.Lib.MathTools;
import io.github.repir.tools.Lib.PrintTools;
import io.github.repir.tools.Lib.StrTools;
import io.github.repir.tools.Stemmer.englishStemmer;
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
 * {@link #getConfiguration()}, {@link #configuredString(java.lang.String)}, etc.
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
   private CollectionID collectionid;
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
      this(new HDFSDir(conf, conf.get("repository.dir", "")), conf.get("repository.prefix", ""));
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
      basedir = new HDFSDir(configuration, configuration.get("repository.dir", ""));
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
      this(new Configuration(conffile));
   }

   public Repository(String args[], String template) {
      this(new Configuration(args, template));
   }

   public Repository(String args[]) {
      this(new Configuration(args, ""));
   }

   protected void useConfiguration(Configuration conf) {
      this.configuration = conf;
      conf.setBoolean("fs.hdfs.impl.disable.cache", false);
      setFileSystem(conf.FS());
   }

   protected void readSettings() {
      partitions = configuredInt("repository.partitions", 1);
      setVocabularySize(configuredInt("repository.vocabularysize", 0));
      setCF(configuredLong("repository.corpustf", 0));
      documentcount = configuredInt("repository.documentcount", 0);
      hashtablecapacity = configuredInt("repository.hashtablecapacity", 0);
      collectionid = (CollectionID) getFeature(CollectionID.class.getSimpleName());
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

   public HDFSDir getIndexDir() {
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

   public Datafile getMasterFile() {
      return new Datafile(getFS(), basedir.getFilename(prefix + MASTERFILE_EXTENSION));
   }

   public String getTestsetName() {
      return this.configuredString("testset.name");
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
            for (String s : configurationstring.split(",")) {
               configuration.read(s);
            }
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
         if (entry.getValue() != getCollectionIDFeature()) {
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
      getStoredFeatures(configuredStrings("repository.feature"));
      return storedfeaturesmap.values();
   }

   public void featuresWriteCache() {
      for (StoredFeature f : getConfiguredFeatures()) {
         f.writeCache();
      }
   }

   public CollectionID getCollectionIDFeature() {
      return collectionid;
   }

   /**
    * Use this method to obtain access to StoredFeatures, which allows
    * the system to reuse single instances of the exact same feature.
    * @param canonicalname
    * @return a Feature instance identified by the canonicalname 
    */
   public Feature getFeature(String canonicalname) {
      Feature f = storedfeaturesmap.get(canonicalname);
      return (f != null)?f:getFeature(canonicalname, canonicalname);
   }
   
   public Feature getFeature(Class featureclass, String ... parameter) {
      String feature = Feature.canonicalName(featureclass, parameter);      
      Feature f = storedfeaturesmap.get(feature);
      return (f != null)?f:getFeature(feature, featureclass, parameter);
   }
   
   /**
    * @param featureclass must be a class with a unique simplename!
    * @return a Feature instance of the featureclass 
    */
   public Feature getFeature(Class featureclass) {
      String feature = Feature.canonicalName(featureclass);
      Feature f = storedfeaturesmap.get(feature);
      return (f != null)?f:getFeature(feature, featureclass);
   }

   private Feature getFeature(String label, String canonicalname) {
      String classname, field = null;
      int pos = canonicalname.indexOf(':');
      if (pos > 0) {
         classname = canonicalname.substring(0, pos).trim();
         field = canonicalname.substring(pos + 1).trim();
      } else {
         classname = canonicalname.trim();
      }
      Class clazz = tryToClass(classname
              , getClass().getPackage().getName()
              , Strategy.class.getPackage().getName());
      if (clazz != null) {
         return (field!=null)?getFeature(label, clazz, field):getFeature(label, clazz);
      }
      return null;
   }

   private Feature getFeature(String label, Class clazz, String ... field ) {
      Feature f = null;
      if (clazz != null) {
         f = storedfeaturesmap.get(clazz.getCanonicalName());
         if (f != null) {
            return f;
         }
         if (field != null && field.length > 0) {
            String fieldstring = ArrayTools.concat(field, 0, field.length, ":");
            Constructor cons = getAssignableConstructor(clazz, Feature.class, Repository.class, String.class);
            f = (Feature) io.github.repir.tools.Lib.ClassTools.construct(cons, this, fieldstring);
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
      }
      return f;
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

   public void unloadStoredDynamicFeature(Set<StoredFeature> sdf) {
      for (StoredFeature f : sdf) {
         unloadStoredDynamicFeature(f);
      }
   }

   public void unloadStoredDynamicFeature(StoredFeature sdf) {
      Iterator<Entry<String, StoredFeature>> iter = storedfeaturesmap.entrySet().iterator();
      while (iter.hasNext()) {
         Entry<String, StoredFeature> entry = iter.next();
         if (entry.getValue() instanceof StoredFeature) {
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
   public String[] configuredStrings(String key) {
      return configuration.getStrings(key);
   }

   public ArrayList<String> configuredStringList(String key) {
      return configuration.getStringList(key);
   }

   public ArrayList<Integer> configuredIntList(String key) {
      return configuration.getIntList(key);
   }

   public ArrayList<Long> configuredLongList(String key) {
      return configuration.getLongList(key);
   }

   public String configuredString(String key) {
      return configuration.getSubString(key);
   }

   public String configurationName() {
      return configuration.get("rr.conf");
   }

   public String configuredString(String key, String defaultvalue) {
      return configuration.get(key, defaultvalue);
   }

   public int configuredInt(String key, int defaultvalue) {
      return configuration.getInt(key, defaultvalue);
   }

   public int configuredInt(String key) {
      return configuredInt(key, Integer.MIN_VALUE);
   }

   public long configuredLong(String key, long defaultvalue) {
      return configuration.getLong(key, defaultvalue);
   }

   public long configuredLong(String key) {
      return configuredLong(key, Long.MIN_VALUE);
   }

   /**
    * @return the Hadoop Configuration container that is used to maintain and
    * communicate all settings for the repository
    */
   public boolean configuredBoolean(String key, boolean defaultvalue) {
      return this.configuration.getBoolean(key, defaultvalue);
   }

   /**
    * Note: Hadoop 0.20 does not support double, so these are stored as strings,
    * if the value is not empty or a valid double a fatal exception is the
    * result
    * <p/>
    * @return the double value of the key.
    */
   public double configuredDouble(String key, double defaultvalue) {
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
      return configuredString("testset.queryparameters");
   }
   
   /**
    * @return A {@link Datafile} that is configured in "testset.topics" as the file containing the
    * topics for evaluation.
    */
   public Datafile getTopicsFile() {
      Datafile df = new Datafile(configuredString("testset.topics"));
      if (!df.exists()) {
         df = new Datafile(getFS(), Repository.this.configuredString("testset.topics"));
      }
      if (!df.exists()) {
         log.fatal("topicfile %s does not exists", df.getFullPath());
      }
      return df;
   }
   
   /**
    * @return A list of {@link Datafile}s that is configured in "testset.qrels" 
    * as the files containing the query relevance labels for evaluation.
    */
   public ArrayList<Datafile> getQrelFiles() {
      String qr = configuredString("testset.qrels");
      if (qr == null) {
         log.fatal("testset.qrels not set");
      }
      String qrs[] = qr.split(",");
      ArrayList<Datafile> list = new ArrayList<Datafile>();
      for (String p : qrs) {
         Datafile f = new Datafile(p);
         Dir d = f.getDir();
         if (!d.exists()) {
            f = new Datafile(getFS(), p);
            d = f.getDir();
         }
         list.addAll(d.matchDatafiles(f.getFilename()));
      }
      return list;
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

   public int getPartition(String docid) {
      return getPartition(docid, getPartitions());
   }
   
   public static int getPartition(String docid, int partitions) {
      return MathTools.Mod(docid.hashCode(), partitions);
   }

   public Repository[] getTuneRepositories() {
      //log.info("crossevaluate %s", getConfigurationString("testset.crossevaluate"));
      String cross[] = StrTools.split(configuredString("testset.crossevaluate"), ",");
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
      String freeparameters[] = configuredStrings("strategy.freeparameter");
      HashSet<String> list = new HashSet<String>();
      for (int i = 0; i < freeparameters.length; i++) {
         if (freeparameters[i].indexOf('=') > 0) {
            list.add(freeparameters[i].substring(0, freeparameters[i].indexOf('=')).trim());
         } else {
            list.add(freeparameters[i].trim());
         }
      }
      if (configuredString("testset.crossevaluate").equalsIgnoreCase("fold")) {
         list.add("fold");
      }
      return list.toArray(new String[list.size()]);
   }

   public Map<String,String> getFreeParameters() {
      String freeparameters[] = configuredStrings("strategy.freeparameter");
      HashMap<String, String> tuneparameters = new HashMap<String, String>();
      for (String s : freeparameters) {
         if (s.indexOf('=') > 0) {
            String parameter = s.substring(0, s.indexOf('=')).trim();
            String value = s.substring(s.indexOf('=')+1).trim();
            tuneparameters.put(parameter, value);
         } else {
            String value = configuredString(s);
            tuneparameters.put(s, PrintTools.sprintf("%s..%s..%d", value, value, 1));
         }
      }
      return tuneparameters;
   }
   
   HashSet<Integer> stopwords;
   public HashSet<Integer> getStopwords() {
      if (stopwords == null) { 
         StopwordsCache sw = (StopwordsCache)getFeature(StopwordsCache.class);
         stopwords = sw.getStopwords();
         if (stopwords.size() == 0) {
            stopwords = StopWords.get(this).getIntSet();
         }
      }
      return stopwords;
   }
   
   public Term getTerm(String term) {
      if (term == null)
         return null;
      if (term.startsWith("@#")) {
         int termid = Integer.parseInt(term);
         if (termid < 0)
            return null;
         TermString termstring = (TermString)getFeature(TermString.class);
         String stemmed = termstring.readValue(termid);
         return new Term(termid, null, stemmed, getStopwords().contains(termid));
      } else {
         if (term == null)
            return null;
         String processedterm;
         if (term.startsWith("@")) {
            processedterm = term.substring(1);
            term = null;
         } else {
            processedterm = englishStemmer.get().stem(term.toLowerCase());
         }
         int termid = termToID(processedterm);
         return new Term(termid, term, processedterm, getStopwords().contains(termid));
      }
   }
   
   public Term getProcessedTerm(String term) {
      if (term == null)
         return null;
      int termid = termToID(term);
      return new Term(termid, null, term, getStopwords().contains(termid));
   }
   
   public Term getTerm(int termid) {
      if (termid < 0)
         return null;
      TermString termstring = (TermString)getFeature(TermString.class);
      String stemmed = termstring.readValue(termid);
      return new Term(termid, null, stemmed, getStopwords().contains(termid));
   }
}