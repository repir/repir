package io.github.repir.Repository;

import java.io.EOFException;
import io.github.repir.Retriever.Document;
import io.github.repir.tools.Content.Datafile;
import io.github.repir.tools.Content.RecordIdentity;
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
public abstract class StoredReportableFeature<F extends RecordIdentity, C> extends StoredFeature {

   public static Log log = new Log(StoredReportableFeature.class);
   public F file;
   public int reportid;
   public int partition = -1;
   protected String field;
   
   public StoredReportableFeature(Repository repository, String field) {
      super( repository );
      this.field = field;
   }

   public void setPartition(int partition) {
      //log.info("setPartition %d", partition);
      closeRead();
      this.partition = partition;
   }
   
   @Override
   public F getFile() {
      if (file == null) {
         file = createFile(repository.getStoredFeatureFile(partition, this));
      }
      return file;
   }
   
   public void setReportID(int id) {
      this.reportid = id;
   }

   public int getReportID() {
      return reportid;
   }
   public abstract long getBytesSize();
   
   @Override
   public String getCanonicalName() {
      String clazz = super.getCanonicalName();
      if (field.length() > 0) {
         return clazz + ":" + field;
      } else {
         return clazz;
      }
   }

   @Override
   public String getLabel() {
      if (field.length() > 0) {
         return super.getLabel() + ":" + field;
      } else {
         return super.getLabel();
      }
   }

   public String getField() {
      return field;
   }
   
   public void find(int id) {
      try {
         getFile().find(id);
      } catch (EOFException ex) {
         log.exception(ex, "Find id %d", id);
      }
   }

   protected void read(int id) {
      try {
         if (file == null) {
            openRead();
         }
         file.read(id);
      } catch (EOFException ex) {
         log.exception(ex, "Read StoredFeature id %d", id);
      }
   }
   
   public void read(Document d) {
      try {
         if (partition != d.partition) {
            setPartition(d.partition);
            openRead();
         }
         read(d.docid);
      } catch (Exception ex) {
         log.exception(ex, "Could not read value for document %d", d.docid);
      }
   }
   
   public abstract F createFile(Datafile datafile);

   @Override
   public void openRead() {
      getFile().openRead();
   }

   @Override
   public void closeRead() {
      getFile().closeRead();
      file = null;
   }

   public boolean hasNext() {
      return file.hasNext();
   }

   public boolean next() {
      return file.next();
   }

   @Override
   public void setBufferSize(int size) {
      getFile().setBufferSize(size);
   }

   public void skip() {
      file.skip();
   }

   public void openWrite() {
      getFile().openWrite();
   }

   public void closeWrite() {
      getFile().closeWrite();
      file = null;
   }
   
   @Override
   public void reuse() {
      getFile().reset();
   }
}
