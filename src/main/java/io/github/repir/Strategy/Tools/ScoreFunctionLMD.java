package io.github.repir.Strategy.Tools;

import io.github.repir.Retriever.Document;
import io.github.repir.tools.Lib.Log;
import io.github.repir.Repository.DocTF;
import io.github.repir.Repository.Repository;
import io.github.repir.Strategy.GraphNode;

/**
 * Implementation of Ponte & Croft language model Smoothed by a Dirichlet prior
 * mu.
 * <p/>
 * @author jeroen
 */
public class ScoreFunctionLMD extends ScoreFunction<ScoreFunctionLMD.Scorable> {

   public static Log log = new Log(ScoreFunctionLMD.class);
   public int mu = 2500;
   DocTF doctf;

   public ScoreFunctionLMD(Repository repository) {
      super(repository);
   }

   @Override
   public void prepareRetrieve() {
      mu = repository.getConfigurationInt("lmd.mu", 2500);
      doctf = (DocTF) retrievalmodel.requestFeature("DocTF:all");
   }

   @Override
   public double score(Document doc) {
      double lambda = mu / (double) (mu + doctf.getValue());
      score = 0;
      for (Scorable s : scorables) {
         double featurescore = 0;
         double frequency = s.feature.getFeatureValues().frequency;
         featurescore = score(s, lambda, frequency);
         if (featurescore != 0) {
            doc.addReport("\n[%s] freq=%f ptc=%f score=%f", s.feature.toTermString(), frequency, s.ptc, featurescore);
            score += featurescore;
         } else {
            if (Double.isNaN(score)) {
               doc.addReport("\n[%s] NaN ptc=%f", s.feature.toTermString(), s.ptc);
            }
         }
      }
      return score;
   }

   public double score(Scorable s, double lambda, double frequency) {
      return s.queryweight * Math.log((lambda * frequency) / doctf.getValue() + (1 - lambda) * s.ptc);

   }

   @Override
   public Scorable create(GraphNode feature) {
      return new Scorable(feature);
   }

   public class Scorable extends ScoreFunction.Scorable {

      double ptc;
      double queryweight;

      public Scorable(GraphNode feature) {
         super(feature);
         ptc = feature.getFeatureValues().corpusfrequency / (double) repository.getCorpusTF();
         queryweight = feature.getFeatureValues().queryweight;
      }
   }
}
