package io.github.repir.Extractor.Tools;

import io.github.repir.tools.Lib.Log;
import io.github.repir.Extractor.EntityChannel;
import io.github.repir.Extractor.Entity;
import io.github.repir.Extractor.Extractor;

/**
 * For debug purposed only, to print a section to the log.
 * @author jer
 */
public class ExtractShow extends ExtractorProcessor {

   public static Log log = new Log(ExtractShow.class);

   public ExtractShow(Extractor extractor, String process) {
      super(extractor, process);
   }

   @Override
   public void process(Entity entity, Entity.Section section, String attribute) {
      log.print(new String(entity.content, section.open, section.close - section.open));
   }
}
