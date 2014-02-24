package io.github.repir.RetrieverMR;
import io.github.repir.tools.Lib.Log; 

/**
 *
 * @author Jeroen Vuurens
 */
public interface IRHDJobCallback {
   public void jobWasSuccesful( QueueIterator qi );
   
   public void JobFailed();
}
