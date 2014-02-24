package io.github.repir.Repository;

import io.github.repir.Extractor.EntityAttribute;
import io.github.repir.tools.Content.RecordBinary;
import io.github.repir.tools.Lib.Log;
import java.util.ArrayList;
import io.github.repir.tools.DataTypes.ByteArrayPos;
import io.github.repir.tools.Lib.ArrayTools;

public abstract class VocabularyToID<F extends RecordBinary> extends StoredUnreportableFeature<F> implements DictionaryFeature {

   public static Log log = new Log(VocabularyToID.class);

   protected VocabularyToID(Repository repository) {
      super(repository);
   }

   public abstract int get(String term);
   
   public int[] getContent(EntityAttribute dc) {
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
