package io.github.repir.Extractor.Tools;

import io.github.repir.tools.Lib.Log;
import io.github.repir.Extractor.EntityAttribute;
import io.github.repir.Extractor.Entity;
import io.github.repir.Extractor.Extractor;

/**
 *
 * @author jbpvuurens
 */
public class ConvertHtmlAmpersandOld extends ExtractorProcessor {

   public static Log log = new Log(ConvertHtmlAmpersandOld.class);

   public ConvertHtmlAmpersandOld(Extractor extractor, String process) {
      super(extractor, process);
   }

   @Override
   public void process(Entity entity, Entity.SectionPos section, String attribute) {
      byte buffer[] = entity.content;
      int p, t, oldp;
      for (p = section.open; p < section.close - 6; p++) {
         if (buffer[p] == '&' && buffer[p + 1] == 'a' && buffer[p + 2] == 'm'
                 && buffer[p + 3] == 'p' && buffer[p + 4] == ';') {
            if (p > 0 && ((buffer[p - 1] >= 'A' && buffer[p - 1] <= 'Z')
                    || (buffer[p - 1] >= 'a' && buffer[p - 1] <= 'z'))) {
               if (((buffer[p + 5] >= 'A' && buffer[p + 5] <= 'Z')
                       || (buffer[p + 5] >= 'a' && buffer[p + 5] <= 'z'))) {
                  oldp = p;
                  for (t = p + 5; t < section.close && buffer[t] != ' '; t++) {
                     buffer[t - 4] = buffer[t];
                  }
                  for (p = t - 4; p < t; p++) {
                     buffer[p] = ' ';
                  }
               }
            }
         }
      }
   }
}
