package io.github.repir.TestSet;

import java.util.HashMap;
import io.github.repir.Retriever.Document;
import io.github.repir.Retriever.Query;
import io.github.repir.tools.Lib.Log;

public class QueryMetricMRR extends QueryMetric {

   public static Log log = new Log(QueryMetricMRR.class);

   public QueryMetricMRR() {
      super();
   }

   @Override
   public double calculate(TestSet testset, Query query) {
      HashMap<String, Integer> gold = testset.getQrels().get(query.id);
      int position = 1;
      double lastscore = 0;
      for (Document doc : query.queryresults) {
         if (getRelevant(doc, gold) > 0) {
            return 1.0 / position;
         }
         position++;
      }
      return 0;
   }
   
   int getRelevant(Document doc, HashMap<String, Integer> gold) {
      String collectionid = doc.retrievalmodel.repository.getCollectionIDFeature().valueReported(doc);
      Integer relevant = gold.get(collectionid);
      return (relevant == null) ? 0 : relevant;
   }


}
