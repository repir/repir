package io.github.repir.Extractor.Tools;

import io.github.repir.tools.ByteRegex.ByteRegex;
import io.github.repir.tools.ByteRegex.ByteRegex.Pos;
import io.github.repir.Extractor.Entity;
import io.github.repir.Extractor.Entity.SectionPos;
import io.github.repir.Extractor.Extractor;
import io.github.repir.tools.Lib.Log;

/**
 * Extract HTML Metadata for the keywords and description field. This data is
 * also added to the 'all' field.
 * <p/>
 * @author jbpvuurens
 */
public class ExtractHtmlMeta extends ExtractRestore {

   public static Log log = new Log(ExtractHtmlMeta.class);
   private ByteRegex keywords = new ByteRegex("\\sname\\s*=\\s*['\"]?(keywords|description)");
   private ByteRegex content = new ByteRegex("\\scontent\\s*=\\s*['\"]");
   private ByteRegex contentend = new ByteRegex("\\Q[^'\"]");

   public ExtractHtmlMeta(Extractor extractor, String process) {
      super(extractor, process);
   }

   @Override
   public void process(Entity entity, SectionPos section, String attribute) {
      Pos p = keywords.find(entity.content, section.open, section.close);
      if (p.found()) {
         p = content.find(entity.content, section.open, section.close);
         if (p.found()) {
            Pos e = contentend.find(entity.content, p.end - 1, section.close);
            if (e.found() && e.start - 1 > p.end) {
               add(entity, p.end, e.start - 1);
            }
         }
      }
   }
}