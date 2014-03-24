package io.github.repir.TestSet.Topic;

import io.github.repir.TestSet.Metric.TestSetTopic;
import io.github.repir.tools.ByteSearch.ByteRegex;
import io.github.repir.tools.Content.Datafile;
import io.github.repir.tools.Content.StructuredTextfileCSV;
import io.github.repir.tools.Lib.Log;
import java.util.HashMap;

public class TopicReaderAnne extends StructuredTextfileCSV implements TopicReader {

   public static Log log = new Log(TopicReaderAnne.class);
   public IntField number = this.addInt("number");
   public StringField topic = this.addString("topic");

   public TopicReaderAnne(Datafile df) {
      super(df);
   }

   @Override
   public String createEndFieldTag(Field f) {
      if (f instanceof IntField)
         return " ";
      else
         return "\n";
   }

   @Override
   public HashMap<Integer, TestSetTopic> getTopics() {
      HashMap<Integer, TestSetTopic> topics = new HashMap<Integer, TestSetTopic>();
      this.openRead();
      while (this.next()) {
         topics.put(this.number.value, new TestSetTopic( number.value, null, topic.value.trim()));
      }
      return topics;
   }
}
