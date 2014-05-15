package io.github.repir.Retriever.Reusable;

import java.io.IOException;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.mapreduce.Mapper;
import io.github.repir.Strategy.Collector.Collector;
import io.github.repir.Repository.Repository;
import io.github.repir.Strategy.Strategy;
import io.github.repir.Retriever.Query;
import io.github.repir.Retriever.Query.Variant;
import io.github.repir.Retriever.MapReduce.CollectorKey;
import io.github.repir.Retriever.MapReduce.CollectorValue;
import io.github.repir.Retriever.MapReduce.QueryWritable;
import io.github.repir.Retriever.MapReduce.QueryInputSplit;
import io.github.repir.Strategy.Collector.CollectorDocument;
import io.github.repir.Strategy.RetrievalModel;
import io.github.repir.tools.Lib.Log;
import org.apache.hadoop.io.IntWritable;

/**
 * The mapper is generic, and collects data for a query request, using the
 * passed retrieval model, scoring function and query string. The common
 * approach is that each node processes all queries for one index partition. The
 * collected results are reshuffled to one reducer per query where all results
 * for a single query are aggregated.
 * <p/>
 * @author jeroen
 */
public class RetrieverMap extends Mapper<IntWritable, QueryWritable, CollectorKey, CollectorValue> {

   public static Log log = new Log(RetrieverMap.class);
   Context context;
   protected Repository repository;
   protected Retriever retriever;
   QueryInputSplit split;
   protected int partition;

   @Override
   protected void setup(Context context) throws IOException, InterruptedException {
      this.context = context;
      repository = new Repository(context.getConfiguration());
      retriever = new Retriever(repository, context);
      split = (QueryInputSplit) context.getInputSplit();
      partition = split.partition;
      log.info("partition %d", partition);
   }

   @Override
   public void map(IntWritable inkey, QueryWritable invalue, Context context) throws IOException, InterruptedException {
      Query q = invalue.getQuery(repository);
      log.info("query %d %s %s doclimit %d variantscount %d", q.id, q.query, q.getScorefunctionClass(), q.documentlimit, q.variantCount());
      for (Query v : q.variantIterator()) {
         Strategy strategy = retriever.prepareStrategy(v, partition);
         log.info("query id %d qid %d variant %d conf %s", q.id, q.getID(), q.getVariantID(), q.getConfiguration());
         retriever.retrieveSegment(strategy); // collect results for query from one index partition
         for (Collector c : strategy.collectors) {
            CollectorKey collectorKey = c.getCollectorKey();
            CollectorValue collectorValue = c.getCollectorValue();
            changeCollectorKey( collectorKey );
            context.write(collectorKey, c.getCollectorValue());
         }
      }
   }
   
   public void changeCollectorKey( CollectorKey key ) {}

   @Override
   protected void cleanup(Context context) throws IOException, InterruptedException {
      Log.reportProfile();
   }
}
