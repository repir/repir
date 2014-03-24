package io.github.repir.Extractor.Tools;

import io.github.repir.tools.Lib.Log;
import io.github.repir.EntityReader.Entity;
import io.github.repir.Extractor.Extractor;
import io.github.repir.tools.ByteSearch.ByteRegex;
import io.github.repir.tools.ByteSearch.ByteSearchPosition;

/**
 * Removes content tagged with <T#number> </T#number>, which is
 * used in some news wires for meta data.
 * <p/>
 * @author jbpvuurens
 */
public class RemoveTTag extends ExtractorProcessor {

   public static Log log = new Log(RemoveTTag.class);
   public ByteRegex open = new ByteRegex("<T[0-9]>");
   public ByteRegex close = new ByteRegex("</T[0-9>");

   public RemoveTTag(Extractor extractor, String process) {
      super(extractor, process);
   }

   @Override
   public void process(Entity entity, Entity.Section section, String attribute) {
      int startpos = section.open;
      for (ByteSearchPosition pos : open.findAllPos(entity.content, section.open, section.close)) {
         if (pos.end < section.close - 5) {
            ByteSearchPosition c = close.findPos(entity.content, pos.end, Math.min(section.close, pos.end + 40));
            if (c.found()) {
               for (int p = pos.start; p < c.end; p++) {
                  entity.content[p] = 32;
               }
            }
         }
      }
   }
}
