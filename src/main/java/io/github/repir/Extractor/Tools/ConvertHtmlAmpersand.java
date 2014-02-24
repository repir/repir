package io.github.repir.Extractor.Tools;

import io.github.repir.tools.ByteRegex.ByteRegex;
import io.github.repir.tools.ByteRegex.ByteRegex.Pos;
import io.github.repir.tools.Lib.Log;
import io.github.repir.Extractor.EntityAttribute;
import io.github.repir.Extractor.Entity;
import io.github.repir.Extractor.Extractor;

/**
 *
 * @author jbpvuurens
 */
public class ConvertHtmlAmpersand extends ExtractorProcessor {

   public static Log log = new Log(ConvertHtmlAmpersand.class);
   ByteRegex regex = new ByteRegex("&amp;");

   public ConvertHtmlAmpersand(Extractor extractor, String process) {
      super(extractor, process);
   }

   @Override
   public void process(Entity entity, Entity.SectionPos section, String attribute) {
      for (Pos p : regex.findAll(entity.content, section.open, section.close)) {
         for (int i = p.start + 1; i < p.end; i++) {
            entity.content[i] = 0;
         }
      }
   }
}
