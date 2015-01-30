package io.github.repir.Retriever.Tuner;

import io.github.repir.Retriever.MapReduce.CollectorKey;
import io.github.repir.Retriever.MapReduce.CollectorValue;
import io.github.repir.Retriever.Query;
import io.github.repir.Retriever.Retriever;
import io.github.repir.tools.lib.Log;
import java.io.IOException;
import java.util.Collection;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.mapreduce.lib.output.NullOutputFormat;

public class Job extends io.github.repir.Retriever.Reusable.Job {

   public static Log log = new Log(Job.class);

   public Job(Retriever retriever) throws IOException {
      super(retriever);
      setJobName("M1Retriever " + retriever.repository.configuredString("rr.conf"));
   }
   
   public Job(Retriever retriever, String path) throws IOException {
      this(retriever);
      if (path != null) {
         this.path = path;
      }
   }

   @Override
   public void setJob() {
      setJarByClass(RetrieverMap.class);
      setMapOutputKeyClass(CollectorKey.class);
      setMapOutputValueClass(CollectorValue.class);
      setOutputKeyClass(NullWritable.class);
      setOutputValueClass(NullWritable.class);
      setMapperClass(RetrieverMap.class);
      if (!retriever.repository.configuredString("testset.crossevaluate").equalsIgnoreCase("fold"))
         setReducerClass(RetrieverReduce.class);
      else
         setReducerClass(RetrieverReduceFold.class);
      setInputFormatClass(inputformat.getClass());
      setOutputFormatClass(NullOutputFormat.class);
      setGroupingComparatorClass(CollectorKey.FirstGroupingComparator.class);
      setSortComparatorClass(CollectorKey.FirstGroupingComparator.class);
      setPartitionerClass(CollectorKey.partitioner.class);
   }
   
   @Override
   public void addQuery(Query q) {
      inputformat.addSeparate(q);
   }
   
   @Override
   public void setReducers(Collection<String> reducers) {
      setNumReduceTasks( 1 );
   }

}
