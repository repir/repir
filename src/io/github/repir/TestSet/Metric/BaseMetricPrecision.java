package io.github.repir.TestSet.Metric;

/**
 * Computes a table of Precision at position metrics.
 */
public class BaseMetricPrecision extends BaseMetric {

   @Override
   public double metricAtPosition(int position, double previousscore, int relevant) {      
      return (previousscore * position + ((relevant > 0)?1:0)) / (position + 1);
   }
}
