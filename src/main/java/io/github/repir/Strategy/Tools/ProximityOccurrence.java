package io.github.repir.Strategy.Tools;
import io.github.repir.tools.Lib.Log; 

/**
 *
 * @author Jeroen Vuurens
 */
public class ProximityOccurrence implements Comparable<ProximityOccurrence> {
   public final int pos; 
   public final int span;

  public ProximityOccurrence(int pos, int span) {
      this.pos = pos;
      this.span = span;
  }

   @Override
   public int compareTo(ProximityOccurrence o) {
      return (span < o.span) ? -1 : (span > o.span) ? 1 : pos - o.pos;
   }
   
   public String toString() {
      return "(" + pos + "," + span + ")";  
   }

}
