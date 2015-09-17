package io.github.repir.Repository;

import io.github.htools.hadoop.io.archivereader.RecordKey;
import io.github.repir.Strategy.GraphRoot;
import io.github.repir.Strategy.Strategy;
import io.github.repir.Retriever.Document;

/**
 * Family of features that are not stored, but only exist during a retrieval job,
 * and can finally be reported per Document. A typical example is a ScoreFunction,
 * which calculates a score for a Document for ranking.
 * @author jer
 * @param <C> 
 */
public abstract class ReportedUnstoredFeature<C> extends Feature implements ReportableFeature {
   protected Strategy strategy;
   
   public ReportedUnstoredFeature( Repository repository ) {
      super( repository );
   }
   
   public void prepareRetrieval( Strategy rm ) {
      this.strategy = rm;
   }
}
