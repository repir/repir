package io.github.repir.TestSet;

import io.github.repir.Retriever.Query;
import io.github.repir.tools.Lib.Log;

public class QueryMetricRecall extends QueryMetric {

   public static Log log = new Log(QueryMetricRecall.class);

   public QueryMetricRecall(int position) {
      super(position);
   }

   @Override
   public double calculate(TestSet testset, Query query) {
      double recall[] = new BaseMetricRecall().calcTable(query, testset.getQrels().get(query.id));
      return (position < recall.length) ? recall[position] : recall[recall.length - 1];
   }
}
