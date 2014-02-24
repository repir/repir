package io.github.repir.Extractor;
import io.github.repir.tools.Lib.Log;

public class test {
   public static Log log = new Log(test.class);

   public static void main(String[] args) {
      String a = "aap";
      byte b[] = a.getBytes();
      b[1] = 0;
      String c = new String(b);
      log.info("%s %d", c, c.length());
   }
}
