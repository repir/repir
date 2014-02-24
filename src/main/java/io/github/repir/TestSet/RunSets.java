package io.github.repir.TestSet;

import io.github.repir.Repository.DocLiteral;
import io.github.repir.Retriever.Document;
import io.github.repir.Retriever.Retriever;
import io.github.repir.Retriever.Query;
import io.github.repir.tools.Lib.Log;

public class RunSets {

   public static Log log = new Log(RunSets.class);
   TrecTopic topics;
   Retriever retriever;
   ResultFile out;
   String system;

   public RunSets(String system, TrecTopic t, Retriever retriever, ResultFile out) {
      this.topics = t;
      this.retriever = retriever;
      this.out = out;
      this.system = system;
   }

   public void run() {
      topics.openRead();
      topics.setBufferSize(10000);
      int count = 0;
      while (topics.next()) {
         //log.info("%d %s", topics.number.value, topics.topic.value);
         //retriever.queueQuery( topics.number.value, topics.topic.value.replaceAll("[()-]", " "), documentlimit);
         count++;
      }
      //log.info("queries %d %d", count, retriever.getQueue().size());
      topics.closeRead();
      Query queries[] = null;//= retriever.retrieveQueue();
      out.openWrite();
      for (Query q : queries) {
         DocLiteral collectionidfeature = retriever.repository.getCollectionIDFeature();
         for (Document d : q.queryresults) {
            out.topic.write(q.id);
            out.id.write(d.docid);
            out.segment.write(d.partition);
            out.docid.write(collectionidfeature.valueReported(d));
            out.score.write(d.score);
         }
      }
      out.closeRead();
   }
}
