package io.github.repir.Strategy.Tools;
import java.util.TreeSet; 
import io.github.repir.tools.lib.Log; 
import java.util.Comparator;

/**
 * Order {@link ProximityOccurrence}s by span, then by position.
 * The method {@link #purgeOccurrencesOccupyingSameSpace() } is used to
 * remove overlapping occurrences, selecting the occurrence with the smallest 
 * span or the left most when overlapping.
 * @author Jeroen Vuurens
 */
public class MatchSetLength extends TreeSet<ProximityOccurrence> { 
   public static Comparator<ProximityOccurrence> comparator = new Comparator<ProximityOccurrence>() {
         @Override
         public int compare(ProximityOccurrence a, ProximityOccurrence b) {
            return (a.span < b.span) ? -1 : (a.span > b.span) ? 1 : a.pos - b.pos;
         }
      };
   
  public MatchSetLength() {
      super(comparator);

  }
  
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
  
  public MatchSetPositional purgeOccurrencesOccupyingSameStart() {
      MatchSetPositional results = new MatchSetPositional();
      if (size() > 1) {
         for (ProximityOccurrence m : this) {
            ProximityOccurrence s = results.floor(m);
            if (s == null || s.pos != m.pos) {
               results.add(m);
            }
         }
      } else if (size() == 1) {
         results.add(first());
      }
      return results;
  }
}
