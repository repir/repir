package io.github.repir.TestSet;

public class BaseMetricPrecision extends BaseMetric {

   @Override
   public double calcAfter(int position, double previousscore, int relevant) {
      //log.info("prev %f pos %d rel %d", previousscore, position, relevant);
      return (previousscore * position + relevant) / (position + 1);
   }

   @Override
   public String getName() {
      return "Precision";
   }
}
