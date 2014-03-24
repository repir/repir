package io.github.repir.TestSet.Metric;

/**
 * Computes a table of Recall at position metrics.
 * @author jer
 */
public class BaseMetricRecall extends BaseMetric {

   @Override
   public double metricAtPosition(int position, double previous, int relevant) {
      return previous + ((relevant > 0)?1:0) / (double) this.totalrelevant;
   }
}
