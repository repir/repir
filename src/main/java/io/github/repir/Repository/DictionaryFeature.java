package io.github.repir.Repository;

import io.github.repir.EntityReader.TermEntityKey;
import io.github.repir.EntityReader.TermEntityValue;

public interface DictionaryFeature {

   public void reduceInput(int id, String term, long tf, long df);

   public void startReduce(long corpustermfreq, int corpusdocumentfrequency);

   public void finishReduce();
}
