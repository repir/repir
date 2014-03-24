package io.github.repir.Repository;

import io.github.repir.EntityReader.MapReduce.TermEntityKey;
import io.github.repir.EntityReader.MapReduce.TermEntityValue;

/**
 * A general interface that features must implement, for creation with RR's general Repository
 * builder apps.Repository.Create.
 */
public interface ReducibleFeature {
   
   public void reduceInput(TermEntityKey key, Iterable<TermEntityValue> values);

   public void startReduce(int partition);

   public void finishReduce();
}
