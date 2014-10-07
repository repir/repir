package io.github.repir.TestSet.Topic;

import io.github.repir.Repository.Repository;
import io.github.repir.tools.Content.Datafile;
import io.github.repir.tools.Structure.StructuredTextCSV;
import io.github.repir.tools.Lib.Log;
import java.util.HashMap;

/**
 * Topic reader for TREC Web Track 2009 ad-hoc set.
 * @author jer
 */
public class TopicReaderWebTrack9 extends StructuredTextCSV implements TopicReader {

   public static Log log = new Log(TopicReaderWebTrack9.class);
   public IntField number = this.addInt("number", "wt09-", ":", "", "");
   public StringField title = this.addString("title", "", "($|\n)", "", "");

   public TopicReaderWebTrack9(Datafile df) {
      super(df);
   }

   public TopicReaderWebTrack9(Repository repository) {
      super(repository.getTopicsFile());
   }

   @Override
   public HashMap<Integer, TestSetTopic> getTopics() {
      HashMap<Integer, TestSetTopic> topics = new HashMap<Integer, TestSetTopic>();
      this.openRead();
      while (this.nextRecord()) {
         topics.put(this.number.get(), new TestSetTopic( number.get(), null, title.get()));
      }
      return topics;
   }
}
