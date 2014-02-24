package io.github.repir.Repository;

import java.util.Collection;
import io.github.repir.tools.Content.Datafile;
import io.github.repir.tools.Content.RecordHeaderDataRecord;
import io.github.repir.tools.Content.RecordHeaderInterface;
import io.github.repir.tools.Lib.Log;

/**
 * Generic class for Features that are not indexed, but can be the result of
 * analysis on the corpus and then stored in the repository for later use.
 * Implementations typically store every feature value in a different file, thus
 * omitting the necessity for a RecordIdenty file. must declare a RecordIdentity
 * file, (usually an extension of RecordBinary that ensures records have a
 * unique ID (int)). For performance, the features that are merged with other
 * features should be stored physically sorted on ID. The second declaration is
 * a data type, which can be complex.
 * <p/>
 * if the configuration contains <classsimplename>.suffix (all lowercase!) then
 * the file used is append with "." and the suffix to allow to use a different version
 * of a file.
 * @author jeroen
 * @param <F> FileType that extends RecordIdentity
 * @param <C> Data type of the feature
 */
public abstract class StoredDynamicFeature<F extends RecordHeaderInterface<R>, R extends RecordHeaderDataRecord> extends StoredFeature<F> {

   public static Log log = new Log(StoredDynamicFeature.class);
   private F file;
   int recordcount = 0;

   public StoredDynamicFeature(Repository repository) {
      super(repository);
   }
   
   @Override
   public int hashCode() {
      return getClass().getCanonicalName().hashCode();
   }

   @Override
   public boolean equals( Object o ) {
      return getClass().getCanonicalName().equals(o.getClass().getCanonicalName());
   }

   public Collection<R> getKeys() {
      return getFile().getKeys();
   }
   
   public F getFile() {
      if (file == null)
         file = createFile();
      return file;
   }
   
   public void setBufferSize(int size) {
      getFile().setBufferSize(size);
   }

   public int getBufferSize() {
      return getFile().getBufferSize();
   }
   
   @Override
   public void readResident() {
      log.fatal("readResident not yet implemented");
   }

   public boolean isReadResident() {
      return false;
   }

   public void reuse() {
      log.fatal("reuse not yet implemented");
   }

   public void remove( Iterable<R> records ) {
      getFile().remove(records);
   }
   
   protected F createFile() {
      Datafile df = repository.getTempFeatureFile(this);
      return createFile(df);
   }
   
   public abstract F createFile(Datafile df);
   
   public R newRecord() {
      return getFile().newRecord();
   }
   
   public R find( R r ) {
      getFile().openRead();
      return (R)file.find(r);
   }
   
   public void openRead() {
      getFile().openRead();
   }
   
   public void closeRead() {
      getFile().closeRead();
   }
   
   public void openWrite() {
      getFile().openWriteAppend();
   }
   
   public void openWriteNew() {
      getFile().openWriteNew();
   }
   
   public void write( R r ) {
      file.write(r);
   }
   
   public void closeWrite() {
      getFile().closeWrite();
   }
}
