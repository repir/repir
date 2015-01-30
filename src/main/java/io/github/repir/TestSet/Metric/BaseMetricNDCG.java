package io.github.repir.TestSet.Metric;

import io.github.repir.Retriever.Query;
import io.github.repir.tools.collection.SortableList;
import io.github.repir.tools.lib.MathTools;
import java.util.HashMap;
import java.util.TreeSet;

/**
 * Computes a table of ndcg scores for the results of a {@link Query} and a set of
 * relevance judgments. 
 * @author jer
 */
public class BaseMetricNDCG extends BaseMetric {

   public double idcg[];

   @Override
   public double normalize(int position, double metric) {
      return (position >= idcg.length) ? metric / idcg[idcg.length - 1] : metric / idcg[position];
   }

   @Override
   public double metricAtPosition(int position, double previousscore, int relevant) {
      if (position == 0) {
         return relevant;
      }
      return previousscore + relevant / MathTools.log2(position + 1);
   }

   /**
    * Sets the gold standard for a query, and additionally calculates the ideal
    * DCG (i.e. the DCG if the documents are ranked according to their gold
    * standard relevance), for computation of Normalized DCG.
    * <p/>
    * @param query
    * @param relevancejudgments
    */
   @Override
   public void setTotals(Query query, HashMap<String, Integer> relevancejudgments) {
      super.setTotals(query, relevancejudgments);
      SortableList<Integer> scores = new SortableList(relevancejudgments.values());
      int pos = 0;
      double previous = 0;
      idcg = new double[relevancejudgments.size()];
      for (Integer i : scores.sortDesc()) {
         if (i < 0) {
            idcg[pos] = previous;
         } else {
            previous = idcg[pos] = metricAtPosition(pos, previous, i);
         }
         pos++;
      }
      //log.info("total %d relevant %d", this.total, this.totalrelevant);
   }
}
