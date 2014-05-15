package io.github.repir.Strategy.Tools;

import java.util.ArrayList;
import io.github.repir.Retriever.Document;
import io.github.repir.Strategy.Operator.Operator;
import io.github.repir.Strategy.Operator.PositionalOperator;
import io.github.repir.Strategy.Operator.QTerm;
import io.github.repir.tools.Lib.Log;

/**
 * Implements the ProximityDependenceSet by setting the dependency for all
 * stopwords to all words on the left and right up to (inclusive) the first
 * non-stopword or query boundary. The ProximityDependenceSet uses this to remove
 * the stopwords when one of their dependent terms does not exists, so there is
 * no need to further evaluate this stopword.
 * @author Jeroen Vuurens
 */
public class ProximityStopwordsSet extends ProximityDependenceSet {

   public static Log log = new Log(ProximityStopwordsSet.class);

   public ProximityStopwordsSet(ArrayList<PositionalOperator> containedfeatures) {
      super(containedfeatures);
   }

   @Override
   protected long[] getDependence() {
      long dependence[] = new long[elements.size()];
      for (int i = 0; i < elements.size(); i++) {
         if (elements.get(i) instanceof QTerm && ((QTerm) elements.get(i)).isStopword()) {
            long pattern = 0;
            for (int j = i - 1; j >= 0; j--) {
               pattern |= 1l << j;
               if (elements.get(j) instanceof QTerm && !((QTerm) elements.get(j)).isStopword())
                  break;
            }
            for (int j = i + 1; j < elements.size(); j++) {
               pattern |= 1l << j;
               if (elements.get(j) instanceof QTerm && !((QTerm) elements.get(j)).isStopword())
                  break;
            }
            dependence[i] = pattern;
         }
      }
      return dependence;
   }
 
}
