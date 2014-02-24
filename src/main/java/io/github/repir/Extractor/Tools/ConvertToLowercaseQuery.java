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
public class ConvertToLowercaseQuery extends ConvertToLowercase {

   public static Log log = new Log(ConvertToLowercaseQuery.class);
   public boolean whitespace[] = new boolean[128];
   public boolean uppercase[] = new boolean[128];

   public ConvertToLowercaseQuery(Extractor extractor, String process) {
      super(extractor, process);
      for (int i = 0; i < 128; i++) {
         whitespace[i] = (i == ' ' || i == '\t' || i == '\n' || i == '\r');
      }
      for (int i = 0; i < 128; i++) {
         uppercase[i] = (i >= 'A' && i <= 'Z');
      }
   }

   @Override
   public void process(Entity entity, Entity.SectionPos section, String attribute) {
      byte buffer[] = entity.content;
      int p;
      int start = section.open;
      boolean colonseen = false;
      for (p = section.open; p < section.close; p++) {
         if (buffer[p] == ':')
            colonseen = true;
         if (whitespace[buffer[p]] || p == section.close - 1) {
            if (colonseen) {
               colonseen = false;
            } else {
               for (int i = start; i <= p; i++) 
                  if (uppercase[ buffer[i] ] )
                     buffer[i] = (byte) (buffer[i] | 32);
            }
            start = p+1;
         }
      }
   }
}
