package io.github.repir.Repository;

import java.io.EOFException;
import io.github.repir.tools.Content.Datafile;
import io.github.repir.tools.Content.RecordBinary;
import io.github.repir.tools.Lib.Log;

/**
 * Generic class for Features that are stored in the repository. Implementations 
 * must declare a RecordIdentity file, (usually an extension of RecordBinary that 
 * ensures records have a unique ID (int)). For performance, the features that are merged with other
 * features should be stored physically sorted on ID. The second declaration is a data 
 * type, which can be complex.
 * @author jeroen
 * @param <F> FileType that extends RecordIdentity
 * @param <C> Data type of the feature
 */
public abstract class StoredFeature<F> extends Feature {

   public static Log log = new Log(StoredFeature.class);
   
   public StoredFeature(Repository repository) {
      super( repository  );
   }
   
   public abstract F getFile();

   public abstract void openRead();

   public abstract void closeRead();

   public abstract void setBufferSize(int size);
   
   public abstract void readResident();
   
   public void writeCache() {}
   
   public abstract boolean isReadResident();
   
   public abstract void reuse();
}
