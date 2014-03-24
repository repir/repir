package io.github.repir.TestSet.Qrel;

import io.github.repir.tools.ByteSearch.ByteRegex;
import io.github.repir.tools.Content.Datafile;
import io.github.repir.tools.Content.StructuredTextfileCSV;
import io.github.repir.tools.Lib.Log;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

public class QrelReaderTREC extends StructuredTextfileCSV implements QrelReader {

   public static Log log = new Log(QrelReaderTREC.class);
   public IntField topic = this.addInt("number");
   public IntField userid = this.addInt("userid");
   public StringField docid = this.addString("docid");
   public IntField relevance = this.addInt("relevance");

   public QrelReaderTREC(Datafile df) {
      super(df);
   }

   @Override
   public ByteRegex createEndFieldRegex(Field f) {
      return  new ByteRegex(new ByteRegex( " +" ), createEndRecordRegex(f).lookAhead());
   }

   @Override
   public HashMap<Integer, HashMap<String, Integer>> getQrels() {
      HashMap<Integer, HashMap<String, Integer>> list = new HashMap<Integer, HashMap<String, Integer>>();
      int currenttopic = -1;
      HashMap<String, Integer> qr = null;
      this.openRead();
      while (this.next()) {
         if (topic.value != currenttopic) {
            currenttopic = topic.value;
            if (list.containsKey(currenttopic)) {
               qr = list.get(currenttopic);
            } else {
               qr = new HashMap<String, Integer>();
               list.put(currenttopic, qr);
            }
         }
         qr.put(docid.value, relevance.value > 0 ? 1 : 0);
      }
      return list;
   }
}
