package io.github.repir.TestSet.Topic;

import io.github.repir.Repository.Repository;
import io.github.repir.tools.io.Datafile;
import io.github.repir.tools.io.struct.StructuredTextFile;
import io.github.repir.tools.lib.Log;
import java.util.HashMap;

/**
 * A reader for topic files used in TREC1-8 ad-hoc.
 *
 * @author jer
 */
public class TopicReaderTREC extends StructuredTextFile implements TopicReader {

   public static Log log = new Log(TopicReaderTREC.class);
   public IntField number = this.addInt("number", "<num\\s*>([ \\c]+:)?", "(?=<)|$", "", "");
   public StringField domain = this.addString("domain", "<dom\\s*>([ \\c]+:)?", "(?=<)|$", "", "");
   public StringField title = this.addString("title", "<title\\s*>([ \\c]+:)?", "(?=<)|$", "", "");
   public StringField description = this.addString("description", "<desc\\s*>([ \\c]+:)?", "(?=<)|$", "", "");
   public StringField summary = this.addString("summary", "<smry\\s*>([ \\c]+:)?", "(?=<)|$", "", "");
   public StringField narrative = this.addString("narrative", "<narr\\s*>([ \\c]+:)?", "(?=<)|$", "", "");

   public TopicReaderTREC(Datafile df) {
      super(df); 
   }

   public TopicReaderTREC(Repository repository) {
      super(repository.getTopicsFile());
   }
   
   public FolderNode createRoot() {
       return addNode(null, "top", "<top>", "</top>", "<top>", "</top>");
   }

   @Override
   public HashMap<Integer, TestSetTopic> getTopics() {
      HashMap<Integer, TestSetTopic> topics = new HashMap<Integer, TestSetTopic>();
      this.openRead();
      while (this.nextRecord()) {
         topics.put(this.number.get(),
                 new TestSetTopic(number.get(),
                         (domain.get() == null) ? "" : domain.get(),
                         title.get()));
      }
      return topics;
   }
}
