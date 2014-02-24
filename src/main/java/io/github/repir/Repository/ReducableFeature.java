package io.github.repir.Repository;

import io.github.repir.EntityReader.TermEntityKey;
import io.github.repir.EntityReader.TermEntityValue;

/**
 * 
 */
public interface ReducableFeature {
   
   public void reduceInput(TermEntityKey key, Iterable<TermEntityValue> values);

   public void startReduce(int partition);

   public void finishReduce();
}
