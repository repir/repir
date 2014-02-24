package io.github.repir.TestSet;

import io.github.repir.Retriever.Query;
import io.github.repir.tools.Lib.Log;

public class QueryMetricNDCG extends QueryMetric {

   public static Log log = new Log(QueryMetricNDCG.class);

   public QueryMetricNDCG(int position) {
      super(position);
   }

   @Override
   public double calculate(TestSet testset, Query query) {
      double ndcg[] = new BaseMetricNDCG().calcTable(query, testset.getQrels().get(query.id));
      return (position < ndcg.length) ? ndcg[position] : ndcg[ndcg.length - 1];
   }
}
