package io.github.repir.EntityReader;

import io.github.repir.tools.Content.Datafile;
import java.io.EOFException;
import io.github.repir.tools.Content.HDFSIn;
import io.github.repir.tools.Lib.Log;
import java.io.IOException;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.DataOutputBuffer;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;
import io.github.repir.tools.DataTypes.Configuration;

/**
 * A document reader read an input file, identifying document markers to store
 * one document at a startTime in a BytesWritable, that is used in a map() process.
 * The LongWritable that is passed along indicates the offset in the input file,
 * which can be used to trace problems.
 * <p/>
 * <p/>
 * @author jeroen
 */
public abstract class IREFEntityReader extends RecordReader<LongWritable, EntityWritable> {

   public static Log log = new Log(IREFEntityReader.class);
   protected TaskAttemptContext context;
   protected long start;
   protected long end;
   protected Datafile fsin;
   protected LongWritable key = new LongWritable();
   protected EntityWritable entitywritable;
   protected FileSystem filesystem;
   protected Configuration conf;
   protected int onlypartition;
   protected int partitions;

   @Override
   public void initialize(InputSplit is, TaskAttemptContext tac) {
      //log.info("initialize");
      try {
         context = tac;
         conf = Configuration.convert(tac.getConfiguration());
         filesystem = FileSystem.get(conf);
         FileSplit fileSplit = (FileSplit) is;
         Path file = fileSplit.getPath();
         start = fileSplit.getStart();
         end = start + fileSplit.getLength();
         fsin = new Datafile(filesystem, file);
         fsin.setOffset(start);
         fsin.setBufferSize(10000000);
         fsin.openRead();
         onlypartition = conf.getInt("repository.onlypartition", -1);
         partitions = conf.getInt("repository.partitions", 1);
         initialize(fileSplit);
      } catch (IOException ex) {
         log.exception(ex, "initialize( %s, %s ) conf %s filesystem %s fsin %s", is, tac, conf, filesystem, fsin);
      }
   }

   public abstract void initialize(FileSplit fileSplit);

   /**
    * Reads the input file, scanning for the next document, setting key and
    * entitywritable with the offset and byte contents of the document read.
    * <p/>
    * @return true if a next document was read
    */
   @Override
   public abstract boolean nextKeyValue();

   @Override
   public LongWritable getCurrentKey() throws IOException, InterruptedException {
      return key;
   }

   @Override
   public EntityWritable getCurrentValue() throws IOException, InterruptedException {
      return entitywritable;
   }

   /**
    * NB this indicates progress as the data that has been read, for some
    * MapReduce tasks processing the data continues for some startTime, causing the
    * progress indicator to halt at 100%.
    * <p/>
    * @return
    * @throws IOException
    * @throws InterruptedException
    */
   @Override
   public float getProgress() throws IOException, InterruptedException {
      return (fsin.getOffset() - start) / (float) (end - start);
   }

   @Override
   public void close() throws IOException {
      fsin.close();
   }
}
