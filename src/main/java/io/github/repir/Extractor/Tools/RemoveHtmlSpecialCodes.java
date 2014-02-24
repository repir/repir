package io.github.repir.Extractor.Tools;

import io.github.repir.tools.ByteRegex.ByteRegex;
import io.github.repir.tools.ByteRegex.ByteRegex.Pos;
import io.github.repir.Extractor.Entity;
import io.github.repir.Extractor.EntityAttribute;
import io.github.repir.Extractor.Extractor;
import io.github.repir.tools.Lib.Log;
import java.util.ArrayList;

/**
 * Remove HTML special codes that are written in between &;, such as &tilde;
 * <p/>
 * @author jeroen
 */
public class RemoveHtmlSpecialCodes extends ExtractorProcessor {

   public static Log log = new Log(RemoveHtmlSpecialCodes.class);
   ByteRegex regex = new ByteRegex("&[A-Za-z]+;");

   private RemoveHtmlSpecialCodes(Extractor extractor, String process) {
      super(extractor, process);
   }

   @Override
   public void process(Entity entity, Entity.SectionPos section, String attribute) {
      ArrayList<Pos> pos = regex.findAll(entity.content, section.open, section.close);
      for (Pos p : pos) {
         for (int i = p.start; i < p.end; i++) {
            entity.content[i] = 32;
         }
      }
   }
}
