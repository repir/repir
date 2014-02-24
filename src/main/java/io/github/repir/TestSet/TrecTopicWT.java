package io.github.repir.TestSet;

import io.github.repir.tools.ByteRegex.ByteRegex;
import io.github.repir.tools.Content.Datafile;
import io.github.repir.tools.Content.RecordCSV;
import io.github.repir.tools.Lib.Log;
import java.util.HashMap;

public class TrecTopicWT extends RecordCSV implements TrecTopicReader {

   public static Log log = new Log(TrecTopicWT.class);
   public IntField number = this.addInt("number");
   public StringField topic = this.addString("topic");

   public TrecTopicWT(Datafile df) {
      super(df);
   }

   @Override
   public String createEndFieldTag(Field f) {
      return ":";
   }

   @Override
   public HashMap<Integer, Topic> getTopics() {
      HashMap<Integer, Topic> topics = new HashMap<Integer, Topic>();
      this.openRead();
      while (this.next()) {
         topics.put(this.number.value, new Topic( number.value, null, topic.value.trim()));
      }
      return topics;
   }
}
