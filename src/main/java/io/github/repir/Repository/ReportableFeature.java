package io.github.repir.Repository;

import io.github.repir.EntityReader.TermEntityKey;
import io.github.repir.EntityReader.TermEntityValue;
import io.github.repir.Retriever.Document;

public interface ReportableFeature<C> {
   
   public abstract void decode(Document d);

   public abstract void encode(Document d);

   public abstract void report(Document doc);
   
   public abstract C valueReported(Document doc);
   
   public abstract void setReportID(int id);

   public abstract int getReportID();
}
