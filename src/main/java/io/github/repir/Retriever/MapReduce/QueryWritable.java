package io.github.repir.Retriever.MapReduce;

import io.github.htools.io.buffer.BufferDelayedWriter;
import io.github.htools.io.buffer.BufferReaderWriter;
import io.github.htools.io.struct.StructureReader;
import io.github.htools.io.struct.StructureWriter;
import io.github.repir.Repository.Repository;
import io.github.repir.Retriever.Query;
import io.github.htools.io.EOCException;
import io.github.htools.lib.Log;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import org.apache.hadoop.io.WritableComparable;
import org.apache.hadoop.io.WritableComparator;

/**
 * An implementation of Hadoop's {@link WritableComparator} class, that is used
 * to send a {@link Query} request to the mapper and the reducer. By
 * default, a separate reducer is created for each query, and results are
 * combined by comparing the unique query id.
 * <p/>
 * @author jeroen
 */
public class QueryWritable implements WritableComparable<QueryWritable> {

   public static Log log = new Log(QueryWritable.class);
   private Query query;

   public QueryWritable() {
   }

   public QueryWritable(Query query) {
      this.query = query;
   }

   public Query getQuery(Repository repository) {
      query.setRepository(repository);
      return query;
   }

   public int getVariants() {
      return query.variantCount();
   }

   public QueryWritable split() {
      return new QueryWritable(query.splitVariants());
   }

   @Override
   public void write(DataOutput out) throws IOException {
      BufferDelayedWriter writer = new BufferDelayedWriter();
      query.write(writer);
      writer.writeBuffer(out);
   }

   public void write(StructureWriter writer) {
      query.write(writer);
   }

   @Override
   public void readFields(DataInput in) throws IOException {
      BufferReaderWriter rw = new BufferReaderWriter();
      rw.readBuffer(in);
      readFields(rw);
   }

   public void readFields(StructureReader reader) throws EOCException {
      query = new Query();
      query.read(reader);
   }

   @Override
   public int compareTo(QueryWritable o) { // never used
      log.fatal("compareTo() should never be used");
      return 0;
   }

   /**
    * In case the partitioning code for two queries are the same, the grouping
    * comparator helps to keep the queries apart.
    */
   public static class FirstGroupingComparator extends WritableComparator {

      protected FirstGroupingComparator() {
         super(QueryWritable.class);
      }

      @Override
      public int compare(byte[] b1, int ss1, int l1, byte[] b2, int ss2, int l2) {
         return WritableComparator.compareBytes(b1, ss1 + 4, 4, b2, ss2 + 4, 4);
      }
   }

//   /**
//    * Sends the results of the same query to the same reducer. For optimal
//    * partitioning the Queries should have consecutive id's, so that a separate
//    * reducer is created for each query.
//    */
//   public static class partitioner extends Partitioner<QueryWritable, Writable> {
//
//      @Override
//      public int getPartition(QueryWritable key, Writable value, int i) {
//         return key.query.partition;
//      }
//   }
}
