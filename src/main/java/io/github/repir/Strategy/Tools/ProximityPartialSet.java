package io.github.repir.Strategy.Tools;
import java.util.ArrayList;
import java.util.Iterator;
import io.github.repir.Retriever.Document;
import io.github.repir.Strategy.Operator.Operator;
import io.github.repir.Strategy.Operator.PositionalOperator;
import io.github.repir.tools.lib.Log; 

/**
 * Matches occurrences of 2 or more Operators in any order. Every occurrence matched
 * will have {@link #getFirst()} return a new current starting position with the
 * Operator that occupies it, and 
 * {@link #otherTermList()} returns a positional ordered list of the next positions 
 * of the other terms. It is up to the caller to decide which of these partial
 * matches is valid.
 * @author Jeroen Vuurens
 */
public class ProximityPartialSet extends ProximitySet {
  public static Log log = new Log( ProximityPartialSet.class ); 

  public ProximityPartialSet(ArrayList<PositionalOperator> containedfeatures) {
     super(containedfeatures);
  }
  
  @Override
  public boolean hasProximityMatches(Document doc) {
      proximitytermlist = new ProximityTermList();
      presentterms = 0;
      for (PositionalOperator f : elements) {
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
         pollFirstLast();
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
         pollFirstLast();
         first.moveFirstBelowNext();
         return true;
      }
      return false;
   }
}
