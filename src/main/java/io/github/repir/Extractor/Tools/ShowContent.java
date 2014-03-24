package io.github.repir.Extractor.Tools;

import io.github.repir.tools.Lib.Log;
import io.github.repir.EntityReader.Entity;
import io.github.repir.EntityReader.Entity.Section;
import io.github.repir.Extractor.Extractor;
import io.github.repir.tools.ByteSearch.ByteSearch;

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
   public void process(Entity entity, Section section, String attribute) {
         log.info("---");
         log.info("content process %s content %s", attribute, new String(entity.content, section.open, section.close - section.open));
   }
}
