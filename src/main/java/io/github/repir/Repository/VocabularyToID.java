package io.github.repir.Repository;

import io.github.repir.Extractor.EntityChannel;
import io.github.repir.tools.Structure.StructuredFile;
import io.github.repir.tools.Lib.Log;
import java.util.ArrayList;
import io.github.repir.tools.Lib.ArrayTools;

/**
 * Abstract class for looking up the TermID in a vocabulary based on the stemmed
 * term string. This adds {@link #getContent(Extractor.EntityChannel)} which is 
 * used by the standard extractor to convert an EntityChannel consisting of 
 * string tokens to an array of TermID's.
 * <p/>
 * @author jer
 * @param <F> 
 */
public abstract class VocabularyToID<F extends StructuredFile> extends StoredUnreportableFeature<F> implements DictionaryFeature {

   public static Log log = new Log(VocabularyToID.class);

   protected VocabularyToID(Repository repository) {
      super(repository);
   }

   public abstract int get(String term);
   
   public int[] getContent(EntityChannel dc) {
      ArrayList<Integer> r = new ArrayList<Integer>();
      int p = 0;
      if (dc != null) {
         for (String chunk : dc) {
            int termid = get(chunk.toString());
            if (termid >= 0) {
               r.add(termid);
            } else {
               //log.info("unknown word %s", chunk);
            }
         }
      }
      return ArrayTools.toIntArray(r);
   }

   public boolean exists(String term) {
      return get(term) >= 0;
   }

   
}
