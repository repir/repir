package io.github.repir.TestSet;

import io.github.repir.Retriever.Query;

public abstract class QueryMetric {

   int position;

   public QueryMetric() {
   }

   public QueryMetric(int position) {
      this.position = position;
   }

   public abstract double calculate(TestSet testset, Query query);
}
