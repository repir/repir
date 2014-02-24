package io.github.repir.RetrieverMR;

import io.github.repir.Repository.Repository;
import io.github.repir.Retriever.Query;
import io.github.repir.tools.Lib.Log;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TreeSet;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.mapreduce.InputFormat;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.JobContext;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;

/**
 * A custom implementation of Hadoop's InputFormat, that holds the InputSplits
 * that are to be retrieved. This class should be used as static, using
 * {@link #setIndex(Repository.Repository)} to initialize and 
 * {@link #add(Repository.Repository, IndexReader.Query) }
 * to add Query requests to the MapReduce job. Internally, a separate InputSplit
 * is created for each repository partition. Whenever a Query request is added,
 * it is added to each Split.
 * <p/>
 * @author jeroen
 */
public class RetrieverMRInputFormat extends InputFormat<NullWritable, QueryWritable> {

   public static Log log = new Log(RetrieverMRInputFormat.class);
   public static Repository repository;
   static HashMap<Integer, RetrieverMRInputSplit> list = new HashMap<Integer, RetrieverMRInputSplit>();
   static boolean cansplit = false;

   @Override
   public RecordReader<NullWritable, QueryWritable> createRecordReader(InputSplit is, TaskAttemptContext tac) {
      return new KeyRecordReader();
   }

   /**
    * Initialize the InputFormat to use the given repository and clear the list
    * of splits. Note that the Queries for one job should use the same
    * Repository.
    * <p/>
    * @param repository
    */
   public static void setIndex(Repository repository) {
      RetrieverMRInputFormat.repository = repository;
      clear();
   }

   /**
    * @param cansplit true indicates Splits may be divided into smaller Splits
    * to retrieve queries in parallel.
    */
   public static void setSplitable(boolean cansplit) {
      RetrieverMRInputFormat.cansplit = cansplit;
   }

   /**
    * Add a Query request to the MapReduce job. Note that this is used as a
    * static method (i.e. can only construct one job at the same startTime).
    * <p/>
    * @param repository Repository to retrieve the Query request from
    * @param queryrequest The Query request to retrieve
    */
   public static void add(Repository repository, Query queryrequest) {
      for (int partition = 0; partition < repository.getPartitions(); partition++) {
         RetrieverMRInputSplit split = list.get(partition);
         if (split == null) {
            split = new RetrieverMRInputSplit(repository, partition);
            list.put(partition, split);
         }
         ((RetrieverMRInputSplit) split).add(queryrequest);
      }
   }

   public static void addSingle(Repository repository, Query queryrequest) {
      RetrieverMRInputSplit split = new RetrieverMRInputSplit(repository, 0);
      split.add(queryrequest);
      list.put(list.size(), split);
   }

   /**
    * Clears the list of map tasks
    */
   public static void clear() {
      list = new HashMap<Integer, RetrieverMRInputSplit>();
   }

   /**
    * if there are less partitions than we have nodes, we can divide Splits into
    * smaller Splits to retrieve queries in parallel. This requires
    * cluster.nodes to be set in the configuration file.
    * <p/>
    * @return
    */
   private static ArrayList<InputSplit> splitList() {
      long count = 0;
      TreeSet<RetrieverMRInputSplit> set = new TreeSet<RetrieverMRInputSplit>(list.values());
      for (RetrieverMRInputSplit split : list.values()) {
         count += split.size();
      }
      int maxnodes = repository.getConfiguration().getInt("cluster.nodes", 1) * 2;
      log.info("variants %d maxnodes %d set %d", count, maxnodes, set.size());
      while (count > set.size() && set.size() < maxnodes) {
         RetrieverMRInputSplit split = set.pollFirst(); // get largest Split
         log.info("old split split1 %d set %d", split.size(), set.size());
         if (split.list.size() == 1 && split.list.get(0).getVariants() == 1) {
            break;
         }
         RetrieverMRInputSplit split2 = new RetrieverMRInputSplit();
         split2.hosts = split.hosts;
         split2.partition = split.partition;
         if (split.list.size() > 1) {
            while (split2.list.size() < split.list.size() - 1) {
               split2.list.add(split.list.remove(0));
            }
         } else {
            split2.add(split.list.get(0).split());
         }
         log.info("split split1 %d split2 %d set %d", split.size(), split2.size(), set.size());
         set.add(split);
         set.add(split2);
      }
      return new ArrayList<InputSplit>(set);
   }
   
   @Override
   public List<InputSplit> getSplits(JobContext context) throws IOException, InterruptedException {
      return (cansplit) ? splitList() : new ArrayList<InputSplit>(list.values());
   }

   public static class KeyRecordReader extends RecordReader<NullWritable, QueryWritable> {

      private QueryWritable current;
      private RetrieverMRInputSplit is;
      private int pos = 0;

      @Override
      public void initialize(InputSplit is, TaskAttemptContext tac) throws IOException, InterruptedException {
         //log.info("initialize()");
         this.is = (RetrieverMRInputSplit) is;
      }

      @Override
      public boolean nextKeyValue() throws IOException, InterruptedException {
         if (pos < is.list.size()) {
            current = is.list.get(pos++);
            return true;
         }
         return false;
      }

      @Override
      public NullWritable getCurrentKey() throws IOException, InterruptedException {
         return NullWritable.get();
      }

      @Override
      public QueryWritable getCurrentValue() throws IOException, InterruptedException {
         return current;
      }

      @Override
      public float getProgress() throws IOException, InterruptedException {
         return (pos) / (float) (is.list.size());
      }

      @Override
      public void close() throws IOException {
      }
   }
}
