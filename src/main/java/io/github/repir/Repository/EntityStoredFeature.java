package io.github.repir.Repository;

import java.util.HashMap;
import io.github.htools.io.struct.StructuredFileIntID;
import io.github.htools.extract.Content;
import io.github.htools.hadoop.io.archivereader.RecordKey;
import io.github.htools.hadoop.io.archivereader.RecordValue;
import io.github.repir.Retriever.Document;
import java.io.IOException;
import org.apache.hadoop.mapreduce.Mapper;

/**
 * An abstract feature that can store a value per Document in the Repository.
 * This value can be accessed with an internal DocumentID passed through
 * {@link EntityStoredFeature#read(io.github.repir.Retriever.Document) }
 * @author jer
 * @param <F> a StructuredFileIntID file to store it's values, allowing 
 * the stored Record to be accessed through an internal integer ID
 * @param <C> The datatype stored
 */
public abstract class EntityStoredFeature<F extends StructuredFileIntID, C> extends StoredReportableFeature<F, C> implements ReduciblePartitionedFeature {

   public HashMap<Integer, C> cache;
   
   public EntityStoredFeature(Repository repository, String field) {
      super(repository, field);
   }

   @Override
   public RecordKey createMapOutputKey(int partition, int feature, String docname, Content entity) {
      RecordKey t = RecordKey.createTermDocKey(partition, feature, 0, docname);
      t.type = RecordKey.Type.ENTITYFEATURE;
      return t;
   }
   
   public String extract(Content entity) {
      return entity.get(entityAttribute()).getContentStr();
   }

   abstract public void setMapOutputValue(RecordValue writer, Content doc);

   RecordKey outkey;
   RecordValue outvalue = new RecordValue();
    @Override
    public void writeMap(Mapper.Context context, int partition, int feature, String docname, Content entity) throws IOException, InterruptedException {
          outkey = createMapOutputKey(partition, feature, docname, entity);
          setMapOutputValue(outvalue, entity);
          context.write(outkey, outvalue);
    }
    
   @Override
   public void finishReduce() {
      getFile().closeWrite();
   }

   @Override
   public void startReduce(int partition, int buffersize) {
      setPartition(partition);
      getFile().setBufferSize(buffersize);
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
   public long getLength() {
      return getFile().getLength();
   }
}
