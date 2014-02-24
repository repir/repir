package io.github.repir.Extractor.Tools;

import io.github.repir.Extractor.Entity;
import io.github.repir.Extractor.Extractor;
import io.github.repir.tools.Lib.BoolTools;
import io.github.repir.tools.Lib.Log;

/**
 * This processor converts tag names to lowercase, for easy processing Note:
 * this has to be done in the raw byte array instead of using regular
 * expressions as the byte array may contain non-ascii.
 * <p/>
 * @author jbpvuurens
 */
public class ConvertTagnamesToLowercase extends ExtractorProcessor {

   public static Log log = new Log(ConvertTagnamesToLowercase.class);
   public boolean[] isTagNameEnd = new boolean[128];
   char minbyte = 0;
   char maxbyte = 127;
   byte byte1;

   public ConvertTagnamesToLowercase(Extractor extractor, String process) {
      super(extractor, process);
      BoolTools.setBooleanArray(isTagNameEnd, minbyte, maxbyte, false);
      BoolTools.setBooleanArray(isTagNameEnd, true, '>', ' ', '/', '\t', '\n', '\r');
   }

   @Override
   public void process(Entity entity, Entity.SectionPos section, String attribute) {
      byte buffer[] = entity.content;
      int c, p, i, j, length;
      for (p = section.open; p < section.close;) {
         if (buffer[p++] == '<') {
            if (p < section.close && buffer[p] == '/') {
               p++;
            }
            for (byte1 = buffer[p]; p < section.close && byte1 >= minbyte && !isTagNameEnd[byte1];) {
               buffer[p] |= 32;         // transform all tagnames to lowercase
               if (++p < section.close) {
                  byte1 = buffer[p];
               }
            }
         }
      }
   }
}
