package io.github.repir.TestSet.Topic;

import io.github.repir.Repository.Repository;
import io.github.repir.tools.Content.Datafile;
import io.github.repir.tools.Structure.StructuredTextCSV;
import io.github.repir.tools.Structure.StructuredTextXML;
import io.github.repir.tools.Lib.Log;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Topic reader for TREC Web Track 2010-2013 ad-hoc topics.
 *
 * @author jer
 */
public class TopicReaderSessionMapping extends StructuredTextCSV {

   public static Log log = new Log(TopicReaderSessionMapping.class);
   public FolderNode session = this.createRoot();
   public IntField sessionid = this.addInt("sessionid");
   public IntField topicid = this.addInt("topicid");
   public IntField subtopic = this.addInt("subtopic");

   private TopicReaderSessionMapping(Repository repository) {
      super(new Datafile(repository.configuredString("testset.mappings")));
      datafile.setBufferSize((int) datafile.getLength());
   }

   public static void assignTopics(Repository repository, HashMap<Integer, TestSetTopic> topics) {
      TopicReaderSessionMapping m = new TopicReaderSessionMapping(repository);
      m.openRead();
      while (m.next()) {
         TestSetTopic get = topics.get(m.sessionid.get());
         get.qrelid = m.topicid.get();
      }
      m.closeRead();
   }
}
