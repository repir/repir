package io.github.repir.Strategy.Collector;
import io.github.repir.Retriever.Document;
import io.github.repir.Strategy.Strategy;
import io.github.repir.tools.Content.StructuredFileKeyValueRecord;
import io.github.repir.tools.Lib.Log;

/**
 * An abstract for {@link Collector}s that are not used by {@link RetrievalModel}s
 * but rather by custom designed {@link Strategy}s, therefore not necessarily retrieving
 * {@link Document}s. This Class assumes the collected results will be cached
 * in a {@link StoredDynamicFeature}, and must specify the Record structure used to
 * write the data as a generic parameter and implement {@link #getStoredDynamicFeature()}
 * to provide the feature used.
 * @author jer
 * @param <R> 
 */
public abstract class CollectorAnalyzer<R extends StructuredFileKeyValueRecord> extends CollectorCachable<R> {
   public static Log log = new Log(CollectorAnalyzer.class);
   
   public CollectorAnalyzer( ) {
   }
   
   public CollectorAnalyzer (Strategy rm) {
      super( rm );
   }

   @Override
   public final boolean reduceInQuery() {
      return false;
   }

   @Override
   public final void setCollectedResults() {
   }

   @Override
   public final void prepareRetrieval() {
   }

   @Override
   protected final void collectDocument(Document doc) {
      throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
   }

   @Override
   public final void decode() {
      throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
   }
}
