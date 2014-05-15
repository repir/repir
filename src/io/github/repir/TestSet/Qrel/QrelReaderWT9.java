package io.github.repir.TestSet.Qrel;

import io.github.repir.tools.Content.Datafile;
import io.github.repir.tools.Content.StructuredTextCSV;
import io.github.repir.tools.Lib.Log;
import java.util.HashMap;

public class QrelReaderWT9 extends StructuredTextCSV implements QrelReader {

   public static Log log = new Log(QrelReaderWT9.class);
   public FolderNode top = this.addRoot("qrel", "", "($|\n)", "", "\n");
   public IntField topic = this.addInt(top, "topic", "", "\\s+", "", " ");
   public StringField docid = this.addString(top, "docid", "", "\\s+", "", " ");
   public IntField relevance = this.addInt(top, "relevance", "", "\\s+", "", "");
   public IntField alg = this.addInt(top, "alg", "", "\\s+", "", " ");
   public DoubleField prob = this.addDouble(top, "prob", "", "($|\\s+)", "", " ");

   public QrelReaderWT9(Datafile df) {
      super(df);
   }

   @Override
   public HashMap<Integer, HashMap<String, Integer>> getQrels() {
      HashMap<Integer, HashMap<String, Integer>> list = new HashMap<Integer, HashMap<String, Integer>>();
      int currenttopic = -1;
      HashMap<String, Integer> qr = null;
      this.openRead();
      while (this.next()) {
         if (topic.get() != currenttopic) {
            currenttopic = topic.get();
            if (list.containsKey(currenttopic)) {
               qr = list.get(currenttopic);
            } else {
               qr = new HashMap<String, Integer>();
               list.put(currenttopic, qr);
            }
         }
         if (relevance.get() > 0)
            qr.put(docid.get(), 1);
      }
      return list;
   }
}
