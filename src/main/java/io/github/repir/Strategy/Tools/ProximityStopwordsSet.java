package io.github.repir.Strategy.Tools;

import java.util.ArrayList;
import io.github.repir.Retriever.Document;
import io.github.repir.Strategy.GraphNode;
import io.github.repir.Strategy.Term;
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

   public ProximityStopwordsSet(ArrayList<GraphNode> containedfeatures) {
      super(containedfeatures);
   }

   @Override
   protected long[] getDependence() {
      long dependence[] = new long[containedfeatures.size()];
      for (int i = 0; i < containedfeatures.size(); i++) {
         if (containedfeatures.get(i) instanceof Term && ((Term) containedfeatures.get(i)).isstopword) {
            long pattern = 0;
            for (int j = i - 1; j >= 0; j--) {
               pattern |= 1l << j;
               if (containedfeatures.get(j) instanceof Term && !((Term) containedfeatures.get(j)).isstopword)
                  break;
            }
            for (int j = i + 1; j < containedfeatures.size(); j++) {
               pattern |= 1l << j;
               if (containedfeatures.get(j) instanceof Term && !((Term) containedfeatures.get(j)).isstopword)
                  break;
            }
            dependence[i] = pattern;
         }
      }
      return dependence;
   }
 
}
