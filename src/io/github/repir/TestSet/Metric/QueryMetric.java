package io.github.repir.TestSet.Metric;

import io.github.repir.Retriever.Query;
import io.github.repir.TestSet.TestSet;

/**
 * Computes a single metric for a query.
 * @author jer
 */
public abstract class QueryMetric {

   int rank;

   public QueryMetric() {
   }
   
   /**
    * @param rank considers only the top-n positions for the metric. 
    */
   public QueryMetric(int rank) {
      this.rank = rank;
   }

   /**
    * @param testset
    * @param query
    * @return The metric for the given {@link Query}, given the relevance judgments
    * in the {@link TestSet}.
    */
   public abstract double calculate(TestSet testset, Query query);
}
