package io.github.repir.Strategy;
import io.github.repir.Retriever.Query;
import io.github.repir.Retriever.Retriever;
import io.github.repir.tools.Lib.Log; 

/**
 *
 * @author Jeroen Vuurens
 */
public class RetrievalModelExpander extends RetrievalModel {
  public static Log log = new Log( RetrievalModelExpander.class ); 

   public RetrievalModelExpander(Retriever retriever) {
      super(retriever);
   }
   
   @Override
   public Query postReform() {
      Query q = super.postReform();
      q.setStrategyClass(RetrievalModel.class.getSimpleName());
      return q;
   }
}
