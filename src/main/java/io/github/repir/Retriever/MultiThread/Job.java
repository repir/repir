package io.github.repir.Retriever.MultiThread;

import io.github.repir.Retriever.Retriever;
import io.github.repir.tools.Lib.Log;
import java.io.IOException;

/**
 * Threaded extension of Job, which calls back the process when the job is done.
 * @author jer
 */
public class Job extends io.github.repir.Retriever.MapReduce.Job {

   public static Log log = new Log(Job.class);

   public Job(Retriever retriever) throws IOException {
      super(retriever);
   }
   
   public Job(Retriever retriever, String path) throws IOException {
      this(retriever);
      if (path != null) {
         this.path = path;
      }
   }
   
   public void startThreadedJob(JobCallback callback) {
      JobManager.get().startJob(this, callback);
   }
   
   public void complete(JobCallback callback) {
      try {
         if (this.isSuccessful()) {
            qi = getFinalQueueIterator(queue);
            callback.jobWasSuccesful(qi);
         } else {
            callback.JobFailed();
         }
      } catch (Exception ex) {
         log.fatalexception(ex, "complete()");
      }
   }
}
