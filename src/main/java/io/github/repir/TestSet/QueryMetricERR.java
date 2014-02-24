package io.github.repir.TestSet;

import java.util.HashMap;
import io.github.repir.Retriever.Document;
import io.github.repir.Retriever.Query;
import io.github.repir.tools.Lib.Log;

public class QueryMetricERR extends QueryMetric {

   public static Log log = new Log(QueryMetricERR.class);

   public QueryMetricERR(int position) {
      super(position);
   }

   @Override
   public double calculate(TestSet testset, Query query) {
      HashMap<String, Integer> gold = testset.getQrels().get(query.id);
      int maxrel = 0;
      for (int i : gold.values())
         maxrel = Math.max(maxrel, i);
      int pos = 1;
      double score = 0;
      double notsatisfied = 1;
      for (Document doc : query.queryresults) {
         int rel = getRelevant(doc, gold);
         if ( rel > 0) {
            double rr = (Math.pow(2, rel) - 1) / Math.pow(2, maxrel);
            score += notsatisfied * rr / pos;
            notsatisfied *= (1 - rr);
         }
         if (pos++ > position)
            break;
      }
      return score;
   }
   
   int getRelevant(Document doc, HashMap<String, Integer> gold) {
      String collectionid = doc.retrievalmodel.repository.getCollectionIDFeature().valueReported(doc);
      Integer relevant = gold.get(collectionid);
      return (relevant == null) ? 0 : relevant;
   }


}
