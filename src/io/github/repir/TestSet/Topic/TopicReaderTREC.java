package io.github.repir.TestSet.Topic;

import io.github.repir.Repository.Repository;
import io.github.repir.tools.Content.Datafile;
import io.github.repir.tools.Content.StructuredTextFile;
import io.github.repir.tools.Lib.Log;
import java.util.HashMap;

/**
 * A reader for topic files used in TREC1-8 ad-hoc.
 *
 * @author jer
 */
public class TopicReaderTREC extends StructuredTextFile implements TopicReader {

   public static Log log = new Log(TopicReaderTREC.class);
   public FolderNode root = this.addRoot("top", "<top>", "</top>");
   public IntField number = this.addInt(root, "number", "<num\\s*>([ \\c]+:)?", "(?=<)|$");
   public StringField domain = this.addString(root, "domain", "<dom\\s*>([ \\c]+:)?", "(?=<)|$");
   public StringField title = this.addString(root, "title", "<title\\s*>([ \\c]+:)?", "(?=<)|$");
   public StringField description = this.addString(root, "description", "<desc\\s*>([ \\c]+:)?", "(?=<)|$");
   public StringField summary = this.addString(root, "summary", "<smry\\s*>([ \\c]+:)?", "(?=<)|$");
   public StringField narrative = this.addString(root, "narrative", "<narr\\s*>([ \\c]+:)?", "(?=<)|$");

   public TopicReaderTREC(Datafile df) {
      super(df);
   }

   public TopicReaderTREC(Repository repository) {
      super(repository.getTopicsFile());
   }

   @Override
   public HashMap<Integer, TestSetTopic> getTopics() {
      HashMap<Integer, TestSetTopic> topics = new HashMap<Integer, TestSetTopic>();
      this.openRead();
      while (this.next()) {
         topics.put(this.number.get(),
                 new TestSetTopic(number.get(),
                         (domain.get() == null) ? "" : domain.get(),
                         title.get()));
      }
      return topics;
   }
}
