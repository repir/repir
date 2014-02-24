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
public class MarkCaption extends SectionMarker {

   public static Log log = new Log(MarkCaption.class);
   public ByteRegex endmarker = new ByteRegex("</caption>");
   public ByteRegex skip = new ByteRegex("\\s*(photo[:;]\\s*)+Associated\\s+Press");

   public MarkCaption(Extractor extractor, String inputsection, String outputsection) {
      super(extractor, inputsection, outputsection);
   }

   @Override
   public ByteRegex getStartMarker() {
      return new ByteRegex("<caption>");
   }

   @Override
   public void process(Entity entity, int sectionstart, int sectionend, ArrayList<Pos> positions) {
      for (Pos start : positions) {
         Pos s = skip.findFirst(entity.content, start.end, sectionend);
         if (s.found())
            start.end = s.end;
         Pos end = endmarker.find(entity.content, start.end, sectionend);
         if (end.found() && end.start > start.end) {
            entity.addSectionPos(outputsection, start.start, start.end, end.start, end.end);
         }
      }
   }
}
