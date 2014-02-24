   package io.github.repir.RetrieverMR;

import java.io.IOException;
import java.util.HashSet;
import io.github.repir.tools.DataTypes.Configuration;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.mapreduce.Reducer;
import io.github.repir.Strategy.Collector.Collector;
import io.github.repir.Strategy.Collector.CollectorCachable;
import io.github.repir.Repository.Repository;
import io.github.repir.Strategy.Strategy;
import io.github.repir.Retriever.Query;
import io.github.repir.tools.Lib.Log;

/**
 * The reducer is generic, using the passed query with the name of the retrieval
 * model to aggregate the results that were collected by each mapper. Each
 * reducer reduces only a single query. The incoming Query object is used to
 * reconstruct the same retrieval model in every location (mappers and reducer),
 * so that the retrieval model can process the map and writeReduce steps similar to
 * retrieval on a single machine.
 * <p/>
 * @author jeroen
 */
public class RetrieverMRReduce extends Reducer<CollectorKey, CollectorValue, NullWritable, NullWritable> {

   public static Log log = new Log(RetrieverMRReduce.class);
   Configuration conf;
   CollectorCachable collector;
   Repository repository;
   RetrieverMR retriever;
   String reducers[];
   Strategy strategy;

   @Override
   protected void setup(Context context) throws IOException, InterruptedException {
      repository = new Repository(context.getConfiguration());
      conf = repository.getConfiguration();
      retriever = new RetrieverMR(repository, context);
      reducers = conf.getStrings("retriever.reducers");

   }

   @Override
   public void reduce(CollectorKey key, Iterable<CollectorValue> tfs, Context context)
           throws IOException, InterruptedException {
      // topicrun.outfile is set to some unique name, which is used to write the query
      // output to
      if ( strategy == null ) {
         String reducer = reducers[ key.getReducer() ];
         log.info("reducer %s %b", reducer, key.isQuery);
         if (key.isQuery) {
            Query q = key.getQuery();
            q.setRepository(repository);
            strategy = retriever.constructStrategy(q);
            strategy.prepareAggregation();
            for (CollectorValue v : tfs) {
              Collector aggregator = strategy.collectors.get(v.collectorid);
              v.collector.setStrategy(strategy);
              v.collector.decode();
              aggregator.aggregate(v.collector);
            }
            strategy.collectors.finishReduce();
            q = strategy.finishReduceTask();
            //log.info("%d %s %d", q.id, q.query, q.queryresults.length);
            strategy.prepareWriteReduce(q);
            strategy.writeReduce(q);
            strategy.finishWriteReduce();
         } else {
            HashSet<Integer> partitionaggregated = new HashSet<Integer>();
            log.info("new collector %d %s %s", key.reducer, key.collector, retriever);
            Collector aggregator = key.collector;
            aggregator.setRetriever(retriever);
            if (collector == null && aggregator instanceof CollectorCachable) {
               
              collector = (CollectorCachable)aggregator;
              collector.startAppend();
            }
            for (CollectorValue v : tfs) {
               if (partitionaggregated.contains(v.partition))
                  aggregator.aggregateDuplicatePartition(v.collector);
               else {
                  aggregator.aggregate(v.collector);
                  partitionaggregated.add(v.partition);
               }
            }
            if (collector != null)
               ((CollectorCachable)aggregator).streamappend(collector);
         }
      }
   }

   @Override
   protected void cleanup(Context context) throws IOException, InterruptedException {
      if (collector != null)
         collector.finishAppend();
      Log.reportProfile();
   }
}
