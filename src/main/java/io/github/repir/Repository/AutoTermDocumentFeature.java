package io.github.repir.Repository;

import io.github.htools.extract.Content;
import io.github.htools.hadoop.io.archivereader.RecordKey;
import io.github.htools.hadoop.io.archivereader.RecordValue;
import io.github.htools.extract.ExtractChannel;
import io.github.htools.io.struct.StructuredFileIntID;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import org.apache.hadoop.mapreduce.Mapper.Context;

/**
 * A AutoTermDocumentFeature that is configured for a Repository is automatically
 * generated by the standard Repository builder in apps.Repository.Build. Other
 * TermDocumentFeatures have to be build manually.
 * @author jeroen
 * @param <F>
 * @param <C> 
 */
public abstract class AutoTermDocumentFeature<F extends StructuredFileIntID, C> extends TermDocumentFeature<F, C> {

   HashMap<String, Integer> docs;
   int reducetermid;

   public AutoTermDocumentFeature(Repository repository, String field) {
      super(repository, field);
   }

   public void setDocs(HashMap<String, Integer> docs) {
      this.docs = docs;
      reducetermid = 0;
   }
   
   RecordKey outkey;
   RecordValue outvalue = new RecordValue();

   public void writeMap(Context context, int partition, int feature, String docname, Content entity) throws IOException, InterruptedException {
       HashMap<Integer, ArrayList<Integer>> tokens = getTokens(entity);
       for (Entry<Integer, ArrayList<Integer>> entry : tokens.entrySet()) {
          outkey = RecordKey.createTermDocKey(partition, feature, entry.getKey(), docname);
          setMapOutputValue(outvalue, docname, entry.getValue());
          context.write(outkey, outvalue);
       }
   }
   
   public abstract void reduceInput(RecordKey key, Iterable<RecordValue> values);
   
   public HashMap<Integer, ArrayList<Integer>> getTokens(Content doc) {
      HashMap<Integer, ArrayList<Integer>> list = new HashMap<Integer, ArrayList<Integer>>();
      ArrayList<Integer> l;
      int pos = 0;
      
      ExtractChannel attr = doc.get(entityAttribute());
      if (attr.tokenized == null) {
         attr.tokenized = repository.tokenize(attr);
      }
      for (int token : attr.tokenized) {
         l = list.get(token);
         if (l == null) {
            l = new ArrayList<Integer>();
            list.put(token, l);
         }
         l.add(pos++);
      }
      return list;
   }

   @Override
   public void openRead() {
      super.openRead();
      //log.info("openRead termid %d", term.getID());
      if (term.exists()) {
         find(term.getID());
         docid = -1;
      }
   }

   public void startReduce(int partition, int buffersize) {
      setPartition(partition);
      getFile().setBufferSize(buffersize);
      getFile().openWrite();
   }

   public void finishReduce() {
      getFile().closeWrite();
   }

   abstract public void setMapOutputValue(RecordValue writer, String docname, ArrayList<Integer> pos);

}
