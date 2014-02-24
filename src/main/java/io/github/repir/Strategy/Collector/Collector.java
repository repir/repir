package io.github.repir.Strategy.Collector;

import java.io.EOFException;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collection;
import io.github.repir.Retriever.Document;
import io.github.repir.Retriever.Retriever;
import io.github.repir.Repository.Repository;
import io.github.repir.Repository.StoredFeature;
import io.github.repir.Strategy.GraphNode;
import io.github.repir.Strategy.GraphRoot;
import io.github.repir.Strategy.Strategy;
import io.github.repir.Retriever.Query;
import io.github.repir.RetrieverMR.CollectorKey;
import io.github.repir.RetrieverMR.CollectorValue;
import io.github.repir.tools.Content.StructureReader;
import io.github.repir.tools.Content.StructureWriter;
import io.github.repir.tools.Lib.ClassTools;
import io.github.repir.tools.Lib.Log;

/**
 * A Collector collects a single piece of data from an GraphRoot. Typical uses are a Collector to
 * score and collect documents in order to construct a ranked list of documents retrieved. Other
 * uses are the collection of corpus wide statistics. SubCollectors are called after the root
 * Features have been processed for the document. Typically, a Collector will call one or more
 * Features {@link Strategy.Feature#process(Retriever.Document) }
 * to obtain the values that need to be collected. It cannot be assumed that the processing Features
 * or other SubCollectors have been executed before this one.
 * <p/>
 * Each Collector must implement the {@link BufferDelayedWriter.Serialize} interface to allow
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
   public ArrayList<GraphNode> containedfeatures = new ArrayList<GraphNode>();
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
      clazz = io.github.repir.tools.Lib.StrTools.removeOptionalStart(clazz, Collector.class.getPackage().getName() + ".");
      return clazz;
   }

   public static Collector create(String canonicalname) {
      Class clazz = ClassTools.toClass(canonicalname, Collector.class.getPackage().getName());
      Constructor c = ClassTools.getAssignableConstructor(clazz, Collector.class);
      return (Collector) ClassTools.construct(c);
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
      //log.info("doc %d", doc.docid);
      for (GraphNode f : containedfeatures) {
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
    * @throws EOFException
    */
   public abstract void readID(StructureReader reader) throws EOFException;

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

   public abstract void readKey(StructureReader reader) throws EOFException;

   /**
    * The value contains the collected data that is to be aggregated in the reducer
    * <p/>
    * @param writer
    */
   public abstract void writeValue(StructureWriter writer);

   public abstract void readValue(StructureReader reader) throws EOFException;
}
