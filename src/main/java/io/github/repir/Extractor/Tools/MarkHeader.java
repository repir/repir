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
public class MarkHeader extends SectionMarker {

   public static Log log = new Log(MarkHeader.class);
   public ByteRegex endmarker = new ByteRegex("</header>");
   public ByteRegex ti = new ByteRegex("<TI>");
   public ByteRegex endti = new ByteRegex("</TI>");

   public MarkHeader(Extractor extractor, String inputsection, String outputsection) {
      super(extractor, inputsection, outputsection);
   }

   @Override
   public ByteRegex getStartMarker() {
      return new ByteRegex("<header>");
   }

   @Override
   public void process(Entity entity, int sectionstart, int sectionend, ArrayList<Pos> positions) {
      for (Pos start : positions) {
         Pos end = endmarker.find(entity.content, start.end, sectionend);
         if (end.found() && end.start > start.end) {
            Pos t = ti.find(entity.content, start.end, end.start);
            if (t.found()) {
               start.end = t.end;
               t = endti.find(entity.content, start.end, end.start);
               if (t.found()) {
                  end.start = t.start;
                  entity.addSectionPos(outputsection, start.start, start.end, end.start, end.end);
               }
            }
         }
      }
   }
}
