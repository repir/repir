package io.github.repir.Extractor.Tools;

import io.github.repir.EntityReader.Entity;
import io.github.repir.Extractor.Extractor;
import io.github.repir.tools.Lib.BoolTools;
import io.github.repir.tools.Lib.Log;

/**
 * Stores a literal section. To use, first mark the section with a
 * SectionMarker, then in the configuration assign this class to the section's
 * process, e.g. "+extractor.literaltitle=StoreLiteralSection" will store the
 * section marked as "literaltitle" as {@link EntityChannel} "literaltitle".
 * <p/>
 * The white spaces in the literal sections are transformed to single spaces,
 * and trimmed of the beginning and end.
 *
 * @author jer
 */
public class StoreLiteralSection extends ExtractorProcessor {

   public static Log log = new Log(StoreLiteralSection.class);
   public boolean whitespace[] = new boolean[256];

   public StoreLiteralSection(Extractor extractor, String process) {
      super(extractor, process);
      whitespace = BoolTools.createASCIIAccept(' ', '\n', '\t', '\r');
   }

   @Override
   public void process(Entity entity, Entity.Section section, String attribute) {
      for (; section.open < section.close && whitespace[entity.content[section.open] & 0xFF]; section.open++);
      for (; section.close > section.open && whitespace[entity.content[section.close - 1] & 0xFF]; section.close--);
      int realchars = section.close - section.open;
      for (int p = section.open; p < section.close; p++) {
         if (entity.content[p] == 0) {
            realchars--;
         } else {
            if (whitespace[entity.content[p] & 0xFF]) {
               entity.content[p] = 32;
               if (p > section.open && entity.content[p - 1] == 32) {
                  realchars--;
               }
            }
         }
      }
      if (section.close - section.open < 1) {
         entity.get(attribute).add("");
      } else if (realchars == section.close - section.open) {
         entity.get(attribute).add(new String(entity.content, section.open, section.close - section.open));
      } else {
         char c[] = new char[realchars];
         for (int cnr = 0, p = section.open; p < section.close; p++) {
            if (entity.content[p] != 0) {
               c[cnr++] = (char) (entity.content[p] & 0xFF);
               if (entity.content[p] == 32) {
                  for (; entity.content[p + 1] == 32; p++);
               }
            }
         }
         entity.get(attribute).add(new String(c));
      }
   }
}
