package io.github.repir.Retriever.MapOnly;

import io.github.repir.Retriever.MapReduce.QueryWritable;
import io.github.repir.Retriever.MapReduce.RetrieverMRInputSplit;
import io.github.repir.Repository.Repository;
import io.github.repir.Retriever.Query;
import io.github.repir.tools.Lib.Log;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.hadoop.io.NullWritable;
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
public class RetrieverMInputFormat extends io.github.repir.Retriever.MapReduce.RetrieverMRInputFormat {

   public static Log log = new Log(RetrieverMInputFormat.class);
   static ArrayList<InputSplit> list = new ArrayList<InputSplit>();

   @Override
   public RecordReader<NullWritable, QueryWritable> createRecordReader(InputSplit is, TaskAttemptContext tac) {
      return new KeyRecordReader();
   }

   /**
    * Add a Query request to the MapReduce job. Note that this is used as a
    * static method (i.e. can only construct one job at the same startTime).
    * <p/>
    * @param repository Repository to retrieve the Query request from
    * @param queryrequest The Query request to retrieve
    */
   public static void add(Repository repository, Query queryrequest) {
      RetrieverMRInputSplit split = new RetrieverMRInputSplit(repository, 0);
      list.add(split);
      split.add(queryrequest);
   }

   public static void addSingle(Repository repository, Query queryrequest) {
      add(repository, queryrequest);
   }

   public static void clear() {
      list = new ArrayList<InputSplit>();
   }

   @Override
   public List<InputSplit> getSplits(JobContext context) throws IOException, InterruptedException {
      return list;
   }
}
