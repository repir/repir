package io.github.repir.tools.MapReduce;

import io.github.repir.Repository.Repository;
import io.github.repir.tools.Lib.Log;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TreeSet;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.mapreduce.InputFormat;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.JobContext;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;

/**
 * A custom implementation of Hadoop's InputFormat, that holds the InputSplits
 * that are to be retrieved. This class should be used as static, using
 * {@link #setRepository(Repository.Repository)} to initialize and 
 * {@link #add(Repository.Repository, IndexReader.Query) }
 * to add Query requests to the MapReduce job. Internally, a separate InputSplit
 * is created for each repository partition. Whenever a Query request is added,
 * it is added to each Split.
 * <p/>
 * When cansplit==true, then the InputSplits are divided over 2 * nodes in
 * cluster (as defined in cluster.nodes), to divide the workload more evenly.
 *
 * @author jeroen
 */
public abstract class MRInputFormat<PWRITABLE extends Writable, P> extends InputFormat<IntWritable, PWRITABLE> {

   public static Log log = new Log(MRInputFormat.class);
   public static Repository repository;
   static boolean cansplit = true;
   static HashMap<Integer, MRInputSplit> list = new HashMap<Integer, MRInputSplit>();

   public MRInputFormat() {}
   
   public MRInputFormat(Repository repository) {
      setRepository(repository);
   }
   
   @Override
   public RecordReader<IntWritable, PWRITABLE> createRecordReader(InputSplit is, TaskAttemptContext tac) {
      return new MRRecordReader<PWRITABLE, P>();
   }

   /**
    * Initialize the InputFormat to use the given repository and clear the list
    * of splits. Note that the Queries for one job should use the same
    * Repository.
    * <p/>
    * @param repository
    */
   public void setRepository(Repository repository) {
      MRInputFormat.repository = repository;
      cansplit = repository.configuredBoolean("inputformat.cansplit", true);
      list = new HashMap<Integer, MRInputSplit>();
   }

   /**
    * Add a Query request to the MapReduce job. Note that this is used as a
    * static method (i.e. can only construct one job at the same startTime).
    * <p/>
    * @param repository Repository to retrieve the Query request from
    * @param queryrequest The Query request to retrieve
    */
   public void add(P term) {
      for (int partition = 0; partition < repository.getPartitions(); partition++) {
         MRInputSplit<PWRITABLE, P> split = list.get(partition);
         if (split == null) {
            split = createIS(repository, partition);
            list.put(partition, split);
         }
         split.add(term);
      }
   }

   public void addSeparate(P queryrequest) {
      for (int partition = 0; partition < repository.getPartitions(); partition++) {
         MRInputSplit<PWRITABLE, P> split = createIS(repository, partition);
         split.add(queryrequest);
         list.put(list.size(), split);
      }
   }

   public void addSingle(P queryrequest) {
      MRInputSplit<PWRITABLE, P> split = createIS(repository, 0);
      split.add(queryrequest);
      list.put(list.size(), split);
   }

   public void setSplitable(boolean cansplit) {
      MRInputFormat.cansplit = cansplit;
   }

   public abstract MRInputSplit<PWRITABLE, P> createIS(Repository repository, int partition);

   private static long count() {
      long count = 0;
      for (MRInputSplit split : list.values()) {
         count += split.size();
      }
      return count;
   }

   /**
    * if there are less partitions than we have nodes, we can divide Splits into
    * smaller Splits to retrieve queries in parallel. This requires
    * cluster.nodes to be set in the configuration file.
    * <p/>
    * @return @throws java.io.IOException
    * @throws java.lang.InterruptedException
    */
   @Override
   public List<InputSplit> getSplits(JobContext context) throws IOException, InterruptedException {
      if (!cansplit) {
         return new ArrayList<InputSplit>(list.values());
      }
      long count = count();
      TreeSet<MRInputSplit> set = new TreeSet<MRInputSplit>(list.values());
      int maxnodes = repository.getConfiguration().getInt("cluster.nodes", 1) * 2;
      while (count > set.size() && set.size() < maxnodes) {
         MRInputSplit<PWRITABLE, P> split = set.pollFirst();
         if (split.list.size() == 1) {
            break;
         }
         MRInputSplit<PWRITABLE, P> split2 = createIS(repository, split.partition);
         split2.hosts = split.hosts;
         split2.partition = split.partition;
         while (split2.list.size() < split.list.size() - 1) {
            split2.list.add(split.list.remove(0));
         }
         set.add(split);
         set.add(split2);
      }
      return new ArrayList<InputSplit>(set);
   }
}
