package io.github.repir.Extractor;

import io.github.repir.Repository.Repository;
import io.github.repir.tools.Lib.Log;

/**
 * For the extraction pipeline of query input, instead of 'all' the input is
 * marked as section 'irefquery', on which the 'irefquery' process is executed.
 * <p/>
 * @author jeroen
 */
public class ExtractorTestSet extends Extractor {

   public static Log log = new Log(ExtractorTestSet.class);

   public ExtractorTestSet(Repository repository) {
      super(repository);
   }

   @Override
   public void init() {
      createProcess("ireftestset");
      linkSectionToProcess("ireftestset", "ireftestset", "ireftestset");
   }

   @Override
   void processSectionMarkers(Entity entity, int bufferpos, int bufferend) {
      entity.addSectionPos("ireftestset", bufferpos, bufferpos, bufferend, bufferend);
   }
}