package io.github.repir.Strategy.Collector;
import io.github.repir.Retriever.Document;
import io.github.repir.Strategy.Strategy;
import io.github.repir.tools.Content.RecordHeaderDataRecord;
import io.github.repir.tools.Lib.Log;

public abstract class CollectorAnalyzer<R extends RecordHeaderDataRecord> extends CollectorCachable<R> {
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
