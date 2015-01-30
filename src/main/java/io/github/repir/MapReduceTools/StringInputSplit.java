package io.github.repir.MapReduceTools;

import io.github.repir.Repository.Repository;
import io.github.repir.tools.io.buffer.BufferDelayedWriter;
import io.github.repir.tools.io.buffer.BufferReaderWriter;
import io.github.repir.tools.lib.Log;
import org.apache.hadoop.io.Text;

/**
 * A custom implementation of Hadoop's InputSplit used by RetrieverMR. 
 * Each Split holds a set of Queries tasks that must all belong to the same partition. 
 * <p/>
 * @author jeroen
 */
public class StringInputSplit extends MRInputSplit<Text, String> {

   public static Log log = new Log(StringInputSplit.class); 

   public StringInputSplit() {
      super();
   }

   public StringInputSplit(Repository repository, int partition) {
      super( repository, partition );
   }

   @Override
   public Text convert(String p) {
      return new Text(p);
   }

   @Override
   public void writeKey(BufferDelayedWriter out, String key) {
      out.write(key);
   }

   @Override
   public String readKey(BufferReaderWriter reader) {
      return reader.readString();
   }
}
