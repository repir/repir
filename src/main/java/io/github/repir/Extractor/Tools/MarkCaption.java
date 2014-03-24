package io.github.repir.Extractor.Tools;

import io.github.repir.tools.ByteSearch.ByteRegex;
import io.github.repir.tools.ByteSearch.ByteSearchPosition;
import io.github.repir.EntityReader.Entity;
import io.github.repir.Extractor.Extractor;
import io.github.repir.tools.ByteSearch.ByteSearch;
import io.github.repir.tools.Lib.Log;
import java.util.ArrayList;

/**
 * Marks <caption> </caption> sections in TREC collections, skipping captions
 * that contain only the message "photo: Associated Press".
 * <p/>
 * @author jbpvuurens
 */
public class MarkCaption extends SectionMarker {

   public static Log log = new Log(MarkCaption.class);
   public ByteSearch endmarker = ByteSearch.create("</caption>");
   public ByteSearch skip = ByteSearch.create("\\s*(photo[:;]\\s*)+Associated\\s+Press");

   public MarkCaption(Extractor extractor, String inputsection, String outputsection) {
      super(extractor, inputsection, outputsection);
   }

   @Override
   public ByteRegex getStartMarker() {
      return new ByteRegex("<caption>");
   }

   @Override
   public void process(Entity entity, int sectionstart, int sectionend, ByteSearchPosition position) {
      int endskip = skip.matchEnd(entity.content, position.end, sectionend);
      if (endskip > -1) {
         position.end = endskip;
      }
      ByteSearchPosition end = endmarker.findPos(entity.content, position.end, sectionend);
      if (end.found() && end.start > position.end) {
         entity.addSectionPos(outputsection, position.start, position.end, end.start, end.end);
      }
   }
}
