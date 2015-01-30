package io.github.repir.TestSet.Metric;

import io.github.repir.Retriever.Document;
import io.github.repir.Retriever.Query;
import io.github.repir.tools.lib.Log;
import java.util.HashMap;

/**
 * Abstract class for the computation of IR metrics, for the results on a {@link Query}
 * given a set of relevance judgments.
 * @author jer
 */
public abstract class BaseMetric {

   public static Log log = new Log(BaseMetric.class);
   public int totalrelevant;
   public int total;

   /**
    * This method will be called in positional order of the documents, an can therefore
    * be implemented as a cumulative function. 
    * @param position the position to score the metric for
    * @param previousmetric the metric at the previous position, or 0 for position 0
    * @param relevant the relevance score of the document at this position
    * @return 
    */
   public abstract double metricAtPosition(int position, double previousmetric, int relevant);

   /**
    * Can be overridden, to normalize the metrics output.
    * @return the normalized metric value for the given position.
    */
   public double normalize(int position, double metric) {
      return metric;
   }

   /**
    * @return a table of metrics, per position, using the {@link Document}s in the
    * result set of the given {@link Query} and the given set of relevance judgments.
    * The key of the Map of relevance judgments, and the first ReportedFeature
    * in the Documents must both be a CollectionIDs.
    */
   public double[] metricTable(Query query, HashMap<String, Integer> relevancejudgments) {
      setTotals(query, relevancejudgments);
      double metric[] = new double[query.getQueryResults().length];
      int position = 0;
      double lastscore = 0;
      for (Document doc : query.getQueryResults()) {
         lastscore = metricAtPosition(position, lastscore, getRelevant(doc, relevancejudgments));
         metric[ position++] = normalize(position, lastscore);
      }
      return metric;
   }

   protected int getRelevant(Document doc, HashMap<String, Integer> gold) {
      String collectionid = doc.getCollectionID();
      Integer relevant = gold.get(collectionid);
      return (relevant == null) ? 0 : relevant;
   }

   protected void setTotals(Query query, HashMap<String, Integer> gold) {
      totalrelevant = 0;
      total = query.getQueryResults().length;
      for (Integer i : gold.values()) {
         if (i > 0) {
            totalrelevant++;
         }
      }
      if (totalrelevant == 0)
         log.info("warning: 0 relevant documents for query %d", query.id);
   }
}
