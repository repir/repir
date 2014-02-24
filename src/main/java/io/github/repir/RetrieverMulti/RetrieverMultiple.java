package io.github.repir.RetrieverMulti;

import io.github.repir.Retriever.PostingIteratorReusable;
import io.github.repir.Retriever.PostingIterator;
import io.github.repir.Retriever.Query;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import io.github.repir.Repository.Repository;
import io.github.repir.Repository.StoredFeature;
import io.github.repir.Repository.StoredReportableFeature;
import io.github.repir.Retriever.Query.Variant;
import io.github.repir.RetrieverMR.QueryIterator;
import io.github.repir.RetrieverMR.QueueIterator;
import io.github.repir.Strategy.RetrievalModel;
import io.github.repir.Strategy.Strategy;
import io.github.repir.tools.Lib.Log;

/**
 * Gives access to an existing {@link Repository.Repository}. The most common
 * way to setup an Retriever is to readValue the repository Configuration from
 * file, create an Repository object using the Repository configuration, and
 * create an Retriever using the Repository.
 * <p/>
 * The most common way to retrieveQueries a single Query, is to construct a
 * Query object using {@link #constructDefaultQuery(java.lang.String)} and call
 * {@link #retrieveQuery(Retriever.Query)}. The query object that is returned
 * contains the results.
 * <p/>
 * Under the hood, the retrieval process is separated in different objects
 * working together. The Query contains the name of the
 * {@link Strategy.Strategy} class, which is used to construct a Retrieval
 * Model. The Retrieval Model will use the query to build an inference model,
 * i.e. the query is parsed into a network of nodes, that first extract all
 * necessary usedfeatures from a document, then process those usedfeatures,
 * resulting in collected results.
 * <p/>
 */
public class RetrieverMultiple extends io.github.repir.RetrieverMR.RetrieverMR {

   public static Log log = new Log(RetrieverMultiple.class);
   protected PostingIteratorReusable postingiterator;
   

   public RetrieverMultiple(Repository repository) {
      super(repository);
   }

    public RetrieverMultiple(Repository repository, org.apache.hadoop.mapreduce.Mapper.Context mappercontext) {
      super(repository, mappercontext);
   }

   /**
    * The Reducer context is used to report progress, to prevent processes form
    * being killed while still aggregating.
    * <p/>
    * @param repository
    * @param reducercontext
    */
   public RetrieverMultiple(Repository repository, org.apache.hadoop.mapreduce.Reducer.Context reducercontext) {
      super(repository, reducercontext);
   }
   
  @Override
   public PostingIterator getPostingIterator(RetrievalModel strategy, int partition) {
     log.s("getPostingIterator");
      if (postingiterator == null) {
         log.reportTime("getPostingIterator");
         strategy.root.setTDFDependencies();
         postingiterator = new PostingIteratorReusable(partition);
         for (StoredReportableFeature sf : strategy.getFeatures().getReportedStoredFeatures()) {
            sf.setPartition(partition);
            sf.readResident();
         }
         log.reportTime("getPostingIterator end");
      }
      else if (postingiterator.partition != partition)
         log.fatal("cannot use RetrieverMultiple to retrieve different partitions");
      postingiterator.reuse(strategy);
      log.e("getPostingIterator");
      return postingiterator;
   }
  
   public Strategy prepareStrategy(Query query, int partition) {
      Strategy strategy = constructStrategy(query);
      strategy.partition = partition;
      strategy.prepareAggregation();
      strategy.prepareRetrieval();
      return strategy;
   }
  
   @Override
   public void retrieveSegment(Strategy strategy) {
      log.info("retrieveSegment %s doclimit %d", strategy.query.query, strategy.query.documentlimit);
      strategy.collectors.reuse();
      log.s("retrieveSegment");
      strategy.doMapTask();
      log.e("retrieveSegment");
      strategy.collectors.postLoadFeatures(strategy.partition);
      strategy.collectors.finishSegmentRetrieval();
   }

   public void addVariant( String retrievalmodelclass, String scorefunctionclass, String settings ) {
      Variant v = new Variant( retrievalmodelclass, scorefunctionclass, settings );
      for (Query q : queue) {
         q.addVariant(v);
      }
   }
      
   @Override
   public IRHDJobMulti createIRHDJob(String path) throws IOException {
      return new IRHDJobMulti( this, path );  
   }
}
