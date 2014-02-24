package io.github.repir.TestSet;

import io.github.repir.Retriever.Query;
import io.github.repir.tools.Lib.Log;
import java.util.Collection;
import java.util.HashMap;

public class QueryMetricAP extends QueryMetric {

   public static Log log = new Log(QueryMetricAP.class);
   public double[] curve;

   public QueryMetricAP() {
      super();
   }

   @Override
   public double calculate(TestSet testset, Query query) {
      curve = new double[11];
      if (query.id > 0) {
         HashMap<Integer, HashMap<String, Integer>> qrels = testset.getQrels();
         //log.info("query id %d qrels %s", query.id, qrels);
         HashMap<String, Integer> goldset = qrels.get(query.id);
         if (goldset != null) {
            double precision[] = new BaseMetricPrecision().calcTable(query, goldset);
            double recall[] = new BaseMetricRecall().calcTable(query, qrels.get(query.id));
            for (int i = 0; i < query.queryresults.length; i++) {
               int slot = (int) Math.floor(recall[i] * 10);
               //log.info("%d", slot);
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
      return io.github.repir.tools.Lib.MathTools.Avg(curve);
   }
}
