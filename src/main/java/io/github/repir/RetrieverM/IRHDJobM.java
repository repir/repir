package io.github.repir.RetrieverM;

import io.github.repir.Retriever.Retriever;
import io.github.repir.tools.Lib.Log;
import java.io.IOException;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.NullOutputFormat;
import io.github.repir.Retriever.Query;

public class IRHDJobM extends io.github.repir.RetrieverMR.IRHDJob {

   public static Log log = new Log(IRHDJobM.class);

   public IRHDJobM(Retriever retriever) throws IOException {
      super(retriever);
   }
   
   public IRHDJobM(Retriever retriever, String path) throws IOException {
      super( retriever, path );
   }

   @Override
   public void setJob() {
      setJobName("RetrieverM " + retriever.repository.getConfigurationString("testset.name"));
      setJarByClass(RetrieverMMap.class);
      setMapOutputKeyClass(NullWritable.class);
      setMapOutputValueClass(NullWritable.class);
      setMapperClass(RetrieverMMap.class);
      setInputFormatClass(RetrieverMInputFormat.class);
      Path p = new Path("dummy");
      try {
         retriever.repository.getFS().delete(p, true);
         FileOutputFormat.setOutputPath(this, p);
      } catch (IOException ex) {
         Logger.getLogger(IRHDJobM.class.getName()).log(Level.SEVERE, null, ex);
      }
   }
   
   @Override
   public void setupInputFormat() {
      RetrieverMInputFormat.setIndex(retriever.repository);
      RetrieverMInputFormat.clear();
   }
   
   @Override
   public void addQuery(Query q) {
      RetrieverMInputFormat.add(retriever.repository, q);
   }
   
   @Override
   public void addSingleQuery(Query q) {
      RetrieverMInputFormat.addSingle(retriever.repository, q);
   }
}
