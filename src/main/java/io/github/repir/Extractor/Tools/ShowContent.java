package io.github.repir.Extractor.Tools;

import io.github.repir.tools.Lib.Log;
import io.github.repir.Extractor.Entity;
import io.github.repir.Extractor.Entity.SectionPos;
import io.github.repir.Extractor.Extractor;

/**
 * Shows the current raw content buffer for debugging
 * <p/>
 * @author jeroen
 */
public class ShowContent extends ExtractorProcessor {

   public static Log log = new Log(ShowContent.class);

   public ShowContent(Extractor extractor, String process) {
      super(extractor, process);
   }

   @Override
   public void process(Entity entity, SectionPos section, String attribute) {
      log.info("content process %s content %s", attribute, new String(entity.content, section.open, section.close - section.open));
   }
}
