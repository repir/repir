package io.github.repir.TestSet.Qrel;

import io.github.repir.tools.Content.Datafile;
import io.github.repir.tools.Structure.StructuredTextCSV;
import io.github.repir.tools.Lib.Log;
import java.util.HashMap;

public class QrelReaderWT9 extends StructuredTextCSV implements QrelReader {

   public static Log log = new Log(QrelReaderWT9.class);
   public IntField topic = this.addInt("topic", "", "\\s+", "", " ");
   public StringField docid = this.addString("docid", "", "\\s+", "", " ");
   public IntField relevance = this.addInt("relevance", "", "\\s+", "", "");
   public IntField method = this.addInt("method", "", "\\s+", "", " ");
   public DoubleField iprob = this.addDouble("prob", "", "($|\\s+)", "", " ");

   public QrelReaderWT9(Datafile df) {
      super(df);
   }

   @Override
   public HashMap<Integer, QRel> getQrels() {
      HashMap<Integer, QRel> list = new HashMap();
      QRel currenttopic = null;
      this.openRead();
      while (this.nextRecord()) {
         if (currenttopic == null || topic.get() != currenttopic.id) {
            currenttopic = new QRel(topic.get());
            list.put(currenttopic.id, currenttopic);
         }
         if (method.get() == 0 || method.get() == 2)
             this.iprob.set(1.0);
         currenttopic.no_sampled++;
         currenttopic.relevance.put(docid.get(), relevance.get());
         currenttopic.iprob.put(docid.get(), iprob.get());
      }
      return list;
   }
}
