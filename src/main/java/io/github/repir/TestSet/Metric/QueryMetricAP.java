package io.github.repir.TestSet.Metric;

import io.github.repir.Retriever.Query;
import io.github.repir.TestSet.Qrel.QRel;
import io.github.repir.TestSet.TestSet;
import io.github.repir.tools.lib.Log;
import java.io.IOException;
import java.util.HashMap;

/**
 * Computes the Average Precision for a {@link Query}, which is the average over
 * 11 Recall points 0.0, 0.1, .. 1,0.
 * @author jer
 */
public class QueryMetricAP extends QueryMetric {

   public static Log log = new Log(QueryMetricAP.class);
   public double[] curve;

   public QueryMetricAP() {
      super();
   }

   @Override
   public double calculate(TestSet testset, Query query) throws IOException {
      curve = new double[11];
      if (testset.getQRelId(query) > 0) {
         HashMap<Integer, QRel> qrels = testset.getQrels();
         HashMap<String, Integer> goldset = qrels.get(testset.getQRelId(query)).relevance;
         if (goldset != null) {
            double precision[] = new BaseMetricPrecision().metricTable(query, goldset);
            double recall[] = new BaseMetricRecall().metricTable(query, goldset);
            for (int i = 0; i < query.getQueryResults().length; i++) {
               int slot = (int) Math.floor(recall[i] * 10);
               for (int j = slot; j >= 0 && precision[i] > curve[j]; j--) {
                  curve[j] = precision[i];
               }
            }
            double sum = 0;
            for (int i = 0; i < 11; i++) {
               sum += curve[i];
            }
         }
      }
      return io.github.repir.tools.lib.DoubleTools.mean(curve);
   }
}
