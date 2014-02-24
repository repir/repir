package io.github.repir.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import io.github.repir.EntityReader.TermEntityKey;
import io.github.repir.EntityReader.TermEntityValue;
import io.github.repir.Extractor.Entity;
import io.github.repir.tools.Content.RecordIdentity;

/**
 * A stored feature that uses a term-document structure similar to a textbook inverted index. This 
 * data structure is best used for sparse data that is to be accessed by term, which gives an ordered
 * list of the documents in which the term appears. The base class can be extended to define the
 * exact data that needs to be stored, such as the term frequency or the list of positions of the
 * term in the document. 
 * @author jeroen
 * @param <F>
 * @param <C> 
 */
public abstract class AutoTermDocumentFeature<F extends RecordIdentity, C> extends TermDocumentFeature<F, C> implements ReducableFeature {

   HashMap<String, Integer> docs;

   public AutoTermDocumentFeature(Repository repository, String field) {
      super(repository, field);
   }

   public void setDocs(HashMap<String, Integer> docs) {
      this.docs = docs;
      termid = 0;
   }

   public void writereduce(TermEntityKey key, Iterable<TermEntityValue> values) {
      reduceInput(key, values);
   }

   @Override
   public void openRead() {
      super.openRead();
      if (termid >= 0) {
         find(termid);
         docid = -1;
      }
   }

   @Override
   public void startReduce(int partition) {
      setPartition(partition);
      getFile().openWrite();
   }

   @Override
   public void finishReduce() {
      getFile().closeWrite();
   }

   abstract public void writeMapOutput(TermEntityValue writer, Entity doc, ArrayList<Integer> pos);

}
