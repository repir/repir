package io.github.repir.Extractor.Tools;

import io.github.repir.tools.Lib.Log;
import io.github.repir.Extractor.Entity;
import io.github.repir.Extractor.EntityAttribute;
import io.github.repir.Extractor.Extractor;
import io.github.repir.tools.DataTypes.ByteArrayPos;

/**
 * Trims Literal tokens, by reducing inter-string multiple whitespace characters
 * to single spaces.
 * <p/>
 * @author jeroen
 */
public class TrimTokens extends ExtractorProcessor {

   private static Log log = new Log(TrimTokens.class);

   public TrimTokens(Extractor extractor, String process) {
      super(extractor, process);
   }

   @Override
   public void process(Entity entity, Entity.SectionPos section, String attributename) {
      EntityAttribute attribute = entity.get(attributename);
      for (int i = attribute.size()-1; i >= 0; i--) {
         String t = attribute.get(i);
         String trim = t.trim();
         attribute.set(i, trim);
      }
   }
}