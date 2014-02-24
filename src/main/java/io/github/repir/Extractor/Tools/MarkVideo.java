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
public class MarkVideo extends SectionMarker {

   public static Log log = new Log(MarkVideo.class);
   public ByteRegex endmarker = new ByteRegex("</video\\s*>");
   public ByteRegex tagend = new ByteRegex("\\Q>"); // quotes safe end of tag

   public MarkVideo(Extractor extractor, String inputsection, String outputsection) {
      super(extractor, inputsection, outputsection);
   }

   @Override
   public ByteRegex getStartMarker() {
      return new ByteRegex("<video");
   }

   @Override
   public void process(Entity entity, int sectionstart, int sectionend, ArrayList<Pos> positions) {
      for (Pos start : positions) {
         Pos tagclose = tagend.find(entity.content, start.end, sectionend);
         if (tagclose.found()) {
            Pos end = endmarker.find(entity.content, start.end, sectionend);
            if (end.start > start.end) {
               entity.addSectionPos(outputsection, start.start, tagclose.end, end.start, end.end);
            }
         }
      }
   }
}
