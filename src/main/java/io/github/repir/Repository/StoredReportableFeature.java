package io.github.repir.Repository;

import io.github.repir.Retriever.Document;
import io.github.htools.io.Datafile;
import io.github.htools.io.EOCException;
import io.github.htools.io.struct.StructuredFileIntID;
import io.github.htools.lib.Log;

/**
 * Generic class for Features that are stored in the repository. Implementations 
 * must declare a StructuredFileIntID file, (usually an extension of RecordBinary that 
 * ensures records have a unique ID (int)). For performance, the features that are merged with other
 * features should be stored physically sorted on ID. The second declaration is a data 
 * type, which can be complex.
 * @author jeroen
 * @param <F> FileType that extends StructuredFileIntID
 * @param <C> Data type of the feature
 */
public abstract class StoredReportableFeature<F extends StructuredFileIntID, C> extends StoredFeature implements ReportableFeature<C> {

   public static Log log = new Log(StoredReportableFeature.class);
   public F file; 
   public int partition = -1;
   
   public StoredReportableFeature(Repository repository, String field) {
      super( repository, field );
   }

   public void setPartition(int partition) {
      //log.info("setPartition %d", partition);
      if (this.partition > -1)
         closeRead();
      this.partition = partition;
   }
   
   @Override
   public F getFile() {
      if (file == null) {
         file = createFile(getStoredFeatureFile(partition));
      }
      return file;
   }
   
   public abstract long getLength();

   @Override
   public String getLabel() {
      if (getField().length() > 0) {
         return super.getLabel() + ":" + getField();
      } else {
         return super.getLabel();
      }
   }
   
   public void find(int id) {
      try {
         getFile().find(id);
      } catch (EOCException ex) {
         log.exception(ex, "Find id %d", id);
      }
   }

   protected void read(int id) {
      try {
         if (file == null) {
            openRead();
         }
         file.read(id);
      } catch (EOCException ex) {
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
      return file.nextRecord();
   }

   @Override
   public void reuse() {
      getFile().reuseBuffer();
   }

   public void skip() {
      file.skipRecord();
   }

   public void openWrite() {
      getFile().openWrite();
   }

   public void closeWrite() {
      getFile().closeWrite();
      file = null;
   }
}
