package io.github.repir.TestSet.Topic;

import io.github.repir.TestSet.Metric.TestSetTopic;
import io.github.repir.tools.ByteSearch.ByteRegex;
import io.github.repir.tools.Content.Datafile;
import io.github.repir.tools.Content.StructuredTextfileTagUnordered;
import io.github.repir.Repository.Repository;
import io.github.repir.tools.Lib.Log;
import java.util.HashMap;

/**
 * A reader for topic files used in TREC1-8 ad-hoc.
 * @author jer
 */
public class TopicReaderTREC extends StructuredTextfileTagUnordered implements TopicReader {

   public static Log log = new Log(TopicReaderTREC.class);
   public head head = new head();
   public number number = new number();
   public domain domain = new domain();
   public topic topic = new topic();
   public description description = new description();
   public summary summary = new summary();
   public narrative narrative = new narrative();
   public concepts concepts = new concepts();
   public factors factors = new factors();
   public definitions definitions = new definitions();

   public TopicReaderTREC(Datafile df) {
      super(df);
   }

   public TopicReaderTREC(Repository repository) {
      super(new Datafile(repository.getConfigurationString("testset.topics")));
   }

   public class head extends StringField {

      public head() {
         super("head");
         startfieldregex = new ByteRegex("<head\\s*>\\s*");
      }
   }

   public class number extends IntField {

      public number() {
         super("number");
         startfieldregex= new ByteRegex("<num\\s*>([^<>]*?[:])?\\s*");
      }
   }

   public class domain extends StringField {

      public domain() {
         super("domain");
         startfieldregex = new ByteRegex("<dom\\s*>([^<>]*?[:])?\\s*");
      }
   }

   public class topic extends StringField {

      public topic() {
         super("topic");
         startfieldregex = new ByteRegex("<title\\s*>([^<>]*?[:])?\\s*");
      }
   }

   public class description extends StringField {

      public description() {
         super("description");
         startfieldregex = new ByteRegex("<desc\\s*>([^<>]*?[:])?\\s*");
      }
   }

   public class summary extends StringField {

      public summary() {
         super("summary");
         startfieldregex = new ByteRegex("<smry\\s*>([^<>]*?[:])?\\s*");
      }
   }

   public class narrative extends StringField {

      public narrative() {
         super("narrative");
         startfieldregex = new ByteRegex("<narr\\s*>([^<>]*?[:])?\\s*");
      }
   }

   public class concepts extends StringField {

      public concepts() {
         super("concepts");
         startfieldregex = new ByteRegex("<con\\s*>([^<>]*?[:])?\\s*");
      }
   }

   public class factors extends StringField {

      public factors() {
         super("factors");
         startfieldregex = new ByteRegex("<fac\\s*>([^<>]*?[:])?\\s*");
         endfieldregex = new ByteRegex("</fac\\s*>\\s*|(?=<def)|(?=</top)");
      }
   }

   public class definitions extends StringField {

      public definitions() {
         super("definitions");
         startfieldregex = new ByteRegex("<def\\s*>([^<>]*?[:])?\\s*");
      }
   }

   @Override
   public ByteRegex createStartRecordRegex(Field f) {
      return new ByteRegex("<top\\s*>\\s*");
   }

   @Override
   public ByteRegex createEndRecordRegex(Field f) {
      return new ByteRegex("</top\\s*>\\s*");
   }

   @Override
   public ByteRegex createEndFieldRegex(Field f) {
      return new ByteRegex( "(?=<)" );
   }

   @Override
   public HashMap<Integer, TestSetTopic> getTopics() {
      HashMap<Integer, TestSetTopic> topics = new HashMap<Integer, TestSetTopic>();
      this.openRead();
      while (this.next()) {
         topics.put(this.number.value, 
                 new TestSetTopic(number.value, 
                          (domain.value == null)?"":domain.value.trim(), 
                           topic.value.trim()));
      }
      return topics;
   }
}
