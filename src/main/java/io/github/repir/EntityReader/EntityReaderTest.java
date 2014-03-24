package io.github.repir.EntityReader; 

import io.github.repir.EntityReader.MapReduce.EntityWritable;
import io.github.repir.Extractor.EntityChannel;
import io.github.repir.Extractor.Extractor;
import io.github.repir.Repository.Repository;
import io.github.repir.tools.Content.HDFSIn;
import io.github.repir.tools.DataTypes.Configuration;
import io.github.repir.tools.Lib.HDTools;
import io.github.repir.tools.Lib.Log;
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
      Configuration conf = HDTools.readConfigNoMR(args, "source");
      Repository repository = new Repository(conf);
      Path p = new Path(conf.get("source"));
      long length = HDFSIn.getLength(HDTools.getFS(), p);
      String[] locations = HDTools.getLocations(HDTools.getFS(), conf.get("source"), 0);
      FileSplit fs = new FileSplit(p, 0, length, locations);
      EntityReader er = new EntityReaderTrec();
      Extractor extractor = new Extractor(repository);
      er.initialize(fs, conf);
      er.nextKeyValue();
      EntityWritable ew = er.getCurrentValue();
      extractor.process(ew.entity);
      for (Map.Entry<String, EntityChannel> c : ew.entity.entrySet()) {
         log.printf("%s %s\n", c.getKey(), c.getValue().getContentStr());
      }
   }

}
