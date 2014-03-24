package io.github.repir.TestSet.Metric;

import io.github.repir.TestSet.Metric.BaseMetricPrecision;
import io.github.repir.Retriever.Query;
import io.github.repir.TestSet.TestSet;
import io.github.repir.tools.Lib.Log;

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
   public double calculate(TestSet testset, Query query) {
      double precision[] = new BaseMetricPrecision().metricTable(query, testset.getQrels().get(query.id));
      return (rank < precision.length) ? precision[rank] : precision[precision.length - 1];
   }
}
