package io.github.repir.EntityReader.MapReduce;

import io.github.repir.EntityReader.EntityReader;
import io.github.repir.EntityReader.EntityReaderTrec;
import io.github.repir.tools.Content.HDFSDir;
import static io.github.repir.tools.Lib.ClassTools.*;
import io.github.repir.tools.Lib.Log;
import io.github.repir.tools.hadoop.Job;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.compress.CompressionCodec;
import org.apache.hadoop.io.compress.CompressionCodecFactory;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.JobContext;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;
import org.apache.hadoop.mapreduce.lib.output.NullOutputFormat;

/**
 * EntityReaderInputFormat extends FileInputFormat to supply Hadoop with the
 * input to process. To use EntityReaderInputFormat, instantiate with {@link #EntityReaderInputFormat(org.apache.hadoop.mapreduce.Job, java.lang.String[])
 * }
 * using an array of paths on the HDFS, that contain the input files to process.
 * The paths can be files or directories, which are scanned recursively for any
 * file. Before adding a file to the list of inputs,
 * {@link #acceptFile(java.lang.String)} is called to check if this file is to
 * be processed. This way the readme, program and .dtd files in the original
 * TREC collections are skipped.
 * <p/>
 * The input is configured by "repository.inputdir", which can be a comma
 * seperated list of folders, or an array, e.g. multiple
 * "+repository.inputdir=...". The dirs are scanned recursively for input
 * files. See {@link FileFilter} if certain files can be included or excluded.
 * <p/>
 * By default, valid files are submitted to an instantiation of the configured
 * "repository.entityreader". Alternatively, different entityreaders can
 * be configured for different file types, by assigning an entity reader for
 * files that end with some extension, e.g. "+repository.assignentityreader=.pdf
 * EntitReaderPDF"
 * <p/>
 * !!Note that Java does not have a way to uncompress .z files, so the .z files
 * on the original TREC disks have to be uncompressed outside this framework.
 * <p/>
 * @author jeroen
 */
public class EntityReaderInputFormat extends FileInputFormat<LongWritable, EntityWritable> {

   public static Log log = new Log(EntityReaderInputFormat.class);
   FileFilter filefilter;
   String defaultentityreader;
   Configuration configuration;
   HashMap<String, String> assignentityreader = new HashMap<String, String>();
   Job job;

   public EntityReaderInputFormat() {
   }

   public EntityReaderInputFormat(Job job) {
      configuration = job.getConfiguration();
      String inputdirs[] = configuration.get("repository.inputdir").split(",");
      filefilter = new FileFilter(configuration);
      loadEntityReaderSettings(configuration);
      job.setInputFormatClass(this.getClass());
      job.setOutputFormatClass(NullOutputFormat.class);
      for (String dir : inputdirs) {
         addDirs(job, dir);
      }
   }

   public void addDirs(Job job, String dir) {
      FileSystem fs = HDFSDir.getFS(configuration);
      ArrayList<HDFSDir> paths = new ArrayList<HDFSDir>();
      ArrayList<Path> files = new ArrayList<Path>();
      if (dir.length() > 0) {
         HDFSDir d = new HDFSDir(fs, dir);
         if (d.isFile()) {
            addFile(job, new Path(dir));
         } else {
            for (Path f : d.getFiles()) {
               addFile(job, f);
            }
            for (HDFSDir f : d.getSubDirs()) {
               addDirs(job, f.getCanonicalPath());
            }
         }
      }
   }

   public void addFile(Job job, Path path) {
      try {
         if (filefilter.acceptFile(path)) {
            addInputPath(job, path);
         }
      } catch (IOException ex) {
         log.exception(ex, "add( %s, %s )", job, path);
      }
   }

   @Override
   public List<InputSplit> getSplits(JobContext job
                                    ) throws IOException {
      return super.getSplits(job);
   }
   
   public void loadEntityReaderSettings(Configuration conf) {
      defaultentityreader = conf.get("repository.entityreader", EntityReaderTrec.class.getCanonicalName());
      for (String s : conf.getStrings("repository.assignentityreader", new String[0])) {
         String part[] = s.split(" +");
         assignentityreader.put(part[1], part[0]);
      }
   }

   public String getEntityReaderName(InputSplit is, Configuration conf) {
      if (defaultentityreader == null) {
         loadEntityReaderSettings(conf);
      }
      String file = ((FileSplit) is).getPath().getName();
      for (Map.Entry<String, String> entry : assignentityreader.entrySet()) {
         if (file.toLowerCase().endsWith(entry.getKey())) {
            return entry.getValue();
         }
      }
      return defaultentityreader;
   }

   @Override
   public RecordReader<LongWritable, EntityWritable> createRecordReader(InputSplit is, TaskAttemptContext tac) {
      //log.info("documentreader %s", getDocumentReader(tac.getConfiguration()));
      Class clazz = toClass(getEntityReaderName(is, tac.getConfiguration()), EntityReader.class.getPackage().getName());
      Constructor c = getAssignableConstructor(clazz, EntityReader.class);
      return (RecordReader<LongWritable, EntityWritable>) construct(c);
   }

   @Override
   protected boolean isSplitable(JobContext context, Path file) {
      return context.getConfiguration().getBoolean("repository.splitablesource", false);
   }
}
