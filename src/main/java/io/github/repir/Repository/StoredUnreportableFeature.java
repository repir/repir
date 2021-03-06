package io.github.repir.Repository;

import io.github.htools.io.Datafile;
import io.github.htools.io.struct.StructuredFile;

/**
 * Atm, all StoredFeature types have to be content related and need to implement
 * lookups on an integer key and should optimize for fast sequential lookups, 
 * e.g. physically sort their data on ID. This class is for values that need to be
 * stored in the repository, that do not meet those requirements. They cannot as such
 * be handled by general RepIR processes on features, rather the 'user' must be aware of
 * their interface. In some cases, RepIR itself uses this type for system stored 
 * values such as PartitionLocation.
 * <p/>
 * These StoredUnreportableFeature can be announced in the config file, so RepIR will know
 * that they need to be constructed during extraction, and they can be accessed
 * through the Repository using {@link Repository#getFeature(java.lang.Class)}.
 * <p/>
 * Implementations are PartitionLocation, and Vocabulary instances (which lookup the
 * termID for a given String).
 * @author jeroen
 * @param <F> 
 */
public abstract class StoredUnreportableFeature<F extends StructuredFile> extends StoredFeature<F> {

   public F file;

   public StoredUnreportableFeature(Repository repository) {
      super( repository );
   }
   
   public StoredUnreportableFeature(Repository repository, String field) {
      super( repository, field );
   }
   
   public long getFilesize() {
      return (file == null)?0:file.getLength();
   }
   
   @Override
   public F getFile() {
      if (file == null)
         file = createFile(getStoredFeatureFile());
      return file;
   }

   @Override
   public void openRead() {
      getFile().openRead();
   }

   @Override
   public void closeRead() {
      getFile().closeRead();
      file = null;
   }

   public abstract F createFile(Datafile datafile);

   public void openWrite() {
      getFile().setBufferSize(1000000);
      file.openWrite();
   }

   public void closeWrite() {
      getFile().closeWrite();
      file = null;
   }

   @Override
   public void reuse() {
      throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
   }
}
