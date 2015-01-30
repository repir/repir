package io.github.repir.Strategy;
import io.github.repir.Retriever.Retriever;
import io.github.repir.tools.lib.Log; 

/**
 * Is a cross-over between a {@link RetrievalModel} and an {@link Analyze}, which
 * does construct a {@link GraphRoot} with {@link GraphNode}s to perform a 
 * RetrievalModel like process, but does not result in a list of ranked
 * {@link Document}s, but rather uses a collector that will store alternative
 * results in the Repository. The only reason to wrap the {@link Strategy} as 
 * a {@link RetrievalModelAnalyze} instead of a {@link RetrievalModel} is that
 * the {@link Retriever} will not iterate retrieval until a list of ranked 
 * documents is returned.
 * @author Jeroen Vuurens
 */
public class RetrievalModelAnalyze extends RetrievalModel {
  public static Log log = new Log( RetrievalModelAnalyze.class ); 

  public RetrievalModelAnalyze(Retriever retriever) {
      super(retriever);
   }

}
