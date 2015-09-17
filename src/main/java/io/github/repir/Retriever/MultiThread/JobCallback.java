package io.github.repir.Retriever.MultiThread;
import io.github.repir.Retriever.MapReduce.QueueIterator;
import io.github.htools.lib.Log; 

/**
 * 
 * @author Jeroen Vuurens
 */
public interface JobCallback {
   public void jobWasSuccesful( QueueIterator qi );
   
   public void JobFailed();
}
