package io.github.repir.TestSet.Topic;

import io.github.repir.TestSet.Metric.TestSetTopic;
import io.github.repir.tools.Content.Datafile;
import io.github.repir.Repository.Repository;
import java.util.HashMap;

/**
 * Uses the description instead of the title of the topics.
 * @author jer
 */
public class TopicReaderTRECDescription extends TopicReaderTREC {

   public TopicReaderTRECDescription(Datafile df) {
      super(df);
   }

   public TopicReaderTRECDescription(Repository repository) {
      super(repository);
   }

   @Override
   public HashMap<Integer, TestSetTopic> getTopics() {
      HashMap<Integer, TestSetTopic> topics = new HashMap<Integer, TestSetTopic>();
      this.openRead();
      while (this.next()) {
         topics.put(this.number.value, new TestSetTopic(number.value, null, description.value.trim()));
      }
      return topics;
   }
}
