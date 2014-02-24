package io.github.repir.TestSet;

import io.github.repir.tools.Content.Datafile;
import io.github.repir.Repository.Repository;
import java.util.HashMap;

public class TrecTopicDescription extends TrecTopic {

   public TrecTopicDescription(Datafile df) {
      super(df);
   }

   public TrecTopicDescription(Repository repository) {
      super(repository);
   }

   @Override
   public HashMap<Integer, Topic> getTopics() {
      HashMap<Integer, Topic> topics = new HashMap<Integer, Topic>();
      this.openRead();
      while (this.next()) {
         topics.put(this.number.value, new Topic(number.value, null, description.value.trim()));
      }
      return topics;
   }
}
