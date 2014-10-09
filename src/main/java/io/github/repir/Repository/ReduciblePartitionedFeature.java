package io.github.repir.Repository;

import io.github.repir.tools.Extractor.Entity;
import io.github.repir.EntityReader.MapReduce.TermEntityKey;
import io.github.repir.EntityReader.MapReduce.TermEntityValue;
import java.io.IOException;
import org.apache.hadoop.mapreduce.Mapper.Context;

/**
 * A general interface that features must implement, for creation with RR's general Repository
 * builder apps.Repository.Create.
 */
public interface ReduciblePartitionedFeature {
    
   public void writeMap(Context context, int partition, int feature, String docname, Entity entity) throws IOException, InterruptedException;
    
   public void writeReduce(TermEntityKey key, Iterable<TermEntityValue> values);

   public void startReduce(int partition, int buffersize);
   
   public void finishReduce();
   
   public String entityAttribute();
   
   public TermEntityKey createMapOutputKey(int partition, int feature, String docname, Entity entity);
}
