package io.github.repir.Strategy;

import io.github.repir.Repository.Feature;
import io.github.repir.Repository.ReportableFeature;
import io.github.repir.Repository.ReportedUnstoredFeature;
import java.lang.reflect.Constructor;
import io.github.repir.Retriever.Document;
import io.github.repir.Retriever.Retriever;
import io.github.repir.Retriever.PostingIterator;
import io.github.repir.Retriever.Query;
import io.github.repir.tools.Lib.Log;
import io.github.repir.Repository.StoredFeature;
import io.github.repir.Repository.StoredReportableFeature;
import io.github.repir.Retriever.ReportedFeature;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import io.github.repir.Strategy.Collector.Collector;
import io.github.repir.Strategy.Collector.CollectorDocument;
import io.github.repir.tools.Lib.ClassTools;

/**
 * A RetrievalModel is a Strategy that eventually returns the query with a list
 * of ranked {@link Document}s.
 * <p/>
 * Similar to a {@link Strategy}, the {@link RetrievalModel} contains the Query
 * independent logic to control retrieval, e.g. the basic operation for pseudo
 * relevance feedback or Sequential Dependence Model. During retrieval, the
 * retrieval model instance controls the retrieval operation for a single pass
 * on a partition for a single query. All used {@link Feature}s are managed by
 * the {@link RetrievalModel} for a pass; if {@link Operator}s need a
 * {@link StoredFeature} they should request this with
 * {@link RetrievalModel#requestFeature(java.lang.Class, java.lang.String[])}
 * and the reported features that are specified in the {@link Query} are
 * instantiated by the RetrievalModel and can be obtained with f.i.
 * {@link #getReportableFeatures()}.
 * <p/>
 * To allow for multi-pass retrieval, a {@link Retriever} will receive the
 * resulting query after each pass and retrieve again until the Query contains
 * results. A RetrievalModel distinguished itself from a {@link Strategy} by
 * constructing a graph of {@link Operator} nodes, that are used to process each
 * {@link Document}. The nodes should request the features they need using {@link #requestFeature(java.lang.Class, java.lang.String[])
 * }
 * and by default the RetrievalModel will internally use a
 * {@link PostingIterator} to efficiently retrieve the feature values for each
 * {@link Document}. On the final pass, a RetrievaModel will use a
 * {@link DocumentCollector} to collect a ranked list of
 * "retriever.documentlimit" {@link Document}s. Retrieval for a partition is
 * executed by running {@link #doMapTask()}, in which the RetrievalModel
 * iterates over the retrieved {@link Document}s, calls the highest operator
 * nodes in the graph to have all operators set their values corresponding the
 * current document, and call for the {@link Collector} to {@link Collector#collectDocument(io.github.repir.Retriever.Document)
 * }
 * the document. The default {@link DocumentCollector} will use the configured
 * "retriever.scorefunction" to assign a score to the document, and maintain a
 * list of the top ranked documents.
 * <p/>
 * used {@link Feature}s.
 *
 * @author jeroen
 */
public class RetrievalModel extends Strategy {

   public static Log log = new Log(RetrievalModel.class);
   public HashMap<String, StoredFeature> requestedfeatures = new HashMap<String, StoredFeature>();
   public GraphRoot root;
   private Class documentcollectorclass = CollectorDocument.class;
   // requested features
   public HashMap<String, ReportedFeature> featuresmap;
   private ArrayList<ReportedFeature<ReportedUnstoredFeature>> reportableunstoredfeatures;
   private ArrayList<ReportedFeature<StoredReportableFeature>> storedreportablefeatures;
   private ArrayList<ReportedFeature<ReportableFeature>> reportablefeatures;

   /**
    * Use {@link #create(Retriever.Retriever, Retriever.Query)} instead.
    * <p/>
    * @param retriever
    */
   public RetrievalModel(Retriever retriever) {
      super(retriever);
   }

   public static RetrievalModel create(Retriever retriever, Query queryrequest) {
      return (RetrievalModel) create(retriever, queryrequest, RetrievalModel.class);
   }

   public final void buildGraph() {
      root = new GraphRoot(this);
      root.buildGraph();
   }

