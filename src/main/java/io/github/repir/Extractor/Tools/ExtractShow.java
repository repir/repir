package io.github.repir.Extractor.Tools;

import io.github.repir.tools.Lib.Log;
import io.github.repir.Extractor.EntityAttribute;
import io.github.repir.Extractor.Entity;
import io.github.repir.Extractor.Extractor;

public class ExtractShow extends ExtractorProcessor {

   public static Log log = new Log(ExtractShow.class);

   public ExtractShow(Extractor extractor, String process) {
      super(extractor, process);
   }

   @Override
   public void process(Entity entity, Entity.SectionPos section, String attribute) {
      log.print(new String(entity.content, section.open, section.close - section.open));
   }
}
