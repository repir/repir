package io.github.repir.TestSet.Topic;

import io.github.repir.Repository.Repository;
import io.github.htools.io.Datafile;
import io.github.htools.io.struct.StructuredTextXML;
import io.github.htools.lib.Log;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Topic reader for TREC Web Track 2010-2013 ad-hoc topics.
 *
 * @author jer
 */
public class TopicReaderSessionTrack extends StructuredTextXML implements TopicReader {

   public static Log log = new Log(TopicReaderSessionTrack.class);
   public Repository repository;
   public FolderNode session = getRoot();
   public IntField sessionnum = this.addInt(session, "num");
   public StringField sessionstarttime = this.addString(session, "starttime");
   public FolderNode topic = this.addNode(session, "topic");
   public StringField title = this.addString(topic, "title");
   public StringField description = this.addString(topic, "desc");
   public StringField narrative = this.addString(topic, "narr");
   public FolderNode interaction = this.addNode(session, "interaction");
   public IntField interactionnum = this.addInt(interaction, "num");
   public StringField interactionstarttime = this.addString(interaction, "starttime");
   public StringField interactionquery = this.addString(interaction, "query");
   public FolderNode results = this.addNode(interaction, "results");
   public FolderNode result = this.addNode(results, "result");
   public IntField resultrank = this.addInt(result, "rank");
   public StringField url = this.addString(result, "url");
   public StringField clueweb09id = this.addString(result, "clueweb09id");
   public StringField resulttitle = this.addString(result, "title");
   public StringField snippet = this.addString(result, "snippet");
   public FolderNode clicked = this.addNode(interaction, "clicked");
   public FolderNode click = this.addNode(clicked, "click");
   public IntField clicknum = this.addInt(click, "num");
   public StringField clickstarttime = this.addString(click, "starttime");
   public StringField clickendtime = this.addString(click, "endtime");
   public IntField clickrank = this.addInt(click, "rank");
   public FolderNode currentquery = this.addNode(session, "currentquery");
   public StringField currenttime = this.addString(currentquery, "starttime");
   public StringField query = this.addString(currentquery, "query");

   public TopicReaderSessionTrack(Repository repository) {
      super(repository.getTopicsFile());
      this.repository = repository;
      datafile.setBufferSize((int) datafile.getLength());
   }

   @Override
   public FolderNode createRoot() {
       return this.addNode(null, "session");
   }
   
   @Override
   public HashMap<Integer, TestSetTopic> getTopics() {
      TestSetTopicSession t;
      ArrayList<String> topictitles = new ArrayList<String>();
      HashMap<Integer, TestSetTopic> topics = new HashMap<Integer, TestSetTopic>();
      this.openRead();
      while (nextRecord()) {
         int topicnr = sessionnum.get();
         String topictitle = title.get();
         if (topictitles.contains(topictitle)) {
            topictitles.add(topictitle);
            t = new TestSetTopicSession(topicnr, topictitles.size(), null, query.get());
         } else {
            t = new TestSetTopicSession(topicnr, topictitles.indexOf(topictitle) + 1, null, query.get());
         }
         for (NodeValue i : interaction) {
            int lastseen = 0;
            HashSet<Integer> clickedranks = new HashSet<Integer>();
            if (i.get(clicked) != null) {
               for (NodeValue r : i.get(clicked).getListNode(click)) {
                  if (r.get(clickrank) != null) {
                     clickedranks.add((Integer) r.get(clickrank));
                     lastseen = Math.max(lastseen, (Integer) r.get(clickrank));
                  }
               }
            }
            t.priorqueries.add((String) i.get(interactionquery));
            if (i.get(results) != null && i.get(results).get(result) != null) {
               for (NodeValue r : i.get(results).getListNode(result)) {
                  if (clickedranks.contains(r.get(resultrank))) {
                     t.clickeddocuments.add((String) r.get(clueweb09id));
                  } else if (lastseen > (Integer) r.get(resultrank)) {
                     t.unclickeddocuments.add((String) r.get(clueweb09id));
                  } else {
                     t.unseen.add((String) r.get(clueweb09id));
                  }
               }
            }
         }
         topics.put(topicnr, t);
      }
      TopicReaderSessionMapping.assignTopics(repository, topics);
      return topics;
   }
}
