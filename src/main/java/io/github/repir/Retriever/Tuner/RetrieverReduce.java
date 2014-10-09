   package io.github.repir.Retriever.Tuner;

import io.github.repir.Repository.ModelParameters;
import io.github.repir.Repository.ModelParameters.Record;
import io.github.repir.Repository.Repository;
import io.github.repir.Retriever.MapReduce.CollectorKey;
import io.github.repir.Retriever.MapReduce.CollectorValue;
import io.github.repir.Retriever.MapReduce.Retriever;
import io.github.repir.Retriever.MapReduce.RetrieverMRReduce;
import io.github.repir.Retriever.Query;
import io.github.repir.Strategy.Collector.Collector;
import io.github.repir.Strategy.Strategy;
import io.github.repir.TestSet.Metric.QueryMetric;
import io.github.repir.TestSet.Metric.QueryMetricAP;
import io.github.repir.TestSet.ResultSet;
import io.github.repir.TestSet.TestSet;
import io.github.repir.tools.Lib.Log;
import io.github.repir.MapReduceTools.RRConfiguration;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.mapreduce.Reducer;

/**
 * Reduces Query variants, calculating the map scores for parameter settings
 * and storing the results in {@link ModelParameters}.
 * <p/>
 * @author jeroen
 */
public class RetrieverReduce extends Reducer<CollectorKey, CollectorValue, NullWritable, NullWritable> {

   public static Log log = new Log(RetrieverMRReduce.class);
   HashMap<Integer, Query> queries;
   HashMap<Integer, Strategy> strategies = new HashMap<Integer, Strategy>();
   ArrayList<Record> newRecord = new ArrayList<Record>();
   Strategy strategy;
   RRConfiguration conf;
   Repository repository;
   Retriever retriever;
   TestSet testset;
   ResultSet resultstat;
   ModelParameters modelparameters;
   String storedparameters[];
   QueryMetric metric;
   int variant = -1;

   @Override
   protected void setup(Context context) throws IOException, InterruptedException {
      repository = new Repository(context.getConfiguration());
      conf = repository.getConfiguration();
      conf.set("rr.dir", "");
      modelparameters = ModelParameters.get(repository, repository.configurationName());
      storedparameters = repository.getStoredFreeParameters();
      retriever = new Retriever(repository, context);
      metric = QueryMetric.create(conf.get("tuner.metric", "QueryMetricAP"));
   }

   @Override
   public void reduce(CollectorKey key, Iterable<CollectorValue> tfs, Context context)
           throws IOException, InterruptedException {

      if ( key.isQuery) {
            Query q = key.getQuery();
            if (q.getVariantID() != variant) {
               if (queries != null)
                  score(queries.values());
               queries = new HashMap<Integer, Query>();
               variant = q.getVariantID();
               log.info("variant %d %s", variant, q.getConfiguration());
            }
            q.setRepository(repository);
            strategy = strategies.get(q.getID());
            if (strategy == null) {
               strategy = retriever.constructStrategy(q);
               strategy.prepareAggregation();
               strategies.put(q.getID(), strategy);
            } else {
               strategy.setQuery(q);
               strategy.collectors.reuse();
            }
            for (CollectorValue v : tfs) {
              Collector aggregator = strategy.collectors.get(v.collectorid);
              v.collector.setStrategy(strategy);
              v.collector.decode();
              aggregator.aggregate(v.collector);
            }          
            strategy.collectors.finishReduce();
            q = strategy.finishReduceTask();
            q.setVariantID(0);
            log.info("query %d docs %d", q.getID(), q.getQueryResults().length);
            queries.put(q.getID(), q);
      }
   }

   @Override
   protected void cleanup(Context context) throws IOException, InterruptedException {
      if (queries != null)
         score(queries.values());
      modelparameters.setDataBufferSize(1000000);
      modelparameters.openWrite();
      for (Record r : newRecord) {
         modelparameters.write(r);
         log.info("score par=%s queries=%d map=%f", r.parameters, queries.size(), r.map);
      }
      modelparameters.closeWrite();
   }
   
   protected void score( Collection<Query> queries ) {
      if (testset == null) {
         testset = new TestSet(repository);
         resultstat = new ResultSet( metric, testset, this.queries.values());
      } else {
         resultstat.setQueries(queries);
      }
      Record r = modelparameters.newRecord(storedparameters);
      r.map = resultstat.getMean();
      newRecord.add(r);
   }
}
