package io.github.repir.TestSet.Metric;

import io.github.repir.TestSet.Metric.BaseMetricRecall;
import io.github.repir.Retriever.Query;
import io.github.repir.TestSet.TestSet;
import io.github.repir.tools.lib.Log;
import java.io.IOException;

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
   public double calculate(TestSet testset, Query query) throws IOException {
      double recall[] = new BaseMetricRecall().metricTable(query, testset.getQrels().get(testset.getQRelId(query)).relevance);
      return (rank < recall.length) ? recall[rank] : recall[recall.length - 1];
   }
}
