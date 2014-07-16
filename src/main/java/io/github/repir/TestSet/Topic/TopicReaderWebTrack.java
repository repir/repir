package io.github.repir.TestSet.Topic;

import io.github.repir.Repository.Repository;
import io.github.repir.tools.Content.Datafile;
import io.github.repir.tools.Structure.StructuredTextCSV;
import io.github.repir.tools.Structure.StructuredTextFile;
import io.github.repir.tools.Lib.Log;
import java.util.HashMap;

/**
 * Topic reader for TREC Web Track 2010-2013 ad-hoc topics.
 * @author jer
 */
public class TopicReaderWebTrack extends StructuredTextCSV implements TopicReader {

   public static Log log = new Log(TopicReaderWebTrack.class);
   public IntField number = this.addInt("number", "", ":", "", "");
   public StringField title = this.addString("title", "", "($|\n)", "", "");

   public TopicReaderWebTrack(Datafile df) {
      super(df);
   }

   public TopicReaderWebTrack(Repository repository) {
      super(repository.getTopicsFile());
   }

   @Override
   public HashMap<Integer, TestSetTopic> getTopics() {
      HashMap<Integer, TestSetTopic> topics = new HashMap<Integer, TestSetTopic>();
      this.openRead();
      while (this.next()) {
         topics.put(this.number.get(), new TestSetTopic( number.get(), null, title.get()));
      }
      return topics;
   }
}
