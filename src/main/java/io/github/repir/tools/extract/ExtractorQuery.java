package io.github.repir.tools.extract;

import io.github.repir.tools.extract.Content;
import io.github.repir.tools.extract.ExtractorConf;
import io.github.repir.tools.lib.Log;
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
public class ExtractorQuery extends ExtractorConf {

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
   protected void processSectionMarkers(Content entity, int bufferpos, int bufferend) {
      entity.addSectionPos("rrquery", entity.content, bufferpos, bufferpos, bufferend, bufferend);
   }
}
