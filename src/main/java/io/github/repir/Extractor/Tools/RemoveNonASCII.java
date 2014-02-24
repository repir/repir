package io.github.repir.Extractor.Tools;

import io.github.repir.tools.Lib.Log;
import io.github.repir.Extractor.EntityAttribute;
import io.github.repir.Extractor.Entity;
import io.github.repir.Extractor.Extractor;

public class RemoveNonASCII extends ExtractorProcessor {

   public static Log log = new Log(RemoveNonASCII.class);

   public RemoveNonASCII(Extractor extractor, String process) {
      super(extractor, process);
   }

   @Override
   public void process(Entity entity, Entity.SectionPos section, String attribute) {
      for (int p = section.open; p < section.close; p++) {
         if (entity.content[p] < 0) {
            entity.content[p] = 32;
         }
      }
   }
}
