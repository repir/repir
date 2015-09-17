package io.github.htools.extract;

import io.github.htools.extract.Content;
import io.github.htools.extract.ExtractorConf;
import io.github.htools.lib.Log;
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
public class ExtractorTestSet extends ExtractorConf {

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
   protected void processSectionMarkers(Content content) {
      content.addSectionPos("rrtestset", content.getContent(), 0, 0, content.length(), content.length());
   }
}
