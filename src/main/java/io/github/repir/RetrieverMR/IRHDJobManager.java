package io.github.repir.RetrieverMR;

import java.io.IOException;
import java.util.ArrayList;
import io.github.repir.tools.Lib.Log;

/**
 *
 * @author Jeroen Vuurens
 */
public class IRHDJobManager extends Thread {

   public static Log log = new Log(IRHDJobManager.class);
   private static IRHDJobManager jobmanager;
   ArrayList<RunningJob> jobs = new ArrayList<RunningJob>();
   private boolean terminate = false;

   private IRHDJobManager() {
   }

   public static IRHDJobManager get() {
      if (jobmanager == null) {
         jobmanager = new IRHDJobManager();
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
   
   public IRHDJob getJob(int i) {
      return jobs.get(i).job;  
   }

   public void startJob(IRHDJob job, IRHDJobCallback callback) {
      job.startJob();
      jobs.add(new RunningJob(job, callback));
   }

   class RunningJob {

      IRHDJob job;
      IRHDJobCallback callback;

      public RunningJob(IRHDJob job, IRHDJobCallback callback) {
         this.job = job;
         this.callback = callback;
      }
   }
}
