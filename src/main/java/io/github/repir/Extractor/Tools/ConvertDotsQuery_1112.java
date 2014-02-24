package io.github.repir.Extractor.Tools;

import io.github.repir.tools.ByteRegex.ByteRegex;
import io.github.repir.Extractor.Entity;
import io.github.repir.Extractor.Extractor;
import io.github.repir.tools.Lib.Log;
import java.util.ArrayList;
import io.github.repir.tools.DataTypes.ByteArrayPos;

/**
 * Converts dots depending on the context. Does not filter out + and - which can
 * be useful in queries.
 * <p/>
 * @author jbpvuurens
 */
public class ConvertDotsQuery_1112 extends ExtractorProcessor {

   public static Log log = new Log(ConvertDotsQuery_1112.class);
   ByteRegex number = new ByteRegex("\\.(^|(?<=[^\\w\\.]\\d\\.))\\d+(e[\\-\\+]?\\d+)?($|(?=[^\\w\\.]))");
   ByteRegex abbrev = new ByteRegex("\\.((?<=^\\c\\.)|(?<=[^\\w\\.]\\c\\.))(\\c\\.)+");
   ByteRegex other = new ByteRegex("[\\.]");
   ByteRegex combi = new ByteRegex(number, abbrev, other);

   public ConvertDotsQuery_1112(Extractor extractor, String process) {
      super(extractor, process);
   }

   @Override
   public void process(Entity entity, Entity.SectionPos pos, String attribute) {
      ArrayList<ByteRegex.Pos> positions = combi.findAll(entity.content, pos.open, pos.close);
      for (ByteRegex.Pos p : positions) {
         switch (p.pattern) {
            case 0: // number
               break;
            case 1: // abbreviation or initials
               for (int i = p.start; i < p.end - 1; i += 2) {
                  entity.content[i] = 32;
               }
               entity.content[p.end - 1] = 32;
               break;
            case 2: // other . - +
               entity.content[p.start] = 32;
         }
      }
   }
}