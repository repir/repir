package io.github.repir.TestSet;

import io.github.repir.Retriever.Query;
import java.util.HashMap;
import java.util.TreeSet;
import io.github.repir.tools.DataTypes.TreeMapComparable.TYPE;
import io.github.repir.tools.DataTypes.TreeSetComparable;

public class BaseMetricNDCG extends BaseMetric {

   public double idcg[];

   @Override
   public double normalize(int position, double metric) {
      return (position >= idcg.length) ? metric / idcg[idcg.length - 1] : metric / idcg[position];
   }

   @Override
   public double calcAfter(int position, double previousscore, int relevant) {
      if (position == 0) {
         return relevant;
      }
      return previousscore + relevant / io.github.repir.tools.Lib.MathTools.log2(position + 1);
   }

   @Override
   public String getName() {
      return "Precision";
   }

   /**
    * Sets the gold standard for a query, and additionally calculates the ideal
    * DCG (i.e. the DCG if the documents are ranked according to their gold
    * standard relevance), for computation of Normalized DCG.
    * <p/>
    * @param query
    * @param gold
    */
   @Override
   public void setTotals(Query query, HashMap<String, Integer> gold) {
      super.setTotals(query, gold);
      TreeSet<Integer> scores = new TreeSetComparable<Integer>(TYPE.DUPLICATESDESCENDING, gold.values());
      int pos = 0;
      double previous = 0;
      idcg = new double[gold.size()];
      for (Integer i : scores) {
         if (i < 0) {
            idcg[pos] = previous;
         } else {
            previous = idcg[pos] = calcAfter(pos, previous, i);
         }
         pos++;
      }
      //log.info("total %d relevant %d", this.total, this.totalrelevant);
   }
}
