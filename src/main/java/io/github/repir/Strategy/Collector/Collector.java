package io.github.repir.Strategy.Collector;

import io.github.repir.Repository.Repository;
import io.github.repir.Retriever.Document;
import io.github.repir.Retriever.MapReduce.CollectorKey;
import io.github.repir.Retriever.MapReduce.CollectorValue;
import io.github.repir.Retriever.Retriever;
import io.github.repir.Strategy.Operator.Operator;
import io.github.repir.Strategy.Strategy;
import io.github.htools.io.EOCException;
import io.github.htools.lib.ClassTools;
import io.github.htools.lib.Log;
import io.github.htools.io.struct.StructureReader;
import io.github.htools.io.struct.StructureWriter;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A Collector collects data during a retrieval pass. The {@link Collector} is
 * bound to a {@link Strategy} instance, which controls when to collect, and possibly
 * to {@link Operator}s to use. If the {@link Strategy} is a {@link RetrievalModel}, 
 * the collector is called using {@link #processRetrievedDocument(io.github.repir.Retriever.Document)}
 * to trigger processing of their {@link Operator}s. After processing of the {@link Operator}s
 * the collectors are requested to collect their results in {@link #collectDocument(io.github.repir.Retriever.Document) }.
 * Other {@link Strategy}s than {@link RetrievalModel} do not retrieve {@link Document}s
 * therefore collection will be custom defined.
 * <p/>
 * Note that {@link Operator}s that
 * are not bound (recursively) to a collector are not processed in such case, therefore
 * during  pre-pass to collect proximity statistics all Operators that are not needed 
 * are not processed.
 * <p/>
 * Each Collector must implement the {@link BufferSerializable} interface to allow
 * communication over the MapReduce framework. For every document to be processed {@link #processRetrievedDocument(Retriever.Document)
 * } is called once. {@link #postLoadFeatures() } is called when the mapper is finished processing all
 * input so the Collector can do a final processing step before being send to the reducer. In the
 * reducer, the data from all SubCollectors are combined using {@link #aggregate(Retriever.Collector)
 * }.
 * <p/>
 * @author jeroen
 */
public abstract class Collector {

   public static Log log = new Log( Collector.class );
   public Retriever retriever;
   public Strategy strategy;
   public ArrayList<Operator> containedfeatures = new ArrayList<Operator>();
   protected int collectorid = -1;
   //protected String key;
//   public Query query;

   public Collector() {
   }

   public Collector(Strategy rm) {
      setStrategy(rm);
   }

   public void setStrategy(Strategy strategy) {
      setRetriever(strategy.retriever);
      this.strategy = strategy;
      strategy.collectors.add(this);
   }

   @Override
   public abstract boolean equals( Object o );
   
   @Override
   public abstract int hashCode( );
   
   public abstract Collection<String> getReducerIDs();

   public abstract String getReducerName();

   public int getReducerID() {
      return strategy.collectors.getReducerID(this);
   }
   
   public abstract boolean reduceInQuery();
   
   public abstract void reuse();
   
   /**
    * This is called after a retrieval pass is finished, so that features that
    * needed a collector to obtain values needed for a consecutive run are instructed
    * to fetch their collected values.
    */
   public abstract void setCollectedResults();
   
   /**
    * After the prepareAggregation phase, the feature that will be processed are
    * instructed to request the features they need.
    */
   public abstract void prepareRetrieval();

//   public abstract String getKey();

//   public String key() {
//      if (key == null) {
//         key = getKey();
//      }
//      return key;
//   }

   public String getCanonicalName() {
      String clazz = getClass().getCanonicalName();
      clazz = io.github.htools.lib.StrTools.removeOptionalStart(clazz, Collector.class.getPackage().getName() + ".");
      return clazz;
   }

   public static Collector create(String canonicalname) {
        try {
            Class clazz = ClassTools.toClass(canonicalname, Collector.class.getPackage().getName());
            Constructor c = ClassTools.getAssignableConstructor(clazz, Collector.class);
            return (Collector) ClassTools.construct(c);
        } catch (ClassNotFoundException ex) {
            log.fatalexception(ex, "create() could not construct %s", canonicalname);
        }
        return null;
   }

   @Override
   public String toString(){
      return super.toString();
   }
   
   public void setRetriever(Retriever retriever) {
      this.retriever = retriever;
   }

   public Repository getRepository() {
      return retriever.repository;
   }

   public Strategy getRetrievalModel() {
      return strategy;
   }

   public Retriever getIndexReader() {
      return retriever;
   }


   /**
    * All collectors of the same type (e.g. for FeaturePhrase, FeatureSynonym) are 
    * grouped to enable them to store the obtained values for all their features
    * in one write cycle.
    * @param collectors 
    */
   public void finishReduce() {
      setCollectedResults();
   }

   /**
    * Is called after the GraphRoot was initialized and prepared for retrieval to allow the
    * collectors to prepare themselves for retrieval.
    */
   public void prepareAggregation() {
   }

   public void doPrepareRetrieval() {
       prepareRetrieval();
   }

   public void finishSegmentRetrieval() {
   }

   /**
    * Is called once for every retrieved document. All containedfeatures, are 
    * instructed to process their values for the correct document. Afterwards
    * the postProcessRetrievedDocument method is called in which the actual
    * collecting takes place.
    * <p/>
    * @param doc document that is currently processed.
    */
   public final void processRetrievedDocument(Document doc) {
      doc.score = 0;
      for (Operator f : containedfeatures) {
         f.process(doc);
      }
      collectDocument( doc );
   }

   /**
    * This is the actual collect part, that is called after the features for a 
    * document are processed.
    * @param doc 
    */
   protected abstract void collectDocument(Document doc);
   /**
    * Aggregates the collected results of the passed collector into its own.
    * <p/>
    * @param collector
    */
   public abstract void aggregate(Collector collector);

   public void aggregateDuplicatePartition(Collector collector) {
     log.info("aggregateDuplicatePatition %s", collector.getCanonicalName());
   }

   /**
    * Is called once after retrieval finishes to allow the collector to post process the collected
    * data, before the results are pulled.
    */
   public void postLoadFeatures(int partition) {
   }

   /**
    * When sent over the MapReduce framework, dynamic features pose the problem of not being able to
    * be read back without knowing the repository and feature, which is not known as the actual
    * reading back is controlled by Hadoop. To bypass, dynamic features are written as encoded byte
    * arrays, and have to be decoded after being read back. This abstract method should be
    * implemented if the collector contains feature data (for instance retrieved documents), which
    * have to linked to the correct feature and repository and decoded before use.
    */
   public abstract void decode();

   public CollectorKey getCollectorKey() {
      CollectorKey key = new CollectorKey();
      key.set(this, strategy.query);
      return key;
   }

   public CollectorValue getCollectorValue() {
      CollectorValue value = new CollectorValue();
      value.collector = this;
      value.partition = strategy.partition;
      return value;
   }

   /**
    * The header of a Collector's Key always starts with the CanonicalName of the class and can
    * optionally be followed by a unique identifier which forces the different values to be mapped
    * to different reducers. This only needs to be used if the class name of the collector is not
    * enough to separate output to different reducers, e.g. CollectorDocument will use this to send
    * the retrieved documents to different reducers for each query.
    * <p/>
    * @param reader
    * @throws EOCException
    */
   public abstract void readID(StructureReader reader) throws EOCException;

   public abstract void writeID(StructureWriter writer);

   /**
    * If two CollectorKeys have different values written in WriteKey, this does not affect the
    * reducer used, but splits the data into different key-value pairs. e.g. CollectorPhrase uses
    * this to send all collected phrase statistics to a single reducer, distinguishing between
    * different phrases to aggregate the statistics per phrase. This ensures all modifications to
    * the StoredDynamicFeature to made from a single reducer, avoiding problems with concurrent file
    * writes.
    * <p/>
    * @param writer
    */
   public abstract void writeKey(StructureWriter writer);

   public abstract void readKey(StructureReader reader) throws EOCException;

   /**
    * The value contains the collected data that is to be aggregated in the reducer
    * <p/>
    * @param writer
    */
   public abstract void writeValue(StructureWriter writer);

   public abstract void readValue(StructureReader reader) throws EOCException;
}