   /**
    * If an {@link Operator} needs a {@link StoredFeature} for operation, to retrieve
    * data for the inspected {@link Document}s, they should obtain this using this
    * method, which is then automatically added to the {@link PostingIterator}.
    * @param clazz class that extends {@link StoredFeature}
    * @param parameter optional parameter list, e.g. for a {@link Term} that requires
    * its postings list stored in {@link TermInverted}, the parameters are term and
    * channel.
    * @return The {@link StoredFeature} that was requested.
    */
   public StoredFeature requestFeature(Class clazz, String... parameter) {
      StoredFeature sf = (StoredFeature) repository.getFeature(clazz, parameter);
      String name = sf.getCanonicalName();
      StoredFeature exists = requestedfeatures.get(name);
      if (exists != null) {
         return exists;
      }
      requestedfeatures.put(name, sf);
      return sf;
   }

   /**
    * @return list of all {@link StoredFeature}s requested using {@link #requestFeature(java.lang.Class, java.lang.String[]) } 
    */
   public Collection<StoredFeature> getUsedFeatures() {
      return requestedfeatures.values();
   }

   /**
    * By default, a retrieval model uses a {@link CollectorDocument} to retrieve
    * a ranked list of {@link Document}s. RetrievalModels that require a pre-pass
    * should override this to use different collector for the retrieval model.
    * Note: this method is not called when an {@link Operator} flagged that a 
    * pre-pass is needed, in which case the {@link Operator} constructs its own
    * custom {@link Collector} to obtain the data, and no end result is obtained during 
    * that pass.
    */
   @Override
   public void setCollector() {
      if (!root.needsPrePass()) {
         Constructor constructor = ClassTools.getConstructor(documentcollectorclass, RetrievalModel.class);
         ClassTools.construct(constructor, this);
      }
   }

   /**
    * Used to setup the Strategy so that results can be collected and aggregated
    * but are not retrieved yet. This is typically used in the Reducer to create
    * a Strategy for the aggregation of results collected per segments.
    */
   @Override
   public void prepareAggregationDetail() {
      buildGraph();
      root.prepareRetrieval();
   }

   /**
    * Retrieves and processes the results for a single partition. This is
    * typically used in Mappers that only process the results for a single
    * partition and send the results to the reducer.
    * <p/>
    * @param partition id of the partition to retrieveQueries
    * @return the MasterCollector containing the aggregated results of all
    * segments.=
    */
   @Override
   public void doMapTask() {
      if (root.containednodes.size() > 0) {
         PostingIterator pi = retriever.getPostingIterator(this, partition);
         for (Document d = pi.next(); d != null; d = pi.next()) {
            collectors.collect(d);
         }
      }
   }

   /**
    * After a retrieval-pass, the {@link Retriever} calls the results() function
    * in which the Strategy decides whether this was the final pass and results
    * are returned, or if a consecutive retrieval pass is required.
    * <p/>
    * @return {@link Query} object, which contains strategyclass=null and
    * queryresults if the final pass was processed, or a strategyclass with a
    * reformulated query if a consecutive retrieval pass is required.
    */
   public Query finishReduceTask() {
      if (root.needsPrePass()) {
         //log.info("cascade needed prepass");
         query.query = root.postReform();
         query.setStrategyClassname(RetrievalModel.class.getCanonicalName());
      } else {
         Collector c = collectors.getCollector(documentcollectorclass.getSimpleName());
         if (c != null) {
            query.setStrategyClassname(null);
            if (collectors.size() > 0) {
               query.queryresults = ((CollectorDocument) c).getRetrievedDocs();
            }
         }
      }
      return query;
   }

   /**
    * @return reformulated {@link Query} after a retrieval pass. This can be used
    * by multi-pass retrieval models such as Pseudo Relevance Feedback to reformulate 
    * a Query based on retrieved results.
    */
   public Query postReform() {
      query.query = root.postReform();
      return query;
   }

   /**
    * low level constructor to create a new document object
    * <p/>
    * @param terms the number of terms in the query, to initialize the arrays
    * hat contain the statistics per term.
    * @return a Document object
    */
   public Document createDocument(int id, int partition) {
      try {
         return query.createDocument(this, id, partition);
      } catch (Exception ex) {
         log.fatalexception(ex, "createDocument( %d, %d )", id, partition);
         return null;
      }
   }

