package io.github.repir.tools.MapReduce;

import io.github.repir.Repository.Repository;
import io.github.repir.tools.Content.BufferDelayedWriter;
import io.github.repir.tools.Content.BufferReaderWriter;
import io.github.repir.tools.Lib.Log;
import org.apache.hadoop.io.NullWritable;

/**
 * A custom implementation of Hadoop's InputSplit used by RetrieverMR. 
 * Each Split holds a set of Queries tasks that must all belong to the same partition. 
 * <p/>
 * @author jeroen
 */
public class NullInputSplit extends MRInputSplit<NullWritable, Integer> {

   public static Log log = new Log(NullInputSplit.class);

   public NullInputSplit() {
   }

   public NullInputSplit(Repository repository, int partition) {
      super(repository, partition);
   }

   @Override
   public NullWritable convert(Integer p) {
      return NullWritable.get();
   }

   @Override
   public void writeKey(BufferDelayedWriter out, Integer key) {
   }

   @Override
   public Integer readKey(BufferReaderWriter reader) {
      return null;
   }
}
