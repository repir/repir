package io.github.repir.TestSet.Topic;

import io.github.repir.tools.Buffer.BufferReaderWriter;
import io.github.repir.tools.Structure.StructuredTextCSV;
import io.github.repir.tools.Structure.StructuredTextXML;
import io.github.repir.tools.Lib.Log;

/**
 * Topic reader for TREC Web Track 2010-2013 ad-hoc topics.
 *
 * @author jer
 */
public class TopicReaderSessionMappingTest extends StructuredTextCSV {

   public static Log log = new Log(TopicReaderSessionMappingTest.class);
   public FolderNode session = this.createRoot();
   public IntField sessionid = this.addInt("sessionid");
   public IntField topicid = this.addInt("topicid");
   public IntField subtopic = this.addInt("subtopic");

   private TopicReaderSessionMappingTest() {
      super( new BufferReaderWriter(("1 1 3\n" +
                                    "2    2     0\n" +
                                    "2    2     1\n" +
                                    "2    2     2\n" +
                                    "2    2     3").getBytes()));
   }

   public static void main(String args[]) {
      TopicReaderSessionMappingTest m = new TopicReaderSessionMappingTest();
      m.openRead();
      while (m.nextRecord()) {
         log.info("query %d topic %d", m.sessionid.get(), m.topicid.get());
      }
      m.closeRead();
   }
}
