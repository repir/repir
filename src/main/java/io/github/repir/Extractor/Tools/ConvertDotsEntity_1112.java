package io.github.repir.Extractor.Tools;

import io.github.repir.tools.ByteRegex.ByteRegex;
import io.github.repir.tools.ByteRegex.ByteRegex.Pos;
import io.github.repir.tools.Lib.Log;
import io.github.repir.Extractor.EntityAttribute;
import io.github.repir.Extractor.Entity;
import io.github.repir.Extractor.Entity.SectionPos;
import io.github.repir.Extractor.Extractor;
import java.util.ArrayList;

/**
 * Converts dots depending on the context. Dots that are recognized as a decimal
 * point are kept. Dots that are recognized as abbreviations are removed in such
 * way the letters are connected (eg u.s.a. -> usa). Other dots are replaced by
 * spaces.
 * <p/>
 * @author jbpvuurens
 */
public class ConvertDotsEntity_1112 extends ExtractorProcessor {

   public static Log log = new Log(ConvertDotsEntity_1112.class);
   //ByteRegex number = new ByteRegex("\\.(^|(?<=[^\\c\\.]\\d\\.))\\d+(?=[^\\w\\.])");
   //ByteRegex abbrev = new ByteRegex("\\.(?<=[^\\w\\.]\\c\\.)(\\c\\.)+");
   ByteRegex abbrev = new ByteRegex("\\.(?<=[^\\c\\.]\\c\\.)(\\c\\.)+");
   ByteRegex other = new ByteRegex("[\\.]");
   // no numbers
   ByteRegex combi = new ByteRegex( abbrev, other );

   public ConvertDotsEntity_1112(Extractor extractor, String process) {
      super(extractor, process);
   }

   public void process(Entity entity, SectionPos pos, String attribute) {
      ArrayList<Pos> positions = combi.findAll(entity.content, pos.open, pos.close);
      for (Pos p : positions) {
         switch (p.pattern) {
            case 0: // abbreviation or initials
               for (int i = p.start; i < p.end - 1; i += 2)
                  entity.content[i] = 0;
               for (int i = p.start - 1; i < p.end - 1; i += 2)
                  entity.content[i] &= (255 - 32);
               entity.content[p.end-1] = 32;
               break;
            case 1: // other . - +
               entity.content[p.start] = 32;
         }
      }
   }
}