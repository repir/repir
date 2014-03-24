package io.github.repir.Extractor.Tools;

import io.github.repir.tools.Lib.Log;
import io.github.repir.Extractor.EntityChannel;
import io.github.repir.EntityReader.Entity;
import io.github.repir.Extractor.Extractor;
import io.github.repir.tools.Lib.BoolTools;

/**
 * Replaces all non-ASCII characters by spaces. To prevent
 *
 * @author jer
 */
public class RemoveNonASCII extends ExtractorProcessor {

   public static Log log = new Log(RemoveNonASCII.class);
   public boolean letterdigit[];

   public RemoveNonASCII(Extractor extractor, String process) {
      super(extractor, process);
      if (extractor.getConfigurationBoolean(process, "nonasciiremoveword", false)) {
         letterdigit = BoolTools.combineRanges(
                 BoolTools.createASCIIAcceptRange('A', 'Z'),
                 BoolTools.createASCIIAcceptRange('a', 'z'),
                 BoolTools.createASCIIAcceptRange('0', '9'));
      }
   }

   @Override
   public void process(Entity entity, Entity.Section section, String attribute) {
      if (letterdigit == null) {
         for (int p = section.open; p < section.close; p++) {
            if (entity.content[p] < 0) {
               entity.content[p] = 32;
            }
         }
      } else {
         for (int p = section.open; p < section.close; p++) {
            if (entity.content[p] < 0) {
               entity.content[p] = 32;
               for (int i = p - 1; i >= section.open && letterdigit[entity.content[i] & 0xFF]; i--) {
                  entity.content[i] = 32;
               }
               for (; p < section.close && letterdigit[entity.content[p] & 0xFF]; p++) {
                  entity.content[p] = 32;
               }
            }
         }
      }
   }
}
