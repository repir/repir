package io.github.repir.Strategy.Operator;

import io.github.repir.Repository.Repository;
import io.github.repir.Strategy.GraphRoot;
import io.github.htools.lib.Log;
import java.util.ArrayList;

/**
 * A CachableOperator is a node that requires additional data to the data that
 * can usually be obtained from a unigram index. E.g. a phrase or synonym can be
 * implemented as a function that needs the collection statistics for the
 * feature. For these types of features there are two possibilities, (1) either
 * they return true on their needsCollect() function, use the pre-pass to obtain
 * the required statitistics and reformulate the query so that it contains this
 * data on the next pass, or (2) use a StoredDynamicFeature to store the
 * obtained data, which allows it to reuse this if needed again. Case (1) is
 * supported by default, for case (2) the feature needs to extend this class.
 * <p/>
 */
public abstract class CachableOperator extends Operator {

   protected CachableOperator(GraphRoot root, ArrayList<Operator> terms) {
      super(root, terms);
   }

   protected CachableOperator(Repository repository) {
      super(repository);
   }

   @Override
   public void readStatistics() {
      if (needsCollect()) {
         readCachedData();
         if (needsCollect()) {
            super.announce(ANNOUNCEKEY.NEEDSCACHECOLLECT, this);
         } else {
            if (cf == 0 && root.removenonexist) {
               root.remove(this);
            }
         }
      }
   }

   public abstract void readCachedData();
}
