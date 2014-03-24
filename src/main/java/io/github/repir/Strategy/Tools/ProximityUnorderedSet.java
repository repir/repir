package io.github.repir.Strategy.Tools;

import java.util.ArrayList;
import io.github.repir.Retriever.Document;
import io.github.repir.Strategy.Operator.Operator;
import io.github.repir.tools.Lib.Log;

/**
 * Iterates over all co-occurrences in the document that contain all Operators in any order.
 * By definition, co-occurrences matched cannot overlap. ProximityUnorderedSet 
 * minimizes the number of occurrences returned, bringing all operators before the last
 * to a position as close as possible and before the last position. However, it does not
 * look ahead, and therefore cannot determine whether the last Operator will be pat of a 
 * smaller occurrence later on. The returned co-occurrences may therefore contain
 * overlapping positions, which should be pruned by the caller.
 * 
 * @author Jeroen Vuurens
 */
public class ProximityUnorderedSet extends ProximitySet {

   public static Log log = new Log(ProximityUnorderedSet.class);

   public ProximityUnorderedSet(ArrayList<Operator> containedfeatures) {
      super(containedfeatures);
   }

   public boolean hasProximityMatches(Document doc) {
      for (Operator f : containedfeatures) {
         f.process(doc);
      }
      for (int i = containedfeatures.size() - 1; i >= 0; i--) {
         tpi[i].reset();
         if (tpi[i].current == Integer.MAX_VALUE) {
            return false;
         }
      }
      
      proximitytermlist = new ProximityTermList();
      for (ProximityTerm t : tpi) {
         proximitytermlist.add(t);
      }
      pollFirstLast();
      first.moveFirstBelowNext();
      if ( last.current + last.span - first.current > maximumspan ) {
         return next();
      } 
      return true;
   }

   @Override
   public boolean next() {
      do {
         if (first.next() == Integer.MAX_VALUE)
            return false;
         proximitytermlist.add(first);
         pollFirstLast();
         first.moveFirstBelowNext();
      } while (last.current + last.span - first.current > maximumspan);
      return true;
   }
}
