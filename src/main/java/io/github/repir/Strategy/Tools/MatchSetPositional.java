package io.github.repir.Strategy.Tools;
import java.util.Collection; 
import java.util.Comparator;
import java.util.TreeSet;
import io.github.repir.tools.Lib.Log; 

/**
 * Order {@link ProximityOccurrence}s by position.
 * @author Jeroen Vuurens
 */
public class MatchSetPositional extends TreeSet<ProximityOccurrence> { 
   public static Comparator<ProximityOccurrence> comparator = new Comparator<ProximityOccurrence>() {
         @Override
         public int compare(ProximityOccurrence a, ProximityOccurrence b) {
            return (a.pos < b.pos) ? -1 : 1;
         }
      };

  public MatchSetPositional() {
      super(comparator);
  }

   public MatchSetPositional(Collection<ProximityOccurrence> c) {
      super(comparator);
      addAll(c);
   }
}
