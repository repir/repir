package io.github.repir.Strategy.Collector;
import java.util.ArrayList;
import java.util.Collection;
import io.github.repir.Repository.StoredDynamicFeature;
import io.github.repir.Strategy.Strategy;
import io.github.repir.tools.Content.EOCException;
import io.github.repir.tools.Structure.StructuredFileKeyValueRecord;
import io.github.repir.tools.Structure.StructureReader;
import io.github.repir.tools.Structure.StructureWriter;
import io.github.repir.tools.Lib.Log;

/**
 * An abstract for a {@link Collector}s that stores its results in a {@link StoredDynamicFeature}.
 * Implementations must specify the Record structure used to write the data as a 
 * generic parameter and implement {@link #getStoredDynamicFeature()}
 * to provide the feature used. By default, the CanonicalName is used as an identifier
 * to assign all results for the same {@link CollectorCachable} to the same reducer,
 * allowing to write all results to the same {@link StoredDynamicFeature} together.
 * @author jer
 * @param <R> 
 */
public abstract class CollectorCachable<R extends StructuredFileKeyValueRecord> extends Collector {
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
      sdf = getStoredDynamicFeature();
      sdf.openWrite();
   }
   
   public abstract void streamappend( );
   
   public abstract void streamappend( R record );
   
   public abstract void streamappend( CollectorCachable c );
   
   public abstract R createRecord();
   
   public void finishAppend() {
      sdf.closeWrite();
   }
   
   /**
    * By default, collected results are assigned to a reducer based on the CanonicalName. 
    * CollectorCachable should not override this, to ensure all
    * collected statistics for the same StoredDynamicFeature are processed by a 
    * single reducer to avoid concurrent file writes to the StoredDynamicFeature.
    * To collect results for different {@link Operator}s, use different keys, which
    * is used to group the collected results in the reducer.
    * @param reader
    * @throws EOCException 
    */
   @Override
   public final void readID(StructureReader reader) throws EOCException {  }

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
