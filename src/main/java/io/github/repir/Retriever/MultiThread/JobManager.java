package io.github.repir.Retriever.MultiThread;

import io.github.repir.Retriever.MultiThread.Job;
import io.github.repir.Retriever.MultiThread.JobCallback;
import java.io.IOException;
import java.util.ArrayList;
import io.github.repir.tools.Lib.Log;

/**
 * Manages multi-threaded retrieval jobs.
 * <p/>
 * Is not currently used, must be re-tested
 * @author Jeroen Vuurens
 */
public class JobManager extends Thread {

   public static Log log = new Log(JobManager.class);
   private static JobManager jobmanager;
   ArrayList<RunningJob> jobs = new ArrayList<RunningJob>();
   private boolean terminate = false;

   private JobManager() {
   }

   public static JobManager get() {
      if (jobmanager == null) {
         jobmanager = new JobManager();
         jobmanager.start();  
      }
      return jobmanager;
   }
   
   public static void shutdown() {
      if (jobmanager != null)
         jobmanager.terminate = true;
   }
   
   @Override
   public void run() {
      while (!terminate) {
         for (int i = jobs.size() - 1; i >= 0; i--){
            RunningJob rj = jobs.get(i);
            try {
               if (rj.job.isComplete()) {
                  jobs.remove(i);
                  rj.job.complete(rj.callback);
               }
            } catch (IOException ex) {
               log.exception(ex, "job.isComplete()");
            }
         }
         try {
            sleep(100);
         } catch (InterruptedException ex) {
            log.exception(ex, "sleep");
         }
      }
   }
   
   public int numberRunningJobs() {
      return jobs.size();
   }
   
   public Job getJob(int i) {
      return jobs.get(i).job;  
   }

   public void startJob(Job job, JobCallback callback) {
      job.startJob();
      jobs.add(new RunningJob(job, callback));
   }

   class RunningJob {

      Job job;
      JobCallback callback;

      public RunningJob(Job job, JobCallback callback) {
         this.job = job;
         this.callback = callback;
      }
   }
}
