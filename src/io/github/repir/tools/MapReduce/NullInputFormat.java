package io.github.repir.tools.MapReduce;

import io.github.repir.Repository.Repository;
import io.github.repir.Retriever.Query;
import io.github.repir.tools.Lib.Log;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TreeSet;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
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
 * When cansplit==true, then the InputSplits are divided over 2 * nodes in cluster
 * (as defined in cluster.nodes), to divide the workload more evenly.
 * 
 * @author jeroen
 */
public class NullInputFormat extends MRInputFormat<NullWritable, Integer> {

   public static Log log = new Log(NullInputFormat.class);

   public NullInputFormat() {}
   
   public NullInputFormat(Repository repository) {
      super(repository);
   }
   
   @Override
   public MRInputSplit<NullWritable, Integer> createIS(Repository repository, int partition) {
      return new NullInputSplit(repository, partition);
   }

}
