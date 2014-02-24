package io.github.repir.Strategy.Collector;
import java.io.EOFException;
import java.util.ArrayList;
import java.util.Collection;
import io.github.repir.Repository.StoredDynamicFeature;
import io.github.repir.Strategy.Strategy;
import io.github.repir.tools.Content.RecordHeaderDataRecord;
import io.github.repir.tools.Content.RecordHeaderInterface;
import io.github.repir.tools.Content.StructureReader;
import io.github.repir.tools.Content.StructureWriter;
import io.github.repir.tools.Lib.Log;

public abstract class CollectorCachable<R extends RecordHeaderDataRecord> extends Collector {
   public static Log log = new Log(CollectorCachable.class);
   protected StoredDynamicFeature sdf;
   
   public CollectorCachable( ) {
   }
   
   public CollectorCachable (Strategy rm) {
      super( rm );
   }
   
   /**
    * If the collected results should be stored in a StoredDynamicFeature, this 
    * should call append() on that feature to store the results.
    */   
   public abstract StoredDynamicFeature getStoredDynamicFeature();   
   
   public void startAppend() {
      log.info("startAppend %s", this);
      sdf = getStoredDynamicFeature();
      sdf.openWrite();
      //log.info("%s", sdf.file);
   }
   
   public abstract void streamappend( );
   
   public abstract void streamappend( R record );
   
   public abstract void streamappend( CollectorCachable c );
   
   public abstract R createRecord();
   
   public void finishAppend() {
      log.info("finishAppend() %s", this);
      sdf.closeWrite();
   }
   
   /**
    * Usually, CollectorCachable should noyt use different ID's, to ensure all
    * collected statistics for the same StoredDynamicFeature are processed by a 
    * single reducer to avoid concurrent file writes to the StoredDynamicFeature.
    * @param reader
    * @throws EOFException 
    */
   @Override
   public final void readID(StructureReader reader) throws EOFException {  }

   @Override
   public final void writeID(StructureWriter writer) {  }
   
   @Override
   public String getReducerName() {
      return getCanonicalName();
   }

   @Override
   public Collection<String> getReducerIDs() {
      ArrayList<String> reducers = new ArrayList<String>();
      reducers.add(getReducerName());
      return reducers;
   }
}
