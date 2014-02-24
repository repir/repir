package io.github.repir.Extractor.Tools;

import io.github.repir.Extractor.Entity;
import io.github.repir.Extractor.EntityAttribute;
import io.github.repir.Extractor.Extractor;
import io.github.repir.tools.DataTypes.ByteArrayPos;
import io.github.repir.tools.Lib.Log;

public class StoreLiteralSection extends ExtractorProcessor {

   public static Log log = new Log(StoreLiteralSection.class);

   public StoreLiteralSection(Extractor extractor, String process) {
      super(extractor, process);
   }

   @Override
   public void process(Entity entity, Entity.SectionPos section, String attribute) {
      byte b[] = new byte[section.close - section.open];
      System.arraycopy(entity.content, section.open, b, 0, b.length);
      entity.get(attribute).add(new String( b, 0, b.length ));
   }
}
