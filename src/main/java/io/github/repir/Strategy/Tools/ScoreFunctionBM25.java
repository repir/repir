package io.github.repir.Strategy.Tools;

import io.github.repir.Repository.Repository;
import io.github.repir.Retriever.Document;
import io.github.repir.tools.Lib.Log;
import io.github.repir.Repository.DocTF;
import io.github.repir.Strategy.GraphNode;
import io.github.repir.Strategy.Tools.ScoreFunctionBM25.Scorable;

/**
 * Scores documents according to frequency*idf
 * <p/>
 * @author jeroen
 */
public class ScoreFunctionBM25 extends ScoreFunction<Scorable> {

   public static Log log = new Log(ScoreFunctionBM25.class);
   DocTF doctf;
   double avgdoclength;
   double k;
   double b;

   public ScoreFunctionBM25(Repository repository) {
      super(repository);
   }

   @Override
   public void prepareRetrieve() {
      b = repository.getConfigurationDouble("bm25.b", 0.75);
      k = repository.getConfigurationDouble("bm25.k", 1.2);
      documentpriorweight = 0;
      doctf = (DocTF) retrievalmodel.requestFeature("DocTF:all");
      avgdoclength = repository.getCorpusTF() / (double) repository.getDocumentCount();
   }

   public double idf(GraphNode node) {
      return Math.log((repository.getDocumentCount() - node.getFeatureValues().documentfrequency + 0.5)
              / (node.getFeatureValues().documentfrequency + 0.5));
   }

   @Override
   public double score(Document doc) {
      score = 0;
      for (Scorable s : scorables) {
         double frequency = s.feature.getFeatureValues().frequency;
         double featurescore = s.idf * (frequency * (k + 1)) / (frequency + k * (1 - b + b * doctf.getValue() / avgdoclength));
         if (featurescore >= 0) {
            score += featurescore;
         }
         if (report) {
            if (featurescore >= 0) {
               doc.addReport("[%s] freq=%f idf=%f score=%f", s.feature.toTermString(), frequency, s.idf, featurescore);
            } else {
               if (Double.isNaN(score)) {
                  doc.addReport("[%s] NaN idf=%f", s.feature.toTermString(), s.idf);
               }
            }
         }
      }
      return score;
   }

   @Override
   public Scorable create(GraphNode feature) {
      return new Scorable(feature);
   }

   public class Scorable extends ScoreFunction.Scorable {

      double idf;
      double queryweight;

      public Scorable(GraphNode feature) {
         super(feature);
         idf = idf(feature);
         queryweight = feature.getFeatureValues().queryweight;
      }
   }
}
