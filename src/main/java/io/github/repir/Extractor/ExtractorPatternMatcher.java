package io.github.repir.Extractor;

import io.github.repir.tools.ByteRegex.ByteRegex;
import io.github.repir.tools.ByteRegex.ByteRegex.Pos;
import io.github.repir.Extractor.Tools.SectionMarker;
import io.github.repir.tools.Lib.Log;
import java.util.ArrayList;

public class ExtractorPatternMatcher {

   public static Log log = new Log(ExtractorPatternMatcher.class);
   Extractor extractor;
   String section;
   ArrayList<SectionMarker> markers = new ArrayList<SectionMarker>();
   ByteRegex patternmatcher;

   public ExtractorPatternMatcher(Extractor extractor, String section, ArrayList<SectionMarker> markers) {
      this.extractor = extractor;
      this.section = section;
      ArrayList<ByteRegex> regex = new ArrayList<ByteRegex>();
      this.markers = markers;
      for (SectionMarker p : markers) {
         regex.add(((SectionMarker) p).getStartMarker());
      }
      patternmatcher = new ByteRegex(regex.toArray(new ByteRegex[regex.size()]));
   }

   void processSectionMarkers(Entity entity, int sectionstart, int sectionend) {
      //log.info("processSectionMarkers %s %d %d", section, sectionstart, sectionend);
      ArrayList<Pos> pos = patternmatcher.findAll(entity.content, sectionstart, sectionend);
      for (int pattern = 0; pattern < markers.size(); pattern++) {
         ArrayList<Pos> positions = new ArrayList<Pos>();
         for (Pos start : pos) { // find all possible section starts
            if (pattern == start.pattern) {
               positions.add(start);
            }
         }
         //log.info("markers.process %s", positions);
         markers.get(pattern).process(entity, sectionstart, sectionend, positions);
      }
   }
}
