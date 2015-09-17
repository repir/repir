package io.github.repir.Strategy.ScoreFunction;

import io.github.repir.Repository.DocTF;
import io.github.repir.Repository.Repository;
import io.github.repir.Retriever.Document;
import io.github.repir.Strategy.Operator.Operator;
import io.github.htools.lib.Log;

/**
 * Implementation of Dirichlet smoothed Language Model estimate, as described by
 * Metzler and Croft (2005)
 * <p/>
 * mu is the Dirichlet prior, which in theory resembles the average length of
 * documents in the corpus, and can be modified by setting dlm.mu in the
 * configuration file.
 * <p/>
 * @author jeroen
 */
public class ScoreFunctionDirichletLM extends ScoreFunction<ScoreFunctionDirichletLM.Scorable> {

   public static Log log = new Log(ScoreFunctionDirichletLM.class);
   public int mu = 2500;
   DocTF doctf;

   public ScoreFunctionDirichletLM(Repository repository) {
      super(repository);
   }

   public void prepareRetrieve() {
      mu = repository.configuredInt("dlm.mu", 2500);
      //log.info("mu=%d", mu);
      doctf = DocTF.get(repository, "all");
      retrievalmodel.requestFeature(doctf);
   }

   @Override
   public Scorable createScorable(Operator feature) {
      return new Scorable(feature);
   }

   @Override
   public double score(Document doc) {
      score = 0;
      double alpha = mu / (double) (mu + doctf.getValue());
      for (Scorable scorable : scorables) {
         double frequency = scorable.feature.getFrequency();
         double featurescore = dlm(scorable, frequency, alpha);
         if (report) {
            if (Double.isNaN(score)) {
               doc.addReport("[%s] NaN ptc=%f\n", scorable.feature.toTermString(), scorable.ptc);
            } else {
               doc.addReport("[%s] w=%f freq=%f ptc=%f score=%f\n", scorable.feature.toTermString(), scorable.feature.getQueryWeight(), frequency, scorable.ptc, featurescore);
            }
         }
         if (!Double.isNaN(featurescore)) {
            score += featurescore;
         }
      }
      return score;
   }

   public double dlm(Scorable scorable, double frequency, double alpha) {
      return scorable.feature.getQueryWeight() * Math.log((1 - alpha) * frequency / doctf.getValue() + alpha * (scorable.ptc));
   }

   public class Scorable extends ScoreFunction.Scorable {

      double ptc;

      public Scorable(Operator feature) {
         super(feature);
         ptc = feature.getCF() / (double) repository.getCF();
      }
   }
}
