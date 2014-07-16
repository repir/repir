package io.github.repir.Retriever.MultiThread;
import java.util.ArrayList;
import io.github.repir.Repository.Repository;
import io.github.repir.Retriever.Query;

/**
 *
 * @author Jeroen Vuurens
 */
public interface JobThreadCallback {
   public void jobWasSuccesful( ArrayList<Query> queries );
   
   public void JobFailed( ); 
}
