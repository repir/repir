package io.github.repir.Strategy.Tools;

import io.github.repir.Retriever.Document;
import io.github.repir.Strategy.Operator.Operator;
import io.github.repir.Strategy.Operator.PositionalOperator;
import io.github.repir.tools.lib.Log;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Like {@link ProximityPartialSet}, occurrences of 2 or more Operators are
 * returned, but dependencies between Operators are considered. For example,
 * when all partial combinations of "Joan of Arc" are matched, but "of" should
 * only be matched when "Joan" and "Arc" are also present, "of" can be made
 * dependent on the other Operators. When the dependencies for "of" are not
 * satisfied, it will no longer be used for that document. In this case, the
 * iterator will return false for
 * {@link #hasProximityMatches(io.github.repir.Retriever.Document)} for
 * documents that do not contain both "Joan" and "Arc", and will return false on
 * {@link #next()} when the last occurrence of either is passed.
 *
 * @author Jeroen Vuurens
 */
public abstract class ProximityDependenceSet extends ProximityPartialSet {

   public static Log log = new Log(ProximityDependenceSet.class);

   public ProximityDependenceSet(ArrayList<PositionalOperator> containedfeatures) {
      super(containedfeatures);
   }

   @Override
   public boolean hasProximityMatches(Document doc) {
      proximitytermlist = new ProximityTermList();
      presentterms = 0;
      for (PositionalOperator f : elements) {
         f.process(doc);
      }
      for (int i = elements.size() - 1; i >= 0; i--) {
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
         pollFirstLast();
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
            pollFirstLast();
            first.moveFirstBelowNext();
            return true;
         }
      } else if (proximitytermlist.size() > 1) {
         remove(first);
         if (proximitytermlist.size() > 1) {
            pollFirstLast();
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
