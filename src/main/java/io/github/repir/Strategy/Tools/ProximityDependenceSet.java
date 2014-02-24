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
public abstract class ProximityDependenceSet extends ProximitySet {

   public static Log log = new Log(ProximityDependenceSet.class);

   public ProximityDependenceSet(ArrayList<GraphNode> containedfeatures) {
      super(containedfeatures);
   }

   @Override
   public boolean hasProximityMatches(Document doc) {
      proximitytermlist = new ProximityTermList();
      presentterms = 0;
      for (GraphNode f : containedfeatures) {
         f.process(doc);
      }
      for (int i = containedfeatures.size() - 1; i >= 0; i--) {
         tpi[i].reset();
         if (tpi[i].current < Integer.MAX_VALUE) {
            presentterms |= tpi[i].bitsequence;
         }
      }
      for (ProximityTerm t : tpi) {
         if (t.current < Integer.MAX_VALUE) {
            if (t.satisfiesDependency(presentterms)) {
               proximitytermlist.add(t);
            }
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
      if (first.next() < Integer.MAX_VALUE) {
         if (proximitytermlist.size() > 0) {
            proximitytermlist.add(first);
            pollFirst();
            first.moveFirstBelowNext();
            return true;
         }
      } else if (proximitytermlist.size() > 1) {
         remove(first);
         if (proximitytermlist.size() > 1) {
            pollFirst();
            first.moveFirstBelowNext();
            return true;
         }
      }
      return false;
   }

   public void remove(ProximityTerm remove) {
      if (proximitytermlist.size() > 1) {
         presentterms &= ~remove.bitsequence;
         ArrayList<ProximityTerm> toremove = new ArrayList<ProximityTerm>();
         Iterator<ProximityTerm> iter = proximitytermlist.iterator();
         while (iter.hasNext()) {
            ProximityTerm t = iter.next();
            if (!t.satisfiesDependency(presentterms)) {
               toremove.add(t);
               iter.remove();
            }
         }
         for (ProximityTerm t : toremove) {
            remove(t);
         }
      }
   }

   @Override
   protected abstract long[] getDependence();
}
