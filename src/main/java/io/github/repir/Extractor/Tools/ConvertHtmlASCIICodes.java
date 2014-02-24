package io.github.repir.Extractor.Tools;

import io.github.repir.tools.ByteRegex.ByteRegex;
import io.github.repir.tools.ByteRegex.ByteRegex.Pos;
import static io.github.repir.tools.Lib.ByteTools.*;
import io.github.repir.tools.Lib.Log;
import io.github.repir.Extractor.EntityAttribute;
import io.github.repir.Extractor.Entity;
import io.github.repir.Extractor.Entity.SectionPos;
import io.github.repir.Extractor.Extractor;
import java.util.ArrayList;

/**
 * Convert HTML ASCII code like &#101; to the corresponding byte.
 * <p/>
 * @author jeroen
 */
public class ConvertHtmlASCIICodes extends ExtractorProcessor {

   public static Log log = new Log(ConvertHtmlASCIICodes.class);
   private ByteRegex regex = new ByteRegex("&#\\d+;");

   public ConvertHtmlASCIICodes(Extractor extractor, String process) {
      super(extractor, process);
   }

   @Override
   public void process(Entity entity, SectionPos section, String attribute) {
      ArrayList<Pos> pos = regex.findAll(entity.content, section.open, section.close);
      for (Pos p : pos) {
         int ascii = 0;
         for (int i = p.start + 2; i < p.end - 1; i++) {
            ascii = ascii * 10 + entity.content[i] - '0';
         }
         if (ascii > 31 && ascii < 128) {
            entity.content[p.start] = (ascii > 31 && ascii < 128) ? (byte) ascii : 0;
         }
         for (int i = p.start + 1; i < p.end; i++) {
            entity.content[i] = 0;
         }
      }
   }
}
