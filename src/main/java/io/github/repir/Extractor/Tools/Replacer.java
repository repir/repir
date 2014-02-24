package io.github.repir.Extractor.Tools;

import io.github.repir.tools.Lib.Log;
import io.github.repir.Extractor.Entity;
import io.github.repir.Extractor.Extractor;

/**
 *
 * @author jbpvuurens
 */
public abstract class Replacer extends ExtractorProcessor {

   public static Log log = new Log(Replacer.class);
   protected byte[] presearch;
   protected byte[] postsearch;
   protected boolean[] middle = new boolean[128];
   protected byte[] replace;

   public Replacer(Extractor extractor, String process, String presearch, String postsearch, String replace) {
      super(extractor, process);
      this.presearch = presearch.getBytes();
      this.postsearch = postsearch.getBytes();
      this.replace = replace.getBytes();
      for (int i = 0; i < 128; i++) {
         middle[i] = false;
      }
   }

   @Override
   public void process(Entity entity, Entity.SectionPos section, String attribute) {
      byte buffer[] = entity.content;
      int c, p, i, j, length, end;
      end = section.close - presearch.length - postsearch.length;
      for (p = section.open; p < end; p++) {
         if (buffer[p] == presearch[0]) {
            for (i = 1; i < presearch.length && buffer[p + i] == presearch[i]; i++);
            if (i == presearch.length) {
               j = p + i;
               while (j < section.close) {
                  if (buffer[j] == postsearch[0] && j + postsearch.length < section.close) {
                     for (i = 1; i < postsearch.length && buffer[p + i] == postsearch[i]; i++);
                     if (i == postsearch.length) {
                        j = j + i;
                        for (i = 0; i < replace.length; i++) {
                           buffer[p + i] = replace[i];
                        }
                        for (p = p + i; p < j; p++) {
                           buffer[p] = 32;
                        }
                        break;
                     }
                  }
                  if (buffer[j] >= 0 && !middle[ buffer[j]]) {
                     break;
                  }
                  j++;
               }
            }
         }
      }
   }

   public void setAcceptable(char from, char to) {
      for (; from <= to; from++) {
         middle[from] = true;
      }
   }

   public void setAcceptable(char... c) {
      for (char b : c) {
         middle[b] = true;
      }
   }
}
