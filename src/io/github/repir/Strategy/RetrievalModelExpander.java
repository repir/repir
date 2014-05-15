package io.github.repir.Strategy;
import io.github.repir.Retriever.Query;
import io.github.repir.Retriever.Retriever;
import io.github.repir.tools.Lib.Log; 

/**
 * A RetrievalModel that reformulates it query by implementing {@link #getQueryToRetrieve() }
 * and optionally override {@link #postReform() }, which by default switches the
 * {@link RetrievalModel} to the default {@link RetrievalModel}.
 * @author Jeroen Vuurens
 */
public abstract class RetrievalModelExpander extends RetrievalModel {
  public static Log log = new Log( RetrievalModelExpander.class );
  String expandedquery;

   public RetrievalModelExpander(Retriever retriever) {
      super(retriever);
   }
   
  @Override
   public final String getQueryToRetrieve() {
      if (expandedquery == null)
         expandedquery = expandQuery();
      return expandedquery;
   }
   
   public abstract String expandQuery();
   
   @Override
   public Query postReform() {
      Query q = super.postReform();
      q.setStrategyClassname(RetrievalModel.class.getSimpleName());
      return q;
   }
}
