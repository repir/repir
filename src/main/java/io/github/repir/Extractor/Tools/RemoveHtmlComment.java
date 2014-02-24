package io.github.repir.Extractor.Tools;

import io.github.repir.tools.Lib.Log;
import io.github.repir.Extractor.Entity;
import io.github.repir.Extractor.Extractor;

/**
 * Removes HTML comment that is marked with <!-- -->
 * <p/>
 * @author jbpvuurens
 */
public class RemoveHtmlComment extends ExtractorProcessor {

   public static Log log = new Log(RemoveHtmlComment.class);
   public byte open[] = "<!--".getBytes();
   public byte close[] = "-->".getBytes();

   public RemoveHtmlComment(Extractor extractor, String process) {
      super(extractor, process);
   }

   @Override
   public void process(Entity entity, Entity.SectionPos section, String attribute) {
      int startpos = section.open;
      while (true) {
         startpos = io.github.repir.tools.Lib.ByteTools.find(entity.content, open, startpos, section.close, false, false);
         if (startpos < 0) {
            break;
         }
         int closepos = io.github.repir.tools.Lib.ByteTools.find(entity.content, close, startpos, section.close, false, false);
         if (closepos < 0) {
            break;
         }
         closepos += close.length;
         for (int p = startpos; p < closepos; p++) {
            entity.content[p] = 32;
         }
         startpos = closepos;
      }
   }
}
