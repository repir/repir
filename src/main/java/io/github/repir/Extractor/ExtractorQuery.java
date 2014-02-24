package io.github.repir.Extractor;

import io.github.repir.Repository.Repository;
import io.github.repir.tools.Lib.Log;

/**
 * For the extraction pipeline of query input, instead of 'all' the input is
 * marked as section 'repirquery', on which the 'repirquery' process is executed.
 * <p/>
 * @author jeroen
 */
public class ExtractorQuery extends Extractor {

   public static Log log = new Log(ExtractorQuery.class);

   public ExtractorQuery(Repository repository) {
      super(repository);
   }

   @Override
   public void init() {
      createProcess("repirquery");
      linkSectionToProcess("repirquery", "repirquery", "repirquery");
   }

   @Override
   void processSectionMarkers(Entity entity, int bufferpos, int bufferend) {
      entity.addSectionPos("repirquery", bufferpos, bufferpos, bufferend, bufferend);
   }
}
