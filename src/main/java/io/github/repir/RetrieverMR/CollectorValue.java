package io.github.repir.RetrieverMR;

import io.github.repir.tools.Content.BufferDelayedWriter;
import io.github.repir.tools.Content.BufferReaderWriter;
import io.github.repir.Strategy.Collector.MasterCollector;
import io.github.repir.tools.Lib.Log;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.lang.reflect.Constructor;
import org.apache.hadoop.io.Writable;
import io.github.repir.Strategy.Collector.Collector;

/**
 * An implementation of Hadoop's {@link Writable} that transmits a
 * {@link Strategy.MasterCollector} with all contained data over the
 * MapReduce framework. This is used to collect results in every mapper, send
 * these to the reducer where these can be aggregated.
 * <p/>
 * @author jeroen
 */
public class CollectorValue implements Writable {

   public static Log log = new Log(CollectorValue.class);
   public int collectorid;
   public Collector collector;
   public BufferReaderWriter reader;
   public int partition;

   public CollectorValue() {
   }

   @Override
   public void write(DataOutput out) {
      BufferDelayedWriter writer = new BufferDelayedWriter();
      writer.writeC(collector.strategy.collectors.indexOf( collector));
      writer.write(collector.getCanonicalName());
      writer.writeC(partition);
      collector.writeKey(writer);
      collector.writeValue(writer);
      writer.writeBuffer(out);
      writer = null;
   }

   @Override
   public void readFields(DataInput in) throws IOException {
      reader = new BufferReaderWriter(in);
      collectorid = reader.readCInt();
      String collectorclass = reader.readString();
      collector = Collector.create(collectorclass);
      partition = reader.readCInt();
      //log.info("readFields class=%s", collectorclass);
      collector.readKey(reader);
      collector.readValue(reader);
      reader = null;
   }
}
