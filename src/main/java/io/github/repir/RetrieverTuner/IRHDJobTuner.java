package io.github.repir.RetrieverTuner;

import io.github.repir.RetrieverMR.RetrieverMRInputFormat;
import io.github.repir.RetrieverMR.CollectorKey;
import io.github.repir.RetrieverMR.CollectorValue;
import io.github.repir.RetrieverMR.IRHDJob;
import io.github.repir.Retriever.Retriever;
import io.github.repir.tools.Lib.Log;
import java.io.IOException;
import java.util.Collection;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.mapreduce.lib.output.NullOutputFormat;
import io.github.repir.RetrieverMulti.IRHDJobMulti;

public class IRHDJobTuner extends IRHDJobMulti {

   public static Log log = new Log(IRHDJob.class);

   public IRHDJobTuner(Retriever retriever) throws IOException {
      super(retriever);
      setJobName("M1Retriever " + retriever.repository.getConfigurationString("repir.conf"));
   }
   
   public IRHDJobTuner(Retriever retriever, String path) throws IOException {
      this(retriever);
      if (path != null) {
         this.path = path;
      }
   }

   @Override
   public void setJob() {
      setJarByClass(RetrieverTunerMap.class);
      setMapOutputKeyClass(CollectorKey.class);
      setMapOutputValueClass(CollectorValue.class);
      setOutputKeyClass(NullWritable.class);
      setOutputValueClass(NullWritable.class);
      setMapperClass(RetrieverTunerMap.class);
      if (!retriever.repository.getConfigurationString("testset.crossevaluate").equalsIgnoreCase("fold"))
         setReducerClass(RetrieverTuneReduce.class);
      else
         setReducerClass(RetrieverTuneFoldReduce.class);
      setInputFormatClass(RetrieverMRInputFormat.class);
      setOutputFormatClass(NullOutputFormat.class);
      setGroupingComparatorClass(CollectorKey.FirstGroupingComparator.class);
      setSortComparatorClass(CollectorKey.FirstGroupingComparator.class);
      setPartitionerClass(CollectorKey.partitioner.class);
   }
   
   @Override
   public void setReducers(Collection<String> reducers) {
      setNumReduceTasks( 1 );
   }

}
