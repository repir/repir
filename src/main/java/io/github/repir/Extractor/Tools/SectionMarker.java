package io.github.repir.Extractor.Tools;

import io.github.repir.tools.ByteRegex.ByteRegex;
import io.github.repir.tools.ByteRegex.ByteRegex.Pos;
import io.github.repir.Extractor.Entity;
import io.github.repir.Extractor.Extractor;
import java.util.ArrayList;

public abstract class SectionMarker {

   Extractor extractor;
   String inputsection;
   String outputsection;
   public ByteRegex startmarker;

   public SectionMarker(Extractor extractor, String inputsection, String outputsection) {
      this.extractor = extractor;
      this.inputsection = inputsection;
      this.outputsection = outputsection;
      startmarker = getStartMarker();
   }

   public String getInputSection() {
      return inputsection;
   }

   public abstract ByteRegex getStartMarker();

   public abstract void process(Entity entity, int sectionstart, int sectionend, ArrayList<Pos> positions);
}
