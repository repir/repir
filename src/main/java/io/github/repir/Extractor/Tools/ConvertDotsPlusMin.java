package io.github.repir.Extractor.Tools;

import io.github.repir.tools.ByteRegex.ByteRegex;
import io.github.repir.tools.Lib.Log;
import io.github.repir.Extractor.EntityAttribute;
import io.github.repir.Extractor.Entity;
import io.github.repir.Extractor.Entity.SectionPos;
import io.github.repir.Extractor.Extractor;

/**
 * Converts dots depending on the context. Dots that are recognized as a decimal
 * point are kept. Dots that are recognized as abbreviations are removed in such
 * way the letters are connected (eg u.s.a. -> usa). Other dots are replaced by
 * spaces.
 * <p/>
 * @author jbpvuurens
 */
public class ConvertDotsPlusMin extends ExtractorProcessor {

   public static Log log = new Log(ConvertDotsPlusMin.class);
   int highercase = 255 - 32;
   boolean isChar[] = getChar();
   boolean isDigit[] = io.github.repir.tools.Lib.ByteTools.getByteArray("0123456789");
   ByteRegex number = new ByteRegex("(?<=[^\\w\\.]])\\d+\\.\\d+(e[-+]?\\d+)?(?=[^\\w\\.])");
   public boolean isPlusMin[] = io.github.repir.tools.Lib.ByteTools.getByteArray("+-");
   public boolean filterPlusMin[] = io.github.repir.tools.Lib.ByteTools.getByteArray("+-");

   public ConvertDotsPlusMin(Extractor extractor, String process) {
      super(extractor, process);
   }

   private boolean[] getChar() {
      boolean c[] = new boolean[128];
      for (int i = 0; i < 128; i++) {
         c[i] = (i >= 'a' && i <= 'z') || (i >= 'A' && i <= 'Z') || (i >= '0' && i <= '9');
      }
      return c;
   }

   public void process2(Entity entity, SectionPos section, String attribute) {
      byte buffer[] = entity.content;
      if (section.open < section.close && buffer[section.open] == '.') {
         buffer[section.open] = 32;
      }
      for (int p = section.open + 1; p < section.close - 2; p++) {
         if (buffer[p] == '.') {
            if (isDigit[buffer[p - 1]] && isDigit[buffer[p + 1]]) { // check for decimal point
               for (; p < section.close - 1 && isDigit[buffer[p + 1]]; p++);
               if (p < section.close - 2 && buffer[p + 1] == 'e'
                       && (isDigit[buffer[p + 2]] || (p < section.close - 3 && isPlusMin[buffer[p + 2]] && isDigit[buffer[p + 3]]))) {
                  for (p += 1; p < section.close - 1 && !isDigit[buffer[p + 1]]; p++);
                  for (; p < section.close - 1 && isDigit[buffer[p + 1]]; p++);
               }
            } else if (buffer[p + 2] == '.' && isChar[buffer[p + 1]] && isChar[buffer[p - 1]]
                    && (p == section.open + 1 || !isChar[buffer[p - 2]])) {
               int sp = p - 1;
               int pp = p + 3;
               buffer[sp++] = (byte) (buffer[p - 1] | 32);
               buffer[sp++] = (byte) (buffer[p + 1] | 32);
               for (; pp < section.close - 2 && buffer[pp + 1] == '.' && isChar[buffer[pp]]; pp += 2) {
                  buffer[sp++] = (byte) (buffer[pp] | 32);
               }
               for (; sp < pp; sp++) {
                  buffer[sp] = 32;
               }
               p = sp - 1;
            } else {
               buffer[p] = 32;
            }
         } else if (filterPlusMin[buffer[p]] && (p >= section.close - 1 || !isDigit[buffer[p + 1]])) {
            buffer[p] = 32;
         }
      }
   }

   public void process(Entity entity, SectionPos pos, String attribute) {
      byte buffer[] = entity.content;
      if (pos.open < pos.close && buffer[pos.open] == '.') {
         buffer[pos.open] = 32;
      }
      for (int p = pos.open + 1; p < pos.close - 2; p++) {
         if (buffer[p] == '.') {
            if (isDigit[buffer[p - 1]] && isDigit[buffer[p + 1]]) { // check for decimal point
               for (; p < pos.close - 1 && isDigit[buffer[p + 1]]; p++);
               if (p < pos.close - 2 && buffer[p + 1] == 'e'
                       && (isDigit[buffer[p + 2]] || (p < pos.close - 3 && isPlusMin[buffer[p + 2]] && isDigit[buffer[p + 3]]))) {
                  for (p += 1; p < pos.close - 1 && !isDigit[buffer[p + 1]]; p++);
                  for (; p < pos.close - 1 && isDigit[buffer[p + 1]]; p++);
               }
            } else if (isChar[buffer[p - 1]]
                    && (p == pos.open + 1 || !isChar[buffer[p - 2]])) {
               if (isChar[buffer[p + 1]] && buffer[p + 2] == '.') {
                  buffer[p] = 0;
               } else {
                  buffer[p] = 32;
               }
            } else {
               buffer[p] = 32;
            }
         } else if (filterPlusMin[buffer[p]] && (p >= pos.close - 1 || !isDigit[buffer[p + 1]])) {
            buffer[p] = 32;
         }
      }
   }
}