   /**
    * @return a Map containing all {@link ReportedFeature}s that are specified in
    * the {@link Query}.
    */
   public HashMap<String, ReportedFeature> getReportedFeaturesMap() {
      if (featuresmap == null) {
         featuresmap = new HashMap<String, ReportedFeature>();
         for (String featurename : getReportedFeatures()) {
            ReportedFeature f = new ReportedFeature(featurename, (ReportableFeature) repository.getFeature(featurename));
            f.reportID = featuresmap.size();
            featuresmap.put(featurename, f);
         }
      }
      return featuresmap;
   }
   
   /**
    * @return The {@link Feature}s that were specified in the {@link Query}, to report
    * back for each {@link Document}. This method is overridden by retrieval models
    * that implement multi-pass retrieval, to prevent fetching features that are only
    * needed in the final pass and alternatively to fetch features needed after the
    * pre-pass, e.g. Pseduo Relevance Feedback models, which sometimes need the 
    * size of the document's. 
    */
   public ArrayList<String> getReportedFeatures() {
      return query.reportedFeatures;
   }

   

   /**
    * @return a Collection of {@link ReportedFeature}s.
    */
   public Collection<ReportedFeature<ReportableFeature>> getReportableFeatures() {
      if (reportablefeatures == null) {
         reportablefeatures = new ArrayList<ReportedFeature<ReportableFeature>>();
         for (ReportedFeature f : getReportedFeaturesMap().values()) {
            reportablefeatures.add(new ReportedFeature<ReportableFeature>(f));
         }
      }
      return reportablefeatures;
   }

   /**
    * @return The (int) position of the specified {@link ReportableFeature}, which is 
    * used internally to assign the features data to a slot in each {@link Document}s
    * reporteddata array.
    */
   public int getReportID(ReportableFeature f) {
      ReportedFeature rf = getReportedFeaturesMap().get(f.getCanonicalName());
      return (rf == null) ? -1 : rf.reportID;
   }

   /**
    * @return Collection of {@link ReportedFeature}s that are stored, i.e. these
    * are not needed to score {@link Document}s, but only have to be retrieved
    * for the documents in the final ranked list.
    */
   public Collection<ReportedFeature<StoredReportableFeature>> getReportedStoredFeatures() {
      if (storedreportablefeatures == null) {
         storedreportablefeatures = new ArrayList<ReportedFeature<StoredReportableFeature>>();
         for (ReportedFeature f : getReportedFeaturesMap().values()) {
            if (f.feature instanceof StoredReportableFeature) {
               storedreportablefeatures.add(new ReportedFeature<StoredReportableFeature>(f));
            }
         }
      }
      return storedreportablefeatures;
   }

   /**
    * @return Collection of {@link ReportedFeature}s that are not stored, but 
    * created during processing of the graph for each document, and therefore have
    * to be stored during graph processing. A typical example is a secondary
    * {@link ScoreFunction}, which is calculated but not stored as the primary score
    * used to rank the documents.
    */
   public Collection<ReportedFeature<ReportedUnstoredFeature>> getReportedUnstoredFeatures() {
      if (reportableunstoredfeatures == null) {
         reportableunstoredfeatures = new ArrayList<ReportedFeature<ReportedUnstoredFeature>>();
         for (ReportedFeature f : getReportedFeaturesMap().values()) {
            if (f.feature instanceof ReportedUnstoredFeature) {
               reportableunstoredfeatures.add(new ReportedFeature<ReportedUnstoredFeature>(f));
            }
         }
      }
      return reportableunstoredfeatures;
   }

   public ReportedFeature getReportedFeature(Class c, String... parameters) {
      ReportedFeature rf = getReportedFeaturesMap().get(Feature.canonicalName(c, parameters));
      return (rf != null) ? rf : null;
   }

   public ReportedFeature getReportedFeature(ReportableFeature f) {
      ReportedFeature rf = getReportedFeaturesMap().get(f.getCanonicalName());
      return (rf != null) ? rf : null;
   }
}
