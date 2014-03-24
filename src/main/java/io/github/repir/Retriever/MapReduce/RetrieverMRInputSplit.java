package io.github.repir.Retriever.MapReduce;

import io.github.repir.tools.Content.BufferDelayedWriter;
import io.github.repir.tools.Content.BufferReaderWriter;
import io.github.repir.tools.Content.HDFSDir;
import io.github.repir.tools.Lib.ArrayTools;
import io.github.repir.tools.Lib.HDTools;
import io.github.repir.Repository.Repository;
import io.github.repir.Retriever.Query;
import io.github.repir.tools.Lib.Log;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.mapreduce.InputSplit;

/**
 * A custom implementation of Hadoop's InputSplit used by RetrieverMR. 
 * Each Split holds a set of Queries tasks that must all belong to the same partition. 
 * <p/>
 * @author jeroen
 */
public class RetrieverMRInputSplit extends InputSplit implements Writable, Comparable<RetrieverMRInputSplit> {

   public static Log log = new Log(RetrieverMRInputSplit.class);
   public ArrayList<QueryWritable> list = new ArrayList<QueryWritable>();
   String hosts[]; // preferred node to execute the mapper
   public int partition; 

   public RetrieverMRInputSplit() {
   }

   public RetrieverMRInputSplit(Repository repository, int partition) {
      // if index creation was done properly, a single reducer was used to write all
      // files for a single partition. These files have probably been replicated,
      // so the intersection of hosts indicates the best node to map the split.
      hosts = repository.getPartitionLocation(partition);
      this.partition = partition;
   }

   public RetrieverMRInputSplit cloneEmpty() {
      RetrieverMRInputSplit clone = new RetrieverMRInputSplit();
      clone.hosts = hosts;
      clone.partition = partition;
      return clone;
   }

   /**
    * @param q The Query request to add to this Split
    */
   public void add(Query q) {
      this.list.add(new QueryWritable(q));
   }

   /**
    * @param q The Query request to add to this Split
    */
   protected void add(QueryWritable q) {
      this.list.add(q);
   }

   /**
    * @return the number of Query requests in this split
    */
   @Override
   public long getLength() throws IOException, InterruptedException {
      return list.size();
   }
   
   public int size() {
      int count = 0;
      for (QueryWritable q : list)
         count += q.getVariants();
      return count;
   }

   @Override
   public void write(DataOutput out) throws IOException {
      BufferDelayedWriter writer = new BufferDelayedWriter();
      writer.write(hosts);
      writer.write(partition);
      writer.write(list.size());
      for (QueryWritable q : list) {
         q.write(writer);
      }
      out.write(writer.getAsByteBlock());
   }

   @Override
   public void readFields(DataInput in) throws IOException {
      int length = in.readInt();
      byte b[] = new byte[length];
      in.readFully(b);
      BufferReaderWriter reader = new BufferReaderWriter(b);
      hosts = reader.readStringArray();
      partition = reader.readInt();
      int listsize = reader.readInt();
      list = new ArrayList<QueryWritable>();
      for (int q = 0; q < listsize; q++) {
         QueryWritable qp = new QueryWritable();
         qp.readFields(reader);
         list.add(qp);
      }
   }

   @Override
   public String[] getLocations() throws IOException, InterruptedException {
      return hosts;
   }

   // enables sorting of splits, so that bigger splits can be processed first
   @Override
   public int compareTo(RetrieverMRInputSplit o) {
      int comp = o.list.size() - list.size();
      if (comp == 0) {
         comp = o.list.get(0).getVariants() - list.get(0).getVariants();
      }
      return comp != 0 ? comp : 1; // cannot be 0!
   }
}
