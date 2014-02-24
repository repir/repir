package io.github.repir.TestSet;

import io.github.repir.tools.ByteRegex.ByteRegex;
import io.github.repir.tools.Content.Datafile;
import io.github.repir.tools.Content.RecordCSV;
import io.github.repir.tools.Lib.Log;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

public class TrecQrels extends RecordCSV {

   public static Log log = new Log(TrecQrels.class);
   public IntField topic = this.addInt("number");
   public IntField userid = this.addInt("userid");
   public StringField docid = this.addString("docid");
   public IntField relevance = this.addInt("relevance");

   public TrecQrels(Datafile df) {
      super(df);
   }

   @Override
   public ByteRegex createEndFieldRegex(Field f) {
      return  new ByteRegex(new ByteRegex( " +" ), createEndRecordRegex(f).lookAhead());
   }

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

   public static HashMap<Integer, HashMap<String, Integer>> merge(Collection<HashMap<Integer, HashMap<String, Integer>>> qrels) {
      HashMap<Integer, HashMap<String, Integer>> result = new HashMap<Integer, HashMap<String, Integer>>();
      for (HashMap<Integer, HashMap<String, Integer>> a : qrels) {
         result.putAll(a);
      }
      return result;
   }

   public static HashMap<Integer, HashMap<String, Integer>> readQrels(Collection<Datafile> df) {
      ArrayList<HashMap<Integer, HashMap<String, Integer>>> list = new ArrayList<HashMap<Integer, HashMap<String, Integer>>>();
      for (Datafile d : df) {
         list.add(new TrecQrels(d).getQrels());
      }
      return merge(list);
   }
}
