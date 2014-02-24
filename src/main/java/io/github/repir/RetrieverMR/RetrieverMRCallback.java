package io.github.repir.RetrieverMR;
import java.util.ArrayList;
import io.github.repir.Repository.Repository;
import io.github.repir.Retriever.Query;

/**
 *
 * @author Jeroen Vuurens
 */
public interface RetrieverMRCallback {
   public void jobWasSuccesful( ArrayList<Query> queries );
   
   public void JobFailed( );
}
