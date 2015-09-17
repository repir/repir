package io.github.repir.Strategy.ScoreFunction;

import io.github.repir.Repository.Repository;
import io.github.repir.Retriever.Document;
import io.github.htools.lib.Log;
import io.github.repir.Repository.DocTF;
import io.github.repir.Strategy.Operator.Operator;
import io.github.repir.Strategy.ScoreFunction.ScoreFunctionBM25.Scorable;

/**
 * Implementation of BM25
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
      b = repository.configuredDouble("bm25.b", 0.75);
      k = repository.configuredDouble("bm25.k", 1.2);
      documentpriorweight = 0;
      doctf = DocTF.get(repository, "all");
      retrievalmodel.requestFeature(doctf);
      avgdoclength = repository.getCF() / (double) repository.getDocumentCount();
   }

   public double idf(Operator node) {
      return Math.log((repository.getDocumentCount() - node.getDF() + 0.5)
              / (node.getDF() + 0.5));
   }

   @Override
   public double score(Document doc) {
      score = 0;
      for (Scorable s : scorables) {
         double frequency = s.feature.getFrequency();
         double featurescore = s.feature.getQueryWeight() * s.idf * (frequency * (k + 1)) / (frequency + k * (1 - b + b * doctf.getValue() / avgdoclength));
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
   public Scorable createScorable(Operator feature) {
      return new Scorable(feature);
   }

   public class Scorable extends ScoreFunction.Scorable {

      double idf;

      public Scorable(Operator feature) {
         super(feature);
         idf = idf(feature);
      }
   }
}
