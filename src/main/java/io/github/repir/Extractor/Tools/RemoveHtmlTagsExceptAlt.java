package io.github.repir.Extractor.Tools;

import io.github.repir.tools.Lib.Log;
import static io.github.repir.tools.Lib.ByteTools.*;
import io.github.repir.tools.DataTypes.Tuple2;
import io.github.repir.Extractor.Entity;
import io.github.repir.Extractor.Extractor;

/**
 * Removes HTML Tags, except for the contents in the ALT attribute
 * <p/>
 * @author jeroen
 */
public class RemoveHtmlTagsExceptAlt extends ExtractorProcessor {

   public static Log log = new Log(RemoveHtmlTagsExceptAlt.class);
   public boolean tagname[] = new boolean[128];
   public byte alt[] = "alt".getBytes();
   public byte tagstart[] = "<".getBytes();
   public byte tagend[] = ">".getBytes();

   private RemoveHtmlTagsExceptAlt(Extractor extractor, String process) {
      super(extractor, process);
      for (int i = 0; i < 128; i++) {
         tagname[i] = (i >= 'a' && i <= 'z') || (i >= 'A' && i <= 'Z') || i == '/';
      }
   }

   @Override
   public void process(Entity entity, Entity.SectionPos section, String attribute) {
      //log.info("process %d %d", startpos, endpos);
      int startpos = section.open;
      int endpos = section.close;
      Tuple2<Integer, Integer> pos;
      while ((pos = find(entity.content, tagstart, tagend, startpos, endpos, false, true)) != null) {
         if (tagname[entity.content[pos.value1 + 1]]) {
            int altpos = pos.value1;
            int altendpos = -1;
            while (altendpos == -1 && (altpos = find(entity.content, alt, altpos, pos.value2, true, true)) > 0) {
               altpos += alt.length;
               int quotestart = skipIgnoreWS(entity.content, " = ".getBytes(), altpos, pos.value2, false);
               if (quotestart > -1) {
                  altpos = quotestart;
                  altendpos = findEndQuote(entity.content, altpos, pos.value2);
               }
               //log.info("ALT %d %d %d %s", altpos, quotestart, altendpos, new String(buffer, altpos, 100));
            }
            if (altendpos > 0) {
               for (int p = pos.value1; p < altpos; p++) {
                  entity.content[p] = 32;
               }
            }
            for (int p = (altendpos > 0) ? altendpos : pos.value1; p <= pos.value2; p++) {
               entity.content[p] = 32;
            }
            startpos = pos.value2 + 1;
         } else {
            startpos = pos.value1 + 1;
         }
      }
      //log.info("afterTagEraser %s", new String(buffer, 0, endpos));
   }
}
