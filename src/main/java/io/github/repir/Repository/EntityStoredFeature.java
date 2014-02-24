package io.github.repir.Repository;

import java.io.EOFException;
import java.util.HashMap;
import io.github.repir.tools.Content.RecordIdentity;
import io.github.repir.Extractor.Entity;
import io.github.repir.EntityReader.TermEntityKey;
import io.github.repir.EntityReader.TermEntityValue;
import io.github.repir.Retriever.Document;
import io.github.repir.tools.Content.BufferReaderWriter;

public abstract class EntityStoredFeature<F extends RecordIdentity, C> extends StoredReportableFeature<F, C> implements ReducableFeature {

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
