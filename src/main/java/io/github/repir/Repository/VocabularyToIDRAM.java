package io.github.repir.Repository;

import io.github.htools.io.struct.StructuredFile;
import io.github.htools.lib.Log;

/**
 * This abstract class is to indicate that reading into memory is supported,
 * which is implemented by {@link VocMem3} and {@link VocMem4}. When the
 * Repository is requested to {@link Repository#tokenize}, it first looks for an
 * implementation of this class, which indicates that it can be read into memory
 * for fast reuse. If no such feature is present, it will look for and use a
 * {@link VocabularyToID} that is not read into memory.
 */
public abstract class VocabularyToIDRAM<F extends StructuredFile> extends VocabularyToID<F> {

   public static Log log = new Log(VocabularyToIDRAM.class);

   protected VocabularyToIDRAM(Repository repository) {
      super(repository);
   }
}
