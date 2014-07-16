package io.github.repir.TestSet;

import io.github.repir.Retriever.Query;
import java.util.ArrayList;
import java.util.Collection;

/**
 * File Structure to store the results of a {@link TestSet} in a text file.
 * @author jer
 */
public interface ResultFile {
   /**
    * @return Collection of {@link Query}s that are specified in the {@link TestSet}, 
    * containing the results stored in the given file. 
    */
   public ArrayList<Query> getResults();

   /**
    * Stores the results in the list of {@link Query}s, in the file.
    * @param results 
    */
   public void writeresults(ArrayList<Query> results);
}
