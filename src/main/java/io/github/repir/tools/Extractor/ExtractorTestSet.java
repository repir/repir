package io.github.repir.tools.Extractor;

import io.github.repir.tools.Lib.Log;
import org.apache.hadoop.conf.Configuration;

/**
 * Implements an extractor for RepIR {@link Query} strings that occur within
 * the source for a {@link TestSet}. The extraction
 * {@link Entity.Section} and extraction process are called 'rrtestset', which can
 * be configured similar to the Extractor used to build the Repository. Typically,
 * for test sets, characters that are used with a different meaning than in the
 * RR Query syntax are removed (e.g. hyphens, brackets).
 * <p/>
 * @author jeroen
 */
public class ExtractorTestSet extends Extractor {

   public static Log log = new Log(ExtractorTestSet.class);

   public ExtractorTestSet(Configuration conf) {
      super(conf);
   }

   @Override
   public void init() {
      createProcess("rrtestset");
      addSectionProcess("rrtestset", "rrtestset", "rrtestset");
   }

   @Override
   void processSectionMarkers(Entity entity, int bufferpos, int bufferend) {
      entity.addSectionPos("rrtestset", bufferpos, bufferpos, bufferend, bufferend);
   }
}
