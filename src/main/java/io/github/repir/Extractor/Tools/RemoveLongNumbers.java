package io.github.repir.Extractor.Tools;

import io.github.repir.tools.Lib.Log;
import io.github.repir.Extractor.Entity;
import io.github.repir.Extractor.Extractor;
import io.github.repir.tools.Lib.BoolTools;
import java.util.Iterator;

/**
 * Remove tokens that are longer than (default=5) digits. Note, this process can
 * only be used if the tokens only contain ASCII characters. The maximum length
 * can be set in the configuration e.g. extractor.<process>.removelongnumbers =
 * 5
 * <p/>
 * @author jeroen
 */
public class RemoveLongNumbers extends ExtractorProcessor {

   private static Log log = new Log(RemoveLongNumbers.class);
   boolean number[] = BoolTools.createASCIIAcceptRange('0', '9');
   final int maxlength;

   public RemoveLongNumbers(Extractor extractor, String process) {
      super(extractor, process);
      number['.'] = true;
      maxlength = extractor.conf.getInt("extractor." + process + ".removelongnumbers", 5);
   }

   @Override
   public void process(Entity entity, Entity.SectionPos section, String attribute) {
      int j;
      Iterator<String> iter = entity.get(attribute).iterator();
      while (iter.hasNext()) {
         String chunk = iter.next();
         if (chunk.length() > maxlength) {
            char c = chunk.charAt(0);
            if (c <= '9' && c >= '0') {
               iter.remove();
            }
         }
      }
   }
}