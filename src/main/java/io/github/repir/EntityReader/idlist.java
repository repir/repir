package io.github.repir.EntityReader;
import io.github.repir.tools.Lib.Log; 

/**
 *
 * @author Jeroen Vuurens
 */
public class idlist {
   int[] spam = new int[10000031 / 32]; 

  public void set(String cwid) {
      int id = Integer.parseInt(cwid.substring(20)) + 100000 * Integer.parseInt(cwid.substring(17, 19));
      int pos = id / 32;
      int bit = id % 32;
      spam[pos] |= (1 << bit);

  }

   public boolean get(String cwid) {
      int id = Integer.parseInt(cwid.substring(20)) + 100000 * Integer.parseInt(cwid.substring(17, 19));
      int pos = id / 32;
      int bit = id % 32;
      return (spam[pos] & (1 << bit)) > 0;
   }

}
