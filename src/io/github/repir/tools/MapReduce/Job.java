package io.github.repir.tools.MapReduce;

import io.github.repir.Repository.Repository;
import io.github.repir.tools.Content.FSFile;
import io.github.repir.Repository.Configuration;
import io.github.repir.tools.Lib.Log;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.util.StringUtils;

/**
 * Extension of Hadoop Job, setting the Repir jar files to include in the MR Job.
 * @author jer
 */
public class Job extends org.apache.hadoop.mapreduce.Job {

   public static Log log = new Log(Job.class);
   public Repository repository;

   public Job(Repository repository) throws IOException {
      // Jars need to be added to the Configuration before construction 
      super(addJars(repository.getConfiguration()));
      this.repository = repository;
      //addJars(repository.getConfiguration());
      //HDTools.addJars(repository.getConfiguration());
   }

   public Job(Repository repository, String jobname) throws IOException {
      this(repository);
      setJobName(jobname);
   }

  public void submit() throws IOException, InterruptedException, ClassNotFoundException {
      setJarByClass(this.getMapperClass());
      super.submit();
  }
   
   public static Configuration addJars(Configuration conf) {
      String libs = libToArgs(conf);
      try {
         if (libs != null && libs.length() > 0) {
            String[] files = validateFiles(libs, conf).split(",");
            conf.set("tmpjars", validateFiles(libs, conf)); // seems to be needed by Job
            URL[] libjars = new URL[files.length];
            for (int i = 0; i < files.length; i++) {
               Path tmp = new Path(files[i]);
               libjars[i] = FileSystem.getLocal(conf).pathToFile(tmp).toURI().toURL();
               //log.info("lib %s", libjars[i]);
            }
            if (libjars != null && libjars.length > 0) {
               conf.setClassLoader(new URLClassLoader(libjars, conf.getClassLoader()));
               Thread.currentThread().setContextClassLoader(
                       new URLClassLoader(libjars,
                       Thread.currentThread().getContextClassLoader()));
            }
         }
      } catch (IOException ex) {
         log.fatalexception(ex, "addJarsToJobClassPath", libs);
      }
      return conf;
   }

   public static String libToArgs(Configuration conf) {
      StringBuilder sb = new StringBuilder();
      for (String lib : conf.getStrings("rr.lib")) {
         if (!FSFile.exists(lib)) {
            lib = conf.get("rr.libdir") + lib;
         }
         sb.append(",").append(lib);
      }
      return (sb.length() == 0)?"":sb.substring(1);
   }
   
   public static String validateFiles(String files, Configuration conf) throws IOException {
      if (files == null) {
         return null;
      }
      String[] fileArr = files.split(",");
      String[] finalArr = new String[fileArr.length];
      for (int i = 0; i < fileArr.length; i++) {
         String tmp = fileArr[i];
         String finalPath;
         Path path = new Path(tmp);
         URI pathURI = path.toUri();
         FileSystem localFs = FileSystem.getLocal(conf);
         if (pathURI.getScheme() == null) {
            //default to the local file system
            //check if the file exists or not first
            if (!localFs.exists(path)) {
               throw new FileNotFoundException("File " + tmp + " does not exist.");
            }
            finalPath = path.makeQualified(localFs).toString();
         } else {
            // check if the file exists in this file system
            // we need to recreate this filesystem object to copy
            // these files to the file system jobtracker is running
            // on.
            FileSystem fs = path.getFileSystem(conf);
            if (!fs.exists(path)) {
               throw new FileNotFoundException("File " + tmp + " does not exist.");
            }
            finalPath = path.makeQualified(fs).toString();
            try {
               fs.close();
            } catch (IOException e) {
            };
         }
         finalArr[i] = finalPath;
      }
      return StringUtils.arrayToString(finalArr);
   }
     
   public static int getReducerId(Reducer.Context context) {
      return context.getTaskAttemptID().getTaskID().getId();
   }

   public static enum MATCH_COUNTERS {

      MAPTASKSDONE,
      MAPTASKSTOTAL,
      REDUCETASKSDONE,
      REDUCETASKSTOTAL
   }

   public static void reduceReport(Reducer.Context context) {
      context.getCounter(MATCH_COUNTERS.REDUCETASKSDONE).increment(1);
      context.progress();
   }

}
