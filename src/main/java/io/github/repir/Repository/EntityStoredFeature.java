package io.github.repir.Repository;

import java.util.HashMap;
import io.github.repir.tools.Content.StructuredFileIntID;
import io.github.repir.EntityReader.Entity;
import io.github.repir.EntityReader.MapReduce.TermEntityKey;
import io.github.repir.EntityReader.MapReduce.TermEntityValue;
import io.github.repir.Retriever.Document;

/**
 * An abstract feature that can store a value per Document in the Repository.
 * This value can be accessed with an internal DocumentID passed through
 * {@link EntityStoredFeature#read(io.github.repir.Retriever.Document) }
 * @author jer
 * @param <F> a StructuredFileIntID file to store it's values, allowing 
 * the stored Record to be accessed through an internal integer ID
 * @param <C> The datatype stored
 */
public abstract class EntityStoredFeature<F extends StructuredFileIntID, C> extends StoredReportableFeature<F, C> implements ReducibleFeature {

   public HashMap<Integer, C> cache;
   
   public EntityStoredFeature(Repository repository, String field) {
      super(repository, field);
   }

   public void writereduce(TermEntityKey key, Iterable<TermEntityValue> values) {
      reduceInput(key, values);
   }

   abstract public void mapOutput(TermEntityValue writer, Entity doc);

   @Override
   public void finishReduce() {
      getFile().closeWrite();
   }

   @Override
   public void startReduce(int partition) {
      setPartition(partition);
      getFile().openWrite();
   }

   public abstract C getValue();
   
   public abstract void setValue(C value);

   public abstract void write(C value);
   
   @Override
   public void setPartition(int partition) {
      if (this.partition != partition) {
         super.setPartition(partition);
         cache = null;
      }
   }
   
   public void cacheResults() {
      cache = new HashMap<Integer, C>();  
   }
   
   @Override
   public void read(Document d) {
      try {
         if (partition != d.partition) {
            setPartition(d.partition);
            openRead();
            if (cache != null) 
               cacheResults();
         }
         if (cache == null)
            super.read(d.docid);
         else {
            C value = cache.get(d.docid);
            if (value == null) {
               super.read(d.docid);
               cache.put(d.docid, getValue());
            } else 
               setValue(value);
         }
      } catch (Exception ex) {
         log.exception(ex, "Could not read value for document %d", d.docid);
      }
   }
   
   @Override
   public long getBytesSize() {
      return getFile().getFilesize();
   }
}
