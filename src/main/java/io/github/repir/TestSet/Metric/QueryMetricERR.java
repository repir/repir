package io.github.repir.TestSet.Metric;

import java.util.HashMap;
import io.github.repir.Retriever.Document;
import io.github.repir.Retriever.Query;
import io.github.repir.TestSet.TestSet;
import io.github.htools.lib.Log;
import java.io.IOException;

/**
 * Computes the Expected Reciprocal Rank.
 * @author jer
 */
public class QueryMetricERR extends QueryMetric {

   public static Log log = new Log(QueryMetricERR.class);

   public QueryMetricERR(int rank) {
      super(rank);
   }

   @Override
   public double calculate(TestSet testset, Query query) throws IOException {
      HashMap<String, Integer> relevancejudgments = testset.getQrels().get(testset.getQRelId(query)).relevance;
      int maxrel = 0;
      for (int i : relevancejudgments.values())
         maxrel = Math.max(maxrel, i);
      int pos = 1;
      double score = 0;
      double notsatisfied = 1;
      for (Document doc : query.getQueryResults()) {
         int rel = getRelevant(doc, relevancejudgments);
         if ( rel > 0) {
            double rr = (Math.pow(2, rel) - 1) / Math.pow(2, maxrel);
            score += notsatisfied * rr / pos;
            notsatisfied *= (1 - rr);
         }
         if (pos++ > rank)
            break;
      }
      return score;
   }
   
   int getRelevant(Document doc, HashMap<String, Integer> gold) {
      String collectionid = doc.getCollectionID();
      Integer relevant = gold.get(collectionid);
      return (relevant == null) ? 0 : relevant;
   }


}
