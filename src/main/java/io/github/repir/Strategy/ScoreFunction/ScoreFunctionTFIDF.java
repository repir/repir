package io.github.repir.Strategy.ScoreFunction;

import io.github.repir.Repository.Repository;
import io.github.repir.Retriever.Document;
import io.github.repir.Strategy.Operator.Operator;
import io.github.repir.tools.lib.Log;
import io.github.repir.Strategy.ScoreFunction.ScoreFunctionTFIDF.Scorable;

/**
 * Scores documents according to TF*IDF
 * <p/>
 * @author jeroen
 */
public class ScoreFunctionTFIDF extends ScoreFunction<Scorable> {

   public static Log log = new Log(ScoreFunctionTFIDF.class);

   public ScoreFunctionTFIDF(Repository repository) {
      super(repository);
   }

   @Override
   public Scorable createScorable(Operator feature) {
      return new Scorable( feature );
   }

   @Override
   public double score(Document doc) {
      score = 0;
      for (Scorable s : scorables) {
         score += s.idf * s.feature.getFrequency();
      }
      return score;
   }

   @Override
   public void prepareRetrieve() {
   }

   class Scorable extends ScoreFunction.Scorable {
      double idf;
      double queryweight;
      
      public Scorable( Operator feature ) {
         super( feature );
         this.queryweight = feature.getQueryWeight();
         idf = Math.log( repository.getDocumentCount() / (double) feature.getDF() );
      }
   }
}
