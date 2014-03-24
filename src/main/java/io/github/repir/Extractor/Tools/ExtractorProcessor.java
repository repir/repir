package io.github.repir.Extractor.Tools;

import io.github.repir.EntityReader.Entity;
import io.github.repir.EntityReader.Entity.Section;
import io.github.repir.EntityReader.EntityRemovedException;
import io.github.repir.Extractor.EntityChannel;
import io.github.repir.Extractor.Extractor;
import io.github.repir.tools.ByteSearch.ByteSearch;
import io.github.repir.tools.ByteSearch.ByteSearchString;
import io.github.repir.tools.Lib.ByteTools;


/**
 * Implementations can perform a conversion or extraction on an {@link Entity},
 * either modifying their raw byte contents, extracting an {@link EntityChannel}, or
 * modifying an {@link EntityChannel}.
 * @author jer
 */
public abstract class ExtractorProcessor {
   public Extractor extractor;
   public String process;
   public static ByteSearch endtag = ByteSearch.create(">");
   
   public ExtractorProcessor(Extractor extractor, String process) {
      this.extractor = extractor;
      this.process = process;
   }

   public abstract void process(Entity entity, Section section, String entityattribute) throws EntityRemovedException ;

   public int findQuoteSafeTagEnd(Entity entity, int pos, int end) {
      int p = endtag.findQuoteSafe(entity.content, ++pos, end);
      if ((p < 0) || (p - pos > 50)) {
         p = endtag.find(entity.content, pos, end);
      }
      return p;
   }
}
