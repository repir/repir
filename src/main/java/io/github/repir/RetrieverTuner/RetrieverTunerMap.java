package io.github.repir.RetrieverTuner;

import io.github.repir.RetrieverMulti.RetrieverMultiMap;
import io.github.repir.RetrieverMR.CollectorKey;
import io.github.repir.tools.Lib.Log;

/**
 * The mapper is generic, and collects data for a query request, using the
 * passed retrieval model, scoring function and query string. The common
 * approach is that each node processes all queries for one index partition. The
 * collected results are reshuffled to one reducer per query where all results
 * for a single query are aggregated.
 * <p/>
 * @author jeroen
 */
public class RetrieverTunerMap extends RetrieverMultiMap {

   public static Log log = new Log(RetrieverTunerMap.class);

   @Override
   public void changeCollectorKey( CollectorKey key ) {
     key.reducer = 0;
   }
   
}
