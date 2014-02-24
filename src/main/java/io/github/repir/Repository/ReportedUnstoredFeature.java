package io.github.repir.Repository;

import io.github.repir.EntityReader.TermEntityKey;
import io.github.repir.Strategy.GraphRoot;
import io.github.repir.Strategy.Strategy;
import io.github.repir.Retriever.Document;

public  class ReportedUnstoredFeature<C> extends Feature implements ReportableFeature {
   protected Strategy strategy;
   public int reportid;
   
   public ReportedUnstoredFeature( Repository repository ) {
      super( repository );
   }
   
   public void prepareRetrieval( Strategy rm ) {
      this.strategy = rm;
   }

   @Override
   public void decode(Document d) {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   @Override
   public void encode(Document d) {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   @Override
   public void report(Document doc) {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   @Override
   public Object valueReported(Document doc) {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   @Override
   public void setReportID(int id) {
      this.reportid = id;
   }

   @Override
   public int getReportID() {
      return this.reportid;
   }
}
