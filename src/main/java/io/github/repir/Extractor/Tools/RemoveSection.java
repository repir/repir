package io.github.repir.Extractor.Tools;

import io.github.repir.Extractor.Entity;
import io.github.repir.Extractor.Entity.Section;
import io.github.repir.Extractor.EntityChannel;
import io.github.repir.Extractor.Extractor;
import io.github.repir.tools.Lib.Log;

/**
 * Removes a marked section in the {@link Entity}'s content.
 * @author jer
 */
public class RemoveSection extends ExtractorProcessor {

   public static Log log = new Log(RemoveSection.class);

   public RemoveSection(Extractor extractor, String process) {
      super(extractor, process);
   }

   @Override
   public void process(Entity entity, Section section, String attribute) {
      //log.info("process %d %d", section.openlead, section.closetrail);
      for (int i = section.openlead; i < section.closetrail; i++) {
         entity.content[i] = 32;
      }
   }
}
