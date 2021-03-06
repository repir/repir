   package io.github.repir.Retriever.Tuner;

import java.io.IOException;
import java.util.Collection;
import java.util.TreeSet;
import io.github.repir.Repository.ModelParameters;
import io.github.repir.Retriever.Query;
import io.github.repir.TestSet.Metric.QueryMetricAP;
import io.github.repir.TestSet.ResultSet;
import io.github.repir.TestSet.TestSet;
import io.github.htools.lib.Log;

/**
 * Stores the tuned parameters into 10 separate folds, enabling parameter tuning
 * on each queries using the best result of the 9 folds that does not contain the query.
 * <p/>
 * @author jeroen
 */
public class RetrieverReduceFold extends RetrieverReduce {

   public static Log log = new Log(RetrieverReduceFold.class);
   int fold[][] = new int[10][5];
   int foldsize = 5;

   @Override
   protected void score( Collection<Query> queries ) throws IOException {
      if (testset == null) {
         testset = new TestSet(repository);
         resultstat = new ResultSet( metric, testset, this.queries.values());
         TreeSet<Integer> topics = new TreeSet<Integer>(testset.topics.keySet());
         for (int f = 0; f < 10; f++) {
            for (int i = 0; i < foldsize; i++) {
               fold[f][i] = topics.first() + f * foldsize + i;  
            }
         }
      } else {
         resultstat.setQueries(queries);
      }
      for (int f = 0; f < 10; f++) {
         double avg = 0;
         for (int i : fold[f])
            avg += resultstat.queryresult[ resultstat.getResultNumber(i) ];
         avg /= 5;
         ModelParameters.Record r = modelparameters.newRecord(storedparameters);
         r.map = avg;
         r.parameters.put("fold", Integer.toString(f));
         newRecord.add(r);
      }
   }
}
