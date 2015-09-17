package io.github.repir.Repository;

import io.github.htools.extract.Content;
import io.github.htools.hadoop.io.archivereader.RecordKey;
import io.github.htools.hadoop.io.archivereader.RecordValue;
import java.io.IOException;
import org.apache.hadoop.mapreduce.Mapper.Context;

/**
 * A general interface that features must implement, for creation with RR's general Repository
 * builder apps.Repository.Create.
 */
public interface ReducibleFeature {
    
   public void writeMap(Context context, int feature, String docname, Content entity) throws IOException, InterruptedException;
    
   public void writeReduce(RecordKey key, Iterable<RecordValue> values);

   public void startReduce(int buffersize);
   
   public void finishReduce();
   
   public String entityAttribute();
   
   public RecordKey createMapOutputKey(int feature, String docname, Content entity);
}
