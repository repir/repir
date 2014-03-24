package io.github.repir.Strategy.ScoreFunction;

import io.github.repir.Repository.DocTF;
import io.github.repir.Repository.Repository;
import io.github.repir.Retriever.Document;
import io.github.repir.Strategy.Operator.OperatorValues;
import io.github.repir.Strategy.Operator.Operator;
import io.github.repir.tools.Lib.Log;
import io.github.repir.Strategy.ScoreFunction.ScoreFunctionKLD.Scorable;

/**
 * Implementation of Zhai and Lafferty's (2001) algorithm to minimize
 * KL-divergence between a query language model and a Dirichlet smoothed
 * document language model.
 * <p/>
 * Setting:
 * kld.mu=int controls the dirichlet prior
 * kld.report=boolean whether the score per feature is reported for each document (for inspection)
 * <p/>
 * @author jeroen
 */
public class ScoreFunctionKLD extends ScoreFunction<Scorable> {

   public static Log log = new Log(ScoreFunctionKLD.class);
   public int mu = 2500;
   DocTF doctf;

   public ScoreFunctionKLD(Repository repository) {
      super(repository);
   }

   public double scoreToCorpusWeight(OperatorValues values, double total) {
      return 1.0 / (mu * (Math.pow(2, total) - 1));
   }

   @Override
   public void prepareRetrieve() {
      mu = repository.getConfigurationInt("kld.mu", 2500);
      documentpriorweight = 0;
      for (Scorable scorable : scorables) {
         documentpriorweight += scorable.feature.getFeatureValues().documentprior;
      }
      doctf = (DocTF) retrievalmodel.requestFeature(DocTF.class, "all");
   }

   public double ptc(Operator feature) {
      return feature.getFeatureValues().cf / (double) repository.getCF();
   }

   public double documentPrior(Document doc) {
      //log.info("doctf %d %d", doc.docid, doc.tf);
      return documentpriorweight * Math.log((mu / (double) (mu + doctf.getValue())));
   }

   @Override
   public double score(Document doc) {
      score = 0;
      for (Scorable scorable : scorables) {
         double featurescore = 0;
         double frequency = scorable.feature.getFeatureValues().frequency;
         if (frequency > 0) {
            featurescore = kld(scorable, frequency);
         } else if (scorable.feature.getFeatureValues().frequencylist != null) {
            for (double f : scorable.feature.getFeatureValues().frequencylist) {
               featurescore += kld(scorable, f);
               frequency += f;
            }
         }
         if (report) {
            if (featurescore > 0) {
               doc.addReport("[%s] freq=%e ptc=%e score=%f\n", scorable.feature.toTermString(), frequency, scorable.ptc, featurescore);
            } else if (Double.isNaN(score)) {
               doc.addReport("[%s] NaN ptc=%f\n", scorable.feature.toTermString(), scorable.ptc);
            }
         }
         score += featurescore;
      }
//      if (score != 0)
      score += documentPrior(doc);
      return score;
   }

   public double kld(Scorable scorable, double frequency) {
      return scorable.queryweight * Math.log(1 + frequency / (mu * scorable.ptc));
   }

   @Override
   public Scorable createScorable(Operator feature) {
      return new Scorable(feature);
   }

   public class Scorable extends ScoreFunction.Scorable {

      double queryweight;
      double ptc; // P(t|C)

      public Scorable(Operator feature) {
         super(feature);
         this.queryweight = feature.getFeatureValues().queryweight;
         this.ptc = ptc(feature);
      }
   }
}
