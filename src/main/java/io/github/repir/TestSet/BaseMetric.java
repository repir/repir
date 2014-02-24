package io.github.repir.TestSet;

import io.github.repir.Repository.DocLiteral;
import io.github.repir.Retriever.Document;
import io.github.repir.Retriever.Query;
import io.github.repir.tools.Lib.Log;
import java.util.HashMap;

public abstract class BaseMetric {

   public static Log log = new Log(BaseMetric.class);
   public int totalrelevant;
   public int total;

   public abstract double calcAfter(int pos, double previousmetric, int relevant);

   public double normalize(int position, double metric) {
      return metric;
   }

   public abstract String getName();

   public double[] calcTable(Query query, HashMap<String, Integer> gold) {
      setTotals(query, gold);
      double metric[] = new double[query.queryresults.length];
      int position = 0;
      double lastscore = 0;
      for (Document doc : query.queryresults) {
         lastscore = calcAfter(position, lastscore, getRelevant(doc, gold));
         metric[ position++] = normalize(position, lastscore);
      }
      return metric;
   }

   int getRelevant(Document doc, HashMap<String, Integer> gold) {
      String collectionid = doc.retrievalmodel.repository.getCollectionIDFeature().valueReported(doc);
      Integer relevant = gold.get(collectionid);
      return (relevant == null) ? 0 : relevant;
   }

   public void setTotals(Query query, HashMap<String, Integer> gold) {
      totalrelevant = 0;
      total = query.queryresults.length;
      for (Integer i : gold.values()) {
         if (i > 0) {
            totalrelevant++;
         }
      }
      if (totalrelevant == 0)
         log.info("warning: 0 relevant documents for query %d", query.id);
      //log.info("total %d relevant %d", this.total, this.totalrelevant);
   }
}
