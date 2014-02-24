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
public class ExtractHtmlTitle extends ExtractRestore {

   public static Log log = new Log(ExtractHtmlTitle.class);

   public ExtractHtmlTitle(Extractor extractor, String process) {
      super(extractor, process);
   }

   @Override
   public void process(Entity entity, SectionPos section, String attribute) {
      add(entity, section.open, section.close);
   }
}