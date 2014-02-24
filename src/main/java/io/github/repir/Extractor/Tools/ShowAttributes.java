package io.github.repir.Extractor.Tools;

import io.github.repir.tools.Lib.Log;
import io.github.repir.Extractor.Entity;
import io.github.repir.Extractor.Entity.SectionPos;
import io.github.repir.Extractor.EntityAttribute;
import io.github.repir.Extractor.Extractor;
import java.util.Map;

/**
 * Shows the current assigned attributes for debugging
 * <p/>
 * @author jeroen
 */
public class ShowAttributes extends ExtractorProcessor {

   public static Log log = new Log(ShowAttributes.class);

   public ShowAttributes(Extractor extractor, String process) {
      super(extractor, process);
   }

   @Override
   public void process(Entity entity, SectionPos section, String attribute) {
      for (Map.Entry<String, EntityAttribute> entry : entity.entrySet()) {
         log.info("%s=%s", entry.getKey(), entry.getValue().getContentStr());
      }
   }
}
