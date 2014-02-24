   package io.github.repir.RetrieverTuner;

import java.io.IOException;
import java.util.Collection;
import java.util.TreeSet;
import io.github.repir.Repository.ModelParameters;
import io.github.repir.Retriever.Query;
import io.github.repir.TestSet.QueryMetricAP;
import io.github.repir.TestSet.ResultStat;
import io.github.repir.TestSet.TestSet;
import io.github.repir.tools.Lib.Log;

/**
 * Stores the tuned parameters into 10 separate folds, enabling parameter tuning
 * on each queries using the best result of the 9 folds that does not contain the query.
 * <p/>
 * @author jeroen
 */
public class RetrieverTuneFoldReduce extends RetrieverTuneReduce {

   public static Log log = new Log(RetrieverTuneFoldReduce.class);
   int fold[][] = new int[10][5];
   int foldsize = 5;

   @Override
   protected void score( Collection<Query> queries ) {
      if (testset == null) {
         testset = new TestSet(repository);
         QueryMetricAP ap = new QueryMetricAP();
         resultstat = new ResultStat( ap, testset, this.queries.values());
         TreeSet<Integer> topics = new TreeSet<Integer>(testset.topics.keySet());
         for (int f = 0; f < 10; f++) {
            for (int i = 0; i < foldsize; i++) {
               fold[f][i] = topics.first() + f * foldsize + i;  
            }
         }
      } else {
         resultstat.setQueries(queries);
      }
      resultstat.calculateMeasure();
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
