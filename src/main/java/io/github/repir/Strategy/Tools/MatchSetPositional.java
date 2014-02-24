package io.github.repir.Strategy.Tools;
import java.util.Collection; 
import java.util.Comparator;
import java.util.TreeSet;
import io.github.repir.tools.Lib.Log; 

/**
 *
 * @author Jeroen Vuurens
 */
public class MatchSetPositional extends TreeSet<ProximityOccurrence> { 

  public MatchSetPositional() {
      super(new Comparator<ProximityOccurrence>() {
         public int compare(ProximityOccurrence a, ProximityOccurrence b) {
            return (a.pos < b.pos) ? -1 : 1;
         }
      });

  }

   public MatchSetPositional(Collection<ProximityOccurrence> c) {
      super(new Comparator<ProximityOccurrence>() {
         public int compare(ProximityOccurrence a, ProximityOccurrence b) {
            return (a.pos < b.pos) ? -1 : 1;
         }
      });
      addAll(c);
   }

}
