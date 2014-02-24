package io.github.repir.Strategy.Tools;

import java.util.ArrayList;
import java.util.Iterator;
import io.github.repir.Retriever.Document;
import io.github.repir.Strategy.GraphNode;
import io.github.repir.tools.Lib.ArrayTools;
import io.github.repir.tools.Lib.Log;

/**
 *
 * @author Jeroen Vuurens
 */
public class ProximityUnorderedSet extends ProximitySet {

   public static Log log = new Log(ProximityUnorderedSet.class);

   public ProximityUnorderedSet(ArrayList<GraphNode> containedfeatures) {
      super(containedfeatures);
   }

   public boolean hasProximityMatches(Document doc) {
      for (GraphNode f : containedfeatures) {
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
      pollFirst();
      first.moveFirstBelowNext();
      return true;
   }

   @Override
   public boolean next() {
      if (first.next() < Integer.MAX_VALUE) {
         proximitytermlist.add(first);
         pollFirst();
         first.moveFirstBelowNext();
         return true;
      }
      return false;
   }
}
