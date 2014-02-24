package io.github.repir.Extractor.Tools;

import io.github.repir.Extractor.Entity;
import io.github.repir.Extractor.Entity.SectionPos;
import io.github.repir.Extractor.EntityAttribute;
import io.github.repir.Extractor.Extractor;
import io.github.repir.tools.Lib.Log;

public class RemoveSection extends ExtractorProcessor {

   public static Log log = new Log(RemoveSection.class);

   public RemoveSection(Extractor extractor, String process) {
      super(extractor, process);
   }

   @Override
   public void process(Entity entity, SectionPos section, String attribute) {
      //log.info("process %d %d", section.openlead, section.closetrail);
      for (int i = section.openlead; i < section.closetrail; i++) {
         entity.content[i] = 32;
      }
   }
}
