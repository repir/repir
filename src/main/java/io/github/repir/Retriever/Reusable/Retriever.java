package io.github.repir.Retriever.Reusable;

import io.github.repir.Retriever.PostingIteratorReusable;
import io.github.repir.Retriever.PostingIterator;
import io.github.repir.Retriever.Query;
import java.io.IOException;
import io.github.repir.Repository.Repository;
import io.github.repir.Repository.StoredReportableFeature;
import io.github.repir.Retriever.Query.Variant;
import io.github.repir.Retriever.ReportedFeature;
import io.github.repir.Strategy.RetrievalModel;
import io.github.repir.Strategy.Strategy;
import io.github.repir.tools.Lib.Log;

/**
 * This variant of {@link io.github.repir.Retriever.MapReduce.} is more efficient in repeating the same retrieval model
 * multiple times. Under the hood, a {@link PostingIteratorReusable} is used to
 * load data for the retrieval process into memory, which is kept for following
 * passes of the same retrieval model.
 * 
 */
public class Retriever extends io.github.repir.Retriever.MapReduce.Retriever {

   public static Log log = new Log(Retriever.class);
   protected PostingIteratorReusable postingiterator;
   

   public Retriever(Repository repository) {
      super(repository);
   }

    public Retriever(Repository repository, org.apache.hadoop.mapreduce.Mapper.Context mappercontext) {
      super(repository, mappercontext);
   }

   /**
    * The Reducer context is used to report progress, to prevent processes form
    * being killed while still aggregating.
    * <p/>
    * @param repository
    * @param reducercontext
    */
   public Retriever(Repository repository, org.apache.hadoop.mapreduce.Reducer.Context reducercontext) {
      super(repository, reducercontext);
   }
   
  @Override
   public PostingIterator getPostingIterator(RetrievalModel retrievalmodel, int partition) {
      if (postingiterator == null) {
         retrievalmodel.root.setTDFDependencies();
         postingiterator = new PostingIteratorReusable(partition);
         for (ReportedFeature<StoredReportableFeature> sf : retrievalmodel.getReportedStoredFeatures()) {
            sf.feature.setPartition(partition);
            sf.feature.readResident();
         }
      }
      else if (postingiterator.partition != partition)
         log.fatal("cannot use RetrieverMultiple to retrieve different partitions");
      postingiterator.reuse(retrievalmodel);
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
      strategy.collectors.reuse();
      strategy.doMapTask();
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
   public Job createJob(String path) throws IOException {
      return new Job( this, path );  
   }
}
