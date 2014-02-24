package io.github.repir.RetrieverMulti;

import io.github.repir.RetrieverMR.RetrieverMRInputFormat;
import io.github.repir.RetrieverMR.CollectorKey;
import io.github.repir.RetrieverMR.CollectorValue;
import io.github.repir.RetrieverMR.RetrieverMRReduce;
import io.github.repir.RetrieverMR.IRHDJob;
import io.github.repir.Retriever.Retriever;
import io.github.repir.tools.Lib.Log;
import java.io.IOException;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.mapreduce.lib.output.NullOutputFormat;

public class IRHDJobMulti extends IRHDJob {

   public static Log log = new Log(IRHDJob.class);

   public IRHDJobMulti(Retriever retriever) throws IOException {
      super(retriever);
      setJobName("MRetriever " + retriever.repository.getConfigurationString("repir.conf"));
   }
   
   public IRHDJobMulti(Retriever retriever, String path) throws IOException {
      this(retriever);
      if (path != null) {
         this.path = path;
      }
   }

   public void setJob() {
      setJarByClass(RetrieverMultiMap.class);
      setMapOutputKeyClass(CollectorKey.class);
      setMapOutputValueClass(CollectorValue.class);
      setOutputKeyClass(NullWritable.class);
      setOutputValueClass(NullWritable.class);
      setMapperClass(RetrieverMultiMap.class);
      setReducerClass(RetrieverMRReduce.class);
      setInputFormatClass(RetrieverMRInputFormat.class);
      setOutputFormatClass(NullOutputFormat.class);
      setGroupingComparatorClass(CollectorKey.FirstGroupingComparator.class);
      setSortComparatorClass(CollectorKey.FirstGroupingComparator.class);
      setPartitionerClass(CollectorKey.partitioner.class);
   }
}
