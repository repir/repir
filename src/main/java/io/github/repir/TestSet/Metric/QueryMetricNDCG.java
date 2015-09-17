package io.github.repir.TestSet.Metric;

import io.github.repir.TestSet.Metric.BaseMetricNDCG;
import io.github.repir.Retriever.Query;
import io.github.repir.TestSet.TestSet;
import io.github.htools.lib.Log;
import java.io.IOException;

/**
 * Computes Normalized Discounted Cumulative Gain for a Query, given the relevance 
 * judgments of the TestSet.
 * @author jer
 */
public class QueryMetricNDCG extends QueryMetric {

   public static Log log = new Log(QueryMetricNDCG.class);

   public QueryMetricNDCG(int rank) {
      super(rank);
   }

   @Override
   public double calculate(TestSet testset, Query query) throws IOException {
      double ndcg[] = new BaseMetricNDCG().metricTable(query, testset.getQrels().get(testset.getQRelId(query)).relevance);
      return (rank < ndcg.length) ? ndcg[rank] : ndcg[ndcg.length - 1];
   }
}
