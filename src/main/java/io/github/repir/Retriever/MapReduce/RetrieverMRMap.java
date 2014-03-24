package io.github.repir.Retriever.MapReduce;

import java.io.IOException;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.mapreduce.Mapper;
import io.github.repir.Strategy.Collector.Collector;
import io.github.repir.Repository.Repository;
import io.github.repir.Strategy.Strategy;
import io.github.repir.Retriever.Query;
import io.github.repir.tools.Lib.Log;

/**
 * The mapper is generic, and collects data for a query request, using the
 * passed retrieval model, scoring function and query string. The common
 * approach is that each node processes all queries for one index partition. The
 * collected results are reshuffled to one reducer per query where all results
 * for a single query are aggregated.
 * <p/>
 * @author jeroen
 */
public class RetrieverMRMap extends Mapper<NullWritable, QueryWritable, CollectorKey, CollectorValue> {

   public static Log log = new Log(RetrieverMRMap.class);
   Context context;
   private Repository repository;
   private Retriever retriever;
   RetrieverMRInputSplit split;
   int partition;
   String retrievalmodel;

   @Override
   protected void setup(Context context) throws IOException, InterruptedException {
      this.context = context;
      repository = new Repository(context.getConfiguration());
      retriever = new Retriever(repository, context);
      split = (RetrieverMRInputSplit) context.getInputSplit();
      partition = split.partition;
   }

   @Override
   public void map(NullWritable inkey, QueryWritable invalue, Context context) throws IOException, InterruptedException {
      repository.unloadTermDocumentFeatures();
      Query q = invalue.getQuery(repository);
      log.info("%d %s", q.id, q.originalquery);
      Strategy strategy = retriever.retrieveSegment(q, partition); // collect results for query from one index partition
      for (Collector c : strategy.collectors) {
         context.write(c.getCollectorKey(), c.getCollectorValue());
      }
   }

   @Override
   protected void cleanup(Context context) throws IOException, InterruptedException {
      Log.reportProfile();
   }
}
