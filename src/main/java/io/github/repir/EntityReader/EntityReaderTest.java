package io.github.repir.EntityReader; 

import io.github.repir.EntityReader.MapReduce.EntityWritable;
import io.github.repir.tools.Extractor.EntityChannel;
import io.github.repir.tools.Extractor.Extractor;
import io.github.repir.tools.Content.HDFSDir;
import io.github.repir.tools.Content.HDFSIn;
import io.github.repir.tools.Lib.Log;
import io.github.repir.MapReduceTools.RRConfiguration;
import java.io.IOException;
import java.util.Map;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;

/**
 *
 * @author Jeroen Vuurens
 */
public class EntityReaderTest {
  public static Log log = new Log( EntityReaderTest.class ); 

   public static void main(String[] args) throws IOException, InterruptedException {
      RRConfiguration conf = new RRConfiguration(args, "source");
      Path p = new Path(conf.get("source"));
      long length = HDFSIn.getLength(HDFSDir.getFS(conf), p);
      String[] locations = HDFSDir.getLocations(HDFSDir.getFS(conf), conf.get("source"), 0);
      FileSplit fs = new FileSplit(p, 0, length, locations);
      EntityReader er = new EntityReaderTrec();
      Extractor extractor = new Extractor(conf);
      er.initialize(fs, conf);
      er.nextKeyValue();
      EntityWritable ew = er.getCurrentValue();
      extractor.process(ew.entity);
      for (Map.Entry<String, EntityChannel> c : ew.entity.entrySet()) {
         log.printf("%s %s\n", c.getKey(), c.getValue().getContentStr());
      }
   }

}
