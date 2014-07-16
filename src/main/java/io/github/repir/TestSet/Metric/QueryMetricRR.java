package io.github.repir.TestSet.Metric;

import java.util.HashMap;
import io.github.repir.Retriever.Document;
import io.github.repir.Retriever.Query;
import io.github.repir.TestSet.TestSet;
import io.github.repir.tools.Lib.Log;

/**
 * Computes the Reciprocal Rank for a Query, which is 1 / rank of the first
 * relevant Document in the result list.
 * @author jer
 */
public class QueryMetricRR extends QueryMetric {

   public static Log log = new Log(QueryMetricRR.class);

   public QueryMetricRR() {
      super();
   }

   @Override
   public double calculate(TestSet testset, Query query) {
      HashMap<String, Integer> gold = testset.getQrels().get(testset.getQRelId(query)).relevance;
      int position = 1;
      double lastscore = 0;
      for (Document doc : query.getQueryResults()) {
         if (getRelevant(doc, gold) > 0) {
            return 1.0 / position;
         }
         position++;
      }
      return 0;
   }
   
   int getRelevant(Document doc, HashMap<String, Integer> gold) {
      String collectionid = doc.getCollectionID();
      Integer relevant = gold.get(collectionid);
      return (relevant == null) ? 0 : relevant;
   }


}
