package io.github.repir.Repository.Pig;

import io.github.repir.Extractor.Entity;
import io.github.repir.EntityReader.MapReduce.TermEntityKey;
import io.github.repir.EntityReader.MapReduce.TermEntityValue;
import io.github.repir.Repository.*;
import io.github.repir.Retriever.Document;
import io.github.repir.tools.Content.Datafile;
import io.github.repir.tools.Structure.StructuredFileIntID;
import io.github.repir.tools.Structure.StructuredTextPig;
import io.github.repir.tools.Structure.StructuredTextPigTuple;
import io.github.repir.tools.Lib.PrintTools;
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
