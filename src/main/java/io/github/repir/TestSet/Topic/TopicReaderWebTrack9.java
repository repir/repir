package io.github.repir.TestSet.Topic;

import io.github.repir.tools.Content.Datafile;
import io.github.repir.tools.Content.StructuredTextfileCSV;
import io.github.repir.tools.Lib.Log;
import java.util.HashMap;

/**
 * Topic reader for TREC Web Track 2009 ad-hoc set.
 * @author jer
 */
public class TopicReaderWebTrack9 extends TopicReaderWebTrack implements TopicReader {

   public static Log log = new Log(TopicReaderWebTrack9.class);

   public String createStartRecord(Field f) {
      return "wt09\\-";
   }

   public TopicReaderWebTrack9(Datafile df) {
      super(df);
   }
}
