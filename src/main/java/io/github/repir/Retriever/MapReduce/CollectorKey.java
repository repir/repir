package io.github.repir.Retriever.MapReduce;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.io.WritableComparable;
import org.apache.hadoop.io.WritableComparator;
import org.apache.hadoop.mapreduce.Partitioner;
import io.github.repir.Strategy.Collector.Collector;
import io.github.repir.Retriever.Query;
import io.github.repir.tools.io.buffer.BufferDelayedWriter;
import io.github.repir.tools.io.buffer.BufferReaderWriter;
import io.github.repir.tools.lib.Log;

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
public class CollectorKey implements WritableComparable<CollectorKey> {

   public static Log log = new Log(CollectorKey.class);
   public Collector collector;
   public int reducer;
   public boolean isQuery;
   public Query query;

   public CollectorKey() {
   }

   public void set( Collector collector, Query query ) {
      this.query = query;
      this.collector = collector;
      this.reducer = collector.getReducerID( );
      if (reducer < 0) {
         log.info("Reducer does not exist: collector %s %s %s", 
                 collector.getClass().getSimpleName(),
                 collector.getReducerName(), 
                 collector.getReducerIDs());
         log.info("Reducer does not exist: reducers %s", collector.strategy.collectors.getReducers());
         log.fatal("");
      }
      isQuery = Character.isDigit( collector.getReducerName().charAt(0) );
      //log.info(".set() query=%d reducer=%s id=%d", query.id, collector.getReducerIDs(), reducer);
   }
   
   public Query getQuery() {
      return query;
   }
   
   public int getReducer() {
      return reducer;
   }
   
   @Override
   public void write(DataOutput out) {
      BufferDelayedWriter writer = new BufferDelayedWriter();
      writer.writeC( reducer );
      writer.write( isQuery );
      if (isQuery)
         query.write(writer);
      else {
         writer.write(collector.getCanonicalName());
         collector.writeKey(writer);
      }
      writer.writeBuffer(out);
   }

   @Override
   public void readFields(DataInput in) throws IOException {
      BufferReaderWriter reader = new BufferReaderWriter(in);
      reducer = reader.readCInt();
      isQuery = reader.readBoolean();
      if (isQuery) {
         query = new Query();
         query.read(reader);
      } else {
         String classname = reader.readString();
         collector = Collector.create(classname);
         collector.readKey(reader);
      }
   }

   public int compareTo(CollectorKey o) {
      log.fatal("compareTo() should never be used");
      return 0;
   }
   
   /**
    * sorts and groups results by variant number and then by query id
    */
   public static class FirstGroupingComparator extends WritableComparator {

      protected FirstGroupingComparator() {
         super(CollectorKey.class);
      }

      @Override
      public int compare(byte[] b1, int ss1, int l1, byte[] b2, int ss2, int l2) {
         return WritableComparator.compareBytes(b1, ss1+4, l1-4, b2, ss2+4, l2-4);
      }
   }

   /**
    * Sends the results of the same query to the same reducer. For optimal
    * partitioning the Queries should have consecutive id's, so that a separate
    * reducer is created for each query.
    */
   public static class partitioner extends Partitioner<CollectorKey, Writable> {

      @Override
      public int getPartition(CollectorKey key, Writable value, int i) {
         return key.reducer;
      }
   }
}
