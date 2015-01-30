package io.github.repir.TestSet.Qrel;

import io.github.repir.tools.io.Datafile;
import io.github.repir.tools.io.struct.StructuredTextCSV;
import io.github.repir.tools.lib.Log;
import java.util.HashMap;

public class QrelReaderTREC extends StructuredTextCSV implements QrelReader {

   public static Log log = new Log(QrelReaderTREC.class);
   public IntField topic = this.addInt("topic", "", "\\s+", "", " ");
   public IntField userid = this.addInt("system", "", "\\s+", "", " ");
   public StringField docid = this.addString("docid", "", "\\s+", "", " ");
   public IntField relevance = this.addInt("relevance", "", "($|\\s+)", "", "");

   public QrelReaderTREC(Datafile df) {
      super(df);
   }

   @Override
   public HashMap<Integer, QRel> getQrels() {
      HashMap<Integer, QRel> list = new HashMap();
      QRel currenttopic = null;
      this.openRead();
      while (this.nextRecord()) {
         if (currenttopic == null || topic.get() != currenttopic.id) {
            currenttopic = list.get(topic.get());
            if (currenttopic == null) {
               currenttopic = new QRel(topic.get());
               list.put(topic.get(), currenttopic);
            }
         }
         currenttopic.relevance.put(docid.get(), relevance.get());
         currenttopic.iprob.put(docid.get(), 1.0); // for 4 column files, set inclusion prob=1
      }
      return list;
   }
}
