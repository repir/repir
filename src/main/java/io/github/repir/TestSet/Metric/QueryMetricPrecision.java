package io.github.repir.TestSet.Metric;

import io.github.repir.TestSet.Metric.BaseMetricPrecision;
import io.github.repir.Retriever.Query;
import io.github.repir.TestSet.TestSet;
import io.github.repir.tools.lib.Log;
import java.io.IOException;

/**
 * Computes the Precision@rank.
 * @author jer
 */
public class QueryMetricPrecision extends QueryMetric {

   public static Log log = new Log(QueryMetricPrecision.class);

   public QueryMetricPrecision(int rank) {
      super(rank);
   }

   @Override
   public double calculate(TestSet testset, Query query) throws IOException {
      double precision[] = new BaseMetricPrecision().metricTable(query, testset.getQrels().get(testset.getQRelId(query)).relevance);
      return (rank < precision.length) ? precision[rank] : precision[precision.length - 1];
   }
}
