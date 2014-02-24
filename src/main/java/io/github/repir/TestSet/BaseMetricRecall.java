package io.github.repir.TestSet;

import java.util.HashMap;
import java.util.Map;

public class BaseMetricRecall extends BaseMetric {

   @Override
   public double calcAfter(int position, double previous, int relevant) {
      return previous + relevant / (double) this.totalrelevant;
   }

   @Override
   public String getName() {
      return "Recall";
   }
}
