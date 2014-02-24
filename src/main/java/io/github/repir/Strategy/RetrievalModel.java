package io.github.repir.Strategy;

import java.lang.reflect.Constructor;
import io.github.repir.Retriever.Document;
import io.github.repir.Retriever.Retriever;
import io.github.repir.Retriever.PostingIterator;
import io.github.repir.Retriever.Query;
import io.github.repir.tools.Lib.Log;
import io.github.repir.Repository.StoredFeature;
import io.github.repir.Repository.StoredReportableFeature;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import io.github.repir.Strategy.Collector.Collector;
import io.github.repir.Strategy.Collector.CollectorDocument;
import io.github.repir.tools.Lib.ClassTools;

/**
 * The Strategy controls the retrieval process. A Strategy is created by {@link #buildGraph(Retriever.Query)
 * }
 * which uses a {@link Query} request that is parsed into an {@link GraphRoot}. For standard
 * retrieval, the default Strategy should work fine, but alternatively, a Strategy can
 * alter the GraphRoot (e.g. {@link RetrievalModelRM3}).
 * <p/>
 * @author jeroen
 */
public class RetrievalModel extends Strategy {
   
   public static Log log = new Log(RetrievalModel.class);
   public HashMap<String, StoredFeature> usedfeatures = new HashMap<String, StoredFeature>();
   public GraphRoot root;
   public RetrievalModelReportedFeatures reportedfeatures;
   private Class documentcollectorclass = CollectorDocument.class;

   /**
    * Use {@link #create(Retriever.Retriever, Retriever.Query)} instead.
    * <p/>
    * @param retriever
    */
   public RetrievalModel(Retriever retriever) {
      super( retriever );
   }
   
   public static RetrievalModel create( Retriever retriever, Query queryrequest) {
      return (RetrievalModel)create( retriever, queryrequest, RetrievalModel.class);
   }
   
   public final void buildGraph() {
      query.query = getQueryToRetrieve();
      root = new GraphRoot(this);
      root.buildGraph();
   }
   
   /**
    * 
    * @return querystring that is used to construct a processing Graph. This is a
    * one time conversion, which should be based on Query.stemmedquery and is set 
    * as Query.query which is used operational.
    */
   public String getQueryToRetrieve() {
      return query.stemmedquery;
   }

   public RetrievalModelReportedFeatures getFeatures() {
      if (reportedfeatures == null) {
         reportedfeatures = new RetrievalModelReportedFeatures(repository);
         for (String f : getReportedFeatures())
            reportedfeatures.add(f);
      }
      return reportedfeatures;
   }
   
   public ArrayList<String> getReportedFeatures() {
      return query.reportedFeatures;
   }
   
   public StoredFeature requestFeature(String f) {
      //log.info("requestFeature( %s )", f);
      StoredFeature sf = (StoredFeature) repository.getFeature(f);
      String name = sf.getCanonicalName();
      StoredFeature exists = usedfeatures.get(name);
      //log.info("got %s", name);
      if (exists != null) {
         return exists;
      }
      usedfeatures.put(name, sf);
      //log.info("usedfeatures.size %s %d", this, usedfeatures.size());
      return sf;
   }
   
   public Collection<StoredFeature> getUsedFeatures() {
      return usedfeatures.values();
   }

   /**
    * Override to set a different collector for the retrieval model
    */
   public void setCollector() {
      if (!root.needsPrePass()) {
         Constructor constructor = ClassTools.getConstructor(documentcollectorclass, RetrievalModel.class);
         ClassTools.construct(constructor, this);
      }
   }

   /**
    * Used to setup the Strategy so that results can be collected and aggregated but are not
    * retrieved yet. This is typically used in the Reducer to create a Strategy for the
    * aggregation of results collected per segments.
    */
   @Override
   public void prepareAggregationDetail() {
      buildGraph();
      root.prepareRetrieval();
   }
   
   /**
    * Retrieves and processes the results for a single partition. This is typically used in Mappers
    * that only process the results for a single partition and send the results to the reducer.
    * <p/>
    * @param partition id of the partition to retrieveQueries
    * @return the MasterCollector containing the aggregated results of all segments.=
    */
   @Override
   public void doMapTask() {
      if (root.containedfeatures.size() > 0) {
         PostingIterator pi = retriever.getPostingIterator(this, partition);
         for (Document d = pi.next(); d != null; d = pi.next()) {
            collectors.collect(d);
         }
      }
   }
   
   /**
    * After a retrieval-pass, the {@link Retriever} calls the results() function in which the
    * Strategy decides whether this was the final pass and results are returned, or if a
    * consecutive retrieval pass is required.
    * <p/>
    * @return {@link Query} object, which contains strategyclass=null and queryresults if the
    * final pass was processed, or a strategyclass with a reformulated query if a consecutive
    * retrieval pass is required.
    */
   public Query finishReduceTask() {
      if (root.needsPrePass()) {
         //log.info("cascade needed prepass");
         query.stemmedquery = root.postReform();
         query.setStrategyClass(RetrievalModel.class.getCanonicalName());
      } else {
         Collector c = collectors.getCollector(documentcollectorclass.getSimpleName());
         if (c != null ) {
            query.setStrategyClass(null);
            if (collectors.size() > 0) {
               query.queryresults = ((CollectorDocument)c).getRetrievedDocs();
            }
         }
      }
      return query;
   }
   
   public Query postReform() {
      query.stemmedquery = root.postReform();
      return query;
   }
   
   /**
    * low level constructor to create a new document object
    * <p/>
    * @param terms the number of terms in the query, to initialize the arrays hat contain the
    * statistics per term.
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
}
