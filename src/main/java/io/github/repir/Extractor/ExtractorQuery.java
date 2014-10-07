package io.github.repir.Extractor;

import io.github.repir.Repository.Repository;
import io.github.repir.tools.Lib.Log;
import org.apache.hadoop.conf.Configuration;

/**
 * Implements an extractor for RepIR {@link Query} strings. The extraction
 * {@link Entity.Section} and extraction process are called 'rrquery', which can
 * be configured similar to the Extractor used to build the Repository. As a
 * single section, queries are not pre-processed, and some processing has to be
 * handled with more care, requiring alternative implementations for queries,
 * such as handling dots (which can be parts of a Java-Class name).
 * <p/>
 * @author jeroen
 */
public class ExtractorQuery extends Extractor {

   public static Log log = new Log(ExtractorQuery.class);

   public ExtractorQuery(Configuration conf) {
      super(conf);
   }

   @Override
   void init() {
      createProcess("rrquery");
      addSectionProcess("rrquery", "rrquery", "rrquery");
   }

   @Override
   void processSectionMarkers(Entity entity, int bufferpos, int bufferend) {
      entity.addSectionPos("rrquery", bufferpos, bufferpos, bufferend, bufferend);
   }
}
