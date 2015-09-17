package io.github.repir.Retriever.MapReduce;

import io.github.htools.io.buffer.BufferDelayedWriter;
import io.github.htools.io.buffer.BufferReaderWriter;
import io.github.repir.Strategy.Collector.MasterCollector;
import io.github.htools.lib.Log;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.lang.reflect.Constructor;
import org.apache.hadoop.io.Writable;
import io.github.repir.Strategy.Collector.Collector;

/**
 * All results that are communicated from Map to Reduce and from Reduce to
 * calling process, have to be collected by Collector objects. Between Map and Reduce
 * the Collector objects are wrapped in CollectorKey and CollectorValue, to 
 * send to the correct reducer and aggregate. 
 * <p/>
 * The collected data is divided over the CollectorKey,CollectorValue pairs. The
 * CollectorKey contains the Query, and the collector's canonical name, to sort
 * data of multiple queries and collectors to the correct key. The CollectorValue
 * contains the collected data, which is aggregated by the Collector in the reducer.
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
