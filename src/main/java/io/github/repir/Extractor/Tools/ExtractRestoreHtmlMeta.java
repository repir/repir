package io.github.repir.Extractor.Tools;

import io.github.repir.tools.ByteSearch.ByteRegex;
import io.github.repir.tools.ByteSearch.ByteSearchPosition;
import io.github.repir.EntityReader.Entity;
import io.github.repir.EntityReader.Entity.Section;
import io.github.repir.Extractor.Extractor;
import io.github.repir.tools.ByteSearch.ByteSearch;
import io.github.repir.tools.Lib.Log;
import java.util.ArrayList;

/**
 * Extract and restore HTML Metadata for the keywords and description field.
 * This data is also added to the 'all' field.
 * <p/>
 * @author jbpvuurens
 */
public class ExtractRestoreHtmlMeta extends ExtractRestore {

   public static Log log = new Log(ExtractRestoreHtmlMeta.class);
   private ByteSearch keywords = ByteSearch.create("\\sname\\s*=\\s*(keywords|description|'keywords'|'description'|\"keywords\"|\"description\")");
   private ByteSearch endtag = ByteSearch.create(">");
   private ByteSearch content = ByteSearch.create("\\scontent\\s*=\\s*\\Q");
   private boolean quote[] = new boolean[256];

   public ExtractRestoreHtmlMeta(Extractor extractor, String process) {
      super(extractor, process);
      for (int i = 0; i < 256; i++) {
         quote[i] = ((i == '\'') || (i == '"'));
      }
   }

   @Override
   public void process(Entity entity, Section section, String attribute) {
      ByteSearchPosition metatag = keywords.findPos(entity.content, section.open, section.close);
      if (metatag.found()) {
         int tagend = endtag.findQuoteSafe(entity.content, metatag.end, section.close);
         if (tagend > -1) {
            metatag = content.findPos(entity.content, metatag.end, tagend);
            if (metatag.found()) {
               int start, end;
               for (start = metatag.start + 8; !quote[entity.content[start] & 0xFF]; start++);
               byte q = entity.content[start];
               for (end = start + 1; entity.content[end] != q; end++) {
                  if (entity.content[end] == '\\') {
                     end++;
                  }
               }
               add(entity, start + 1, end);
            }
         }
      }
   }
}