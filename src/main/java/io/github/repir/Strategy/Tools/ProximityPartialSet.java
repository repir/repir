package io.github.repir.Strategy.Tools;
import java.util.ArrayList;
import java.util.Iterator;
import io.github.repir.Retriever.Document;
import io.github.repir.Strategy.GraphNode;
import io.github.repir.tools.Lib.Log; 

/**
 *
 * @author Jeroen Vuurens
 */
public class ProximityPartialSet extends ProximitySet {
  public static Log log = new Log( ProximityPartialSet.class ); 

  public ProximityPartialSet(ArrayList<GraphNode> containedfeatures) {
     super(containedfeatures);
  }
  
  @Override
  public boolean hasProximityMatches(Document doc) {
      proximitytermlist = new ProximityTermList();
      presentterms = 0;
      for (GraphNode f : containedfeatures) {
         f.process(doc);
      }
      for (ProximityTerm t : tpi) {
         t.reset();
         if (t.current < Integer.MAX_VALUE) {
            proximitytermlist.add(t);
            presentterms |= t.bitsequence;
         }
      }
      if (proximitytermlist.size() > 1) {
         pollFirst();
         first.moveFirstBelowNext();
         return true;
      } else {
         return false;
      }
   }

  @Override
   public boolean next() {
      if (first.next() < Integer.MAX_VALUE)
         proximitytermlist.add(first);
      if (proximitytermlist.size() > 1) {
         pollFirst();
         first.moveFirstBelowNext();
         return true;
      }
      return false;
   }
}
