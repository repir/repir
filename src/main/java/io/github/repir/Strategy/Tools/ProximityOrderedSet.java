package io.github.repir.Strategy.Tools;
import java.util.ArrayList;
import io.github.repir.Retriever.Document;
import io.github.repir.Strategy.Operator.Operator;
import io.github.repir.Strategy.Operator.PositionalOperator;
import io.github.repir.tools.lib.Log; 

/**
 * Returns only occurrences of all Operators in the order of the List that was
 * given upon initialization.
 * @author Jeroen Vuurens
 */
public class ProximityOrderedSet extends ProximitySet {
  public static Log log = new Log( ProximityOrderedSet.class );

  public ProximityOrderedSet(ArrayList<PositionalOperator> containedfeatures) {
     super(containedfeatures);
     
     // uses a fixed proximitytermlist
     proximitytermlist = new ProximityTermList();
     for (ProximityTerm t : tpi)
        proximitytermlist.add(t);
     last = tpi[tpi.length-1];
     first = tpi[0];
  }
  
   /*
    * Initially each ProximityTerm starts at the beginning of its postinglist
    * To start at a valid ordered co-occurrence, this method adjust the starting
    * position.
    */  
  public boolean hasProximityMatches(Document doc) {
      boolean ok = true;
      for (PositionalOperator f : elements) {
         f.process(doc);
         if (f.getPos().length == 0) {
            ok = false;
            // possibly we can break here
         }
      }
      if (ok) {
         for (int i = elements.size() - 1; i >= 0; i--) {
            tpi[i].reset();
         }
         for (int i = 1; i < elements.size(); i++) {
            while (tpi[i].current < tpi[i - 1].current) {
               if (tpi[i].move() == Integer.MAX_VALUE)
                  return false;
            }  
         }
         shrink();
         while ( last.current + last.span - first.current > maximumspan  ) {
            if (last.move() == Integer.MAX_VALUE)
               return false;
            shrink();
         }
      }
      return ok;
   }
  
   public boolean next() {
      int firstpos = tpi[0].current;
      while ((tpi[0].current == firstpos || last.current + last.span - first.current > maximumspan) && last.move() < Integer.MAX_VALUE) {
         shrink();
      }
      return (last.current < Integer.MAX_VALUE);
   }
  
   private void shrink() {
      for (int i = elements.size() - 2; i >= 0; i--) {
         while (tpi[i].next < tpi[i+1].current) {
            tpi[i].move();
         }
      }
   }
}
