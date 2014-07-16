package io.github.repir.TestSet.Metric;

import io.github.repir.Retriever.Query;
import io.github.repir.TestSet.TestSet;
import io.github.repir.tools.Lib.ClassTools;
import java.lang.reflect.Constructor;

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
   
   public static QueryMetric create(String metricclass) {
       Class clazz = ClassTools.toClass(metricclass, QueryMetric.class.getPackage().getName());
       Constructor assignableConstructor = ClassTools.getAssignableConstructor(clazz, QueryMetric.class);
       return (QueryMetric)ClassTools.construct(assignableConstructor);
   }
}
