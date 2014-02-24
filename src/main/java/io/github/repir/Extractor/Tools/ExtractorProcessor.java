package io.github.repir.Extractor.Tools;

import io.github.repir.Extractor.Entity;
import io.github.repir.Extractor.Entity.SectionPos;
import io.github.repir.Extractor.EntityAttribute;
import io.github.repir.Extractor.Extractor;

public abstract class ExtractorProcessor {

   public ExtractorProcessor(Extractor extractor, String process) {
   }

   public abstract void process(Entity entity, SectionPos section, String entityattribute);
}
