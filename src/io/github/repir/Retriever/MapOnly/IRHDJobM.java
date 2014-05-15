package io.github.repir.Retriever.MapOnly;

import io.github.repir.Retriever.MapReduce.QueryInputFormat;
import io.github.repir.Retriever.Query;
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

public class IRHDJobM extends io.github.repir.Retriever.MapReduce.RetrieverJob {

   public static Log log = new Log(IRHDJobM.class);
   protected QueryInputFormat inputformat;

   public IRHDJobM(Retriever retriever) throws IOException {
      super(retriever);
      inputformat = new QueryInputFormat(retriever.repository);
   }
   
   public IRHDJobM(Retriever retriever, String path) throws IOException {
      super( retriever, path );
      inputformat = new QueryInputFormat(retriever.repository);
   }

   @Override
   public void setJob() {
      setJobName("RetrieverM " + retriever.repository.configuredString("testset.name"));
      setJarByClass(RetrieverMMap.class);
      setMapOutputKeyClass(NullWritable.class);
      setMapOutputValueClass(NullWritable.class);
      setMapperClass(RetrieverMMap.class);
      setInputFormatClass(inputformat.getClass());
      Path p = new Path("dummy");
      try {
         retriever.repository.getFS().delete(p, true);
         FileOutputFormat.setOutputPath(this, p);
      } catch (IOException ex) {
         Logger.getLogger(IRHDJobM.class.getName()).log(Level.SEVERE, null, ex);
      }
   }
   
   @Override
   public void addQuery(Query q) {
      inputformat.add(q);
   }
   
   @Override
   public void addSingleQuery(Query q) {
      inputformat.addSingle(q);
   }
}
