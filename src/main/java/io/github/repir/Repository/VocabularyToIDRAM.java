package io.github.repir.Repository;

import io.github.repir.tools.Content.RecordBinary;
import io.github.repir.tools.Lib.Log;

public abstract class VocabularyToIDRAM<F extends RecordBinary> extends VocabularyToID<F> {

   public static Log log = new Log(VocabularyToIDRAM.class);

   protected VocabularyToIDRAM(Repository repository) {
      super(repository);
   }
}
