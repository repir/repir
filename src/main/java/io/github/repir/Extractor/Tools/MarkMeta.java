package io.github.repir.Extractor.Tools;

import io.github.repir.tools.ByteRegex.ByteRegex;
import io.github.repir.tools.ByteRegex.ByteRegex.Pos;
import io.github.repir.Extractor.Entity;
import io.github.repir.Extractor.Extractor;
import io.github.repir.tools.Lib.Log;
import java.util.ArrayList;

/**
 * Marks <doctitle> </doctitle> sections.
 * <p/>
 * @author jbpvuurens
 */
public class MarkMeta extends SectionMarker {

   public static Log log = new Log(MarkMeta.class);
   public ByteRegex tagend = new ByteRegex("\\Q>"); // quotes safe end of tag

   public MarkMeta(Extractor extractor, String inputsection, String outputsection) {
      super(extractor, inputsection, outputsection);
   }

   @Override
   public ByteRegex getStartMarker() {
      return new ByteRegex("<meta\\s");
   }

   @Override
   public void process(Entity entity, int sectionstart, int sectionend, ArrayList<Pos> positions) {
      for (Pos start : positions) {
         Pos tagclose = tagend.find(entity.content, start.end, sectionend);
         if (tagclose.found()) {
            entity.addSectionPos(outputsection, start.start, start.start, tagclose.end, tagclose.end);
            //log.info("process %d %d %s", start.start, tagclose.end, new String( entity.content, start.start, tagclose.end - start.start));
         }
      }
   }
}
