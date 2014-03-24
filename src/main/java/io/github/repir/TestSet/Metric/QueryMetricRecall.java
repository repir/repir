package io.github.repir.TestSet.Metric;

import io.github.repir.TestSet.Metric.BaseMetricRecall;
import io.github.repir.Retriever.Query;
import io.github.repir.TestSet.TestSet;
import io.github.repir.tools.Lib.Log;

/**
 * Computes the Recall@rank
 * @author jer
 */
public class QueryMetricRecall extends QueryMetric {

   public static Log log = new Log(QueryMetricRecall.class);

   public QueryMetricRecall(int rank) {
      super(rank);
   }

   @Override
   public double calculate(TestSet testset, Query query) {
      double recall[] = new BaseMetricRecall().metricTable(query, testset.getQrels().get(query.id));
      return (rank < recall.length) ? recall[rank] : recall[recall.length - 1];
   }
}
