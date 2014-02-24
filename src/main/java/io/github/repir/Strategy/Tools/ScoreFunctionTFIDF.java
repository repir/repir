package io.github.repir.Strategy.Tools;

import io.github.repir.Repository.Repository;
import io.github.repir.Retriever.Document;
import io.github.repir.Strategy.GraphNode;
import io.github.repir.tools.Lib.Log;
import io.github.repir.Strategy.Tools.ScoreFunctionTFIDF.Scorable;

/**
 * Scores documents according to frequency*idf
 * <p/>
 * @author jeroen
 */
public class ScoreFunctionTFIDF extends ScoreFunction<Scorable> {

   public static Log log = new Log(ScoreFunctionTFIDF.class);

   public ScoreFunctionTFIDF(Repository repository) {
      super(repository);
   }

   @Override
   public Scorable create(GraphNode feature) {
      return new Scorable( feature );
   }

   @Override
   public double score(Document doc) {
      score = 0;
      for (Scorable s : scorables) {
         score += s.idf * s.feature.getFeatureValues().frequency;
      }
      return score;
   }

   @Override
   public void prepareRetrieve() {
   }

   class Scorable extends ScoreFunction.Scorable {
      double idf;
      double queryweight;
      
      public Scorable( GraphNode feature ) {
         super( feature );
         this.queryweight = feature.getFeatureValues().queryweight;
         idf = Math.log( repository.getDocumentCount() / (double) feature.getFeatureValues().documentfrequency );
      }
   }
}
