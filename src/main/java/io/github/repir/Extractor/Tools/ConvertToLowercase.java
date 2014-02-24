package io.github.repir.Extractor.Tools;

import io.github.repir.tools.Lib.Log;
import io.github.repir.Extractor.EntityAttribute;
import io.github.repir.Extractor.Entity;
import io.github.repir.Extractor.Extractor;

/**
 * convert all uppercase characters to lowercase. This processor is not context
 * aware so multi-byte characters such as unicode characters should be converted
 * before running this processor.
 * <p/>
 * @author jbpvuurens
 */
public class ConvertToLowercase extends ExtractorProcessor {

   public static Log log = new Log(ConvertToLowercase.class);
   public boolean highercase[] = new boolean[128];
   public boolean lowernumber[] = new boolean[128];

   public ConvertToLowercase(Extractor extractor, String process) {
      super(extractor, process);
      for (int i = 0; i < 128; i++) {
         highercase[i] = (i >= 'A' && i <= 'Z');
         lowernumber[i] = (i >= 'a' && i <= 'z') || (i >= '0' && i <= '9');
      }
   }

   @Override
   public void process(Entity entity, Entity.SectionPos section, String attribute) {
      byte buffer[] = entity.content;
      int p;
      for (p = section.open; p < section.close; p++) {
         if (highercase[buffer[p]]) {
            buffer[p] = (byte) (buffer[p] | 32);
         }
      }
   }

   public String process(String s) {
      Entity e = new Entity();
      e.content = s.getBytes();
      Entity.SectionPos pos = new Entity.SectionPos();
      pos.open = 0;
      pos.close = e.content.length;
      process(e, pos, "section");
      return new String(e.content, 0, e.content.length);
   }
}
