package io.github.repir.TestSet;

import io.github.repir.Retriever.Query;
import io.github.repir.tools.Lib.Log;

public class QueryMetricPrecision extends QueryMetric {

   public static Log log = new Log(QueryMetricPrecision.class);

   public QueryMetricPrecision(int position) {
      super(position);
   }

   @Override
   public double calculate(TestSet testset, Query query) {
      double precision[] = new BaseMetricPrecision().calcTable(query, testset.getQrels().get(query.id));
      return (position < precision.length) ? precision[position] : precision[precision.length - 1];
   }
}
