package io.github.repir.Repository.Pig;

import io.github.htools.extract.Content;
import io.github.htools.hadoop.io.archivereader.RecordKey;
import io.github.htools.hadoop.io.archivereader.RecordValue;
import io.github.repir.Repository.*;
import io.github.repir.Retriever.Document;
import io.github.htools.io.Datafile;
import io.github.htools.io.struct.StructuredFileIntID;
import io.github.htools.io.struct.StructuredTextPig;
import io.github.htools.io.struct.StructuredTextPigTuple;
import io.github.htools.lib.PrintTools;
import java.util.HashMap;

/**
 * An abstract feature that can store a value per Document in the Repository.
 * This value can be accessed with an internal DocumentID passed through
 * {@link EntityStoredFeature#read(io.github.repir.Retriever.Document) }
 * @author jer
 * @param <F> a StructuredFileIntID file to store it's values, allowing 
 * the stored Record to be accessed through an internal integer ID
 * @param <C> The datatype stored
 */
public abstract class PigFeatureField<F extends StructuredTextPig, C extends StructuredTextPigTuple<? extends StructuredTextPig>> extends PigFeature<F,C> {

   public F file;
   
   public PigFeatureField(Repository repository, String field) {
      super(repository, field);
   }
}
