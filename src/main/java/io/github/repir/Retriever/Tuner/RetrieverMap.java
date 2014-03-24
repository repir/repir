package io.github.repir.Retriever.Tuner;

import io.github.repir.Retriever.MapReduce.CollectorKey;
import io.github.repir.tools.Lib.Log;

/**
 * Force using a single reducer for tuning, to avoid concurrent writes.
 * <p/>
 * @author jeroen
 */
public class RetrieverMap extends io.github.repir.Retriever.Reusable.RetrieverMap {

   public static Log log = new Log(RetrieverMap.class);

   @Override
   public void changeCollectorKey( CollectorKey key ) {
     key.reducer = 0;
   }
   
}
