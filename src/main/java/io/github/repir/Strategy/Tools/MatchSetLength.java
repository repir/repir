package io.github.repir.Strategy.Tools;
import java.util.TreeSet; 
import io.github.repir.tools.Lib.Log; 

/**
 *
 * @author Jeroen Vuurens
 */
public class MatchSetLength extends TreeSet<ProximityOccurrence> { 

  public MatchSetPositional purgeOccurrencesOccupyingSameSpace() {
      MatchSetPositional results = new MatchSetPositional();
      if (size() > 1) {
         for (ProximityOccurrence m : this) {
            ProximityOccurrence s = results.floor(m);
            if (s == null || s.span + s.pos <= m.pos) {
               ProximityOccurrence r = results.ceiling(m);
               if (r == null || m.span + m.pos <= r.pos) {
                  results.add(m);
               }
            }
         }
      } else if (size() == 1) {
         results.add(first());
      }
      return results;

  }

}
