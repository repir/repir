package io.github.repir.Retriever.Reusable;

import io.github.repir.Retriever.MapReduce.QueryInputFormat;
import io.github.repir.Retriever.MapReduce.CollectorKey;
import io.github.repir.Retriever.MapReduce.CollectorValue;
import io.github.repir.Retriever.MapReduce.RetrieverMRReduce;
import io.github.repir.Retriever.Retriever;
import io.github.repir.tools.Lib.Log;
import java.io.IOException;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.mapreduce.lib.output.NullOutputFormat;

public class Job extends io.github.repir.Retriever.MapReduce.RetrieverJob {

   public static Log log = new Log(Job.class);

   public Job(Retriever retriever) throws IOException {
      super(retriever);
      setJobName("MRetriever " + retriever.repository.configuredString("rr.conf"));
   }
   
   public Job(Retriever retriever, String path) throws IOException {
      this(retriever);
      if (path != null) {
         this.path = path;
      }
   }

   public void setJob() {
      setJarByClass(RetrieverMap.class);
      setMapOutputKeyClass(CollectorKey.class);
      setMapOutputValueClass(CollectorValue.class);
      setOutputKeyClass(NullWritable.class);
      setOutputValueClass(NullWritable.class);
      setMapperClass(RetrieverMap.class);
      setReducerClass(RetrieverMRReduce.class);
      setInputFormatClass(inputformat.getClass());
      setOutputFormatClass(NullOutputFormat.class);
      setGroupingComparatorClass(CollectorKey.FirstGroupingComparator.class);
      setSortComparatorClass(CollectorKey.FirstGroupingComparator.class);
      setPartitionerClass(CollectorKey.partitioner.class);
   }
}
