package io.github.repir.Repository;

import java.util.Collection;
import io.github.repir.tools.Content.Datafile;
import io.github.repir.tools.Content.StructuredFileKeyValueRecord;
import io.github.repir.tools.Content.StructuredFileKeyInterface;
import io.github.repir.tools.Lib.Log;
import io.github.repir.tools.Lib.PrintTools;

/**
 * Generic class for Features that are not static and must be stored dynamically.
 * <p/>
 * The feature's data is stored in a file that implemented the StructuredFileKeyInterface.
 * The generic class considers separate Key and KeyValue records, allowing
 * an implementation to choose between a separate StructuredFileKey, which allows
 * efficient access by loading only the small keys and reading only the large 
 * values from disk when needed, or for small amounts of data StructuredFileKeyValue
 * in which Key and Value are stored combined which is allows for fast resident 
 * access to keys and values.
 * <p/>
 * if the configuration contains <classsimplename>.suffix (all lowercase!) then
 * the file used is append with "." and the suffix to allow to use a different version
 * of a file.
 * @author jeroen
 * @param <F> FileType that extends RecordIdentity
 * @param <C> Data type of the feature
 */
public abstract class StoredDynamicFeature<F extends StructuredFileKeyInterface<R>, R extends StructuredFileKeyValueRecord> extends StoredFeature<F> {

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
      Datafile df = getStoredFeatureFile();
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
   
   @Override
   public Datafile getStoredFeatureFile() {
      Datafile datafile;
      String name = getCanonicalName();
      name = name.replaceFirst(":", ".");
      String path = repository.getConfigurationString(name.toLowerCase() + ".path");
      if (path != null && path.length() > 0)
         datafile = new Datafile( repository.fs, path);
      else
         datafile = repository.basedir.getFile(PrintTools.sprintf("repository/dynamic/%s.%s", repository.getPrefix(), getFileNameSuffix()));
      log.info("getStoredFeatureFile %s", datafile.getFullPath());
      return datafile;
   }
}
