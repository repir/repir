package io.github.repir.Extractor.Tools;

import io.github.repir.tools.ByteRegex.ByteRegex;
import io.github.repir.tools.ByteRegex.ByteRegex.Pos;
import io.github.repir.Extractor.Entity;
import io.github.repir.Extractor.Extractor;
import io.github.repir.tools.Lib.Log;
import java.util.ArrayList;

/**
 * Remove HTML special codes that are written in between &;, such as &tilde;
 * <p/>
 * @author jeroen
 */
public class RemoveNonTextAmpersand extends ExtractorProcessor {

   public static Log log = new Log(RemoveNonTextAmpersand.class);
   ByteRegex regexnontext = new ByteRegex("\\&([^\\w]|(?<=[^\\c]))");
   ByteRegex regexother = new ByteRegex("\\&");
   ByteRegex combi = new ByteRegex( regexnontext, regexother );

   private RemoveNonTextAmpersand(Extractor extractor, String process) {
      super(extractor, process);
   }

   @Override
   public void process(Entity entity, Entity.SectionPos section, String attribute) {
      ArrayList<Pos> pos = combi.findAllOverlap(entity.content, section.open, section.close);
      for (Pos p : pos) {
         if ( p.pattern == 0 )
            entity.content[p.start] = 32;
         else
            entity.content[p.start] = 0;
      }
   }
}
