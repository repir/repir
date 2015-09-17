package io.github.repir.Strategy.Collector;

import java.util.ArrayList;
import java.util.Collection;
import io.github.repir.Repository.ProximityStats;
import io.github.repir.Repository.ProximityStats.Record;
import io.github.repir.Strategy.Operator.ProximityOperator;
import io.github.repir.Retriever.Document;
import io.github.htools.io.EOCException;
import io.github.htools.io.struct.StructureReader;
import io.github.htools.io.struct.StructureWriter;
import io.github.htools.lib.Log;
import io.github.htools.lib.MathTools;

/**
 * Collects the collection and document frequency of {@link ProximityOperator}s. 
 * Typically, RR operates on a unigram index, that has no record of co-occurrences, 
 * which must be obtained in a pre-pass to allow scoring of {@link ProximityOperator}s. 
 * The collected frequencies are stored in {@link ProximityStats} so that
 * once obtained these can be reused without the need for pre-passes.
 * <p/>
 * @author jeroen
 */
public class CollectorProximity extends CollectorCachable<Record> {

   public static Log log = new Log(CollectorProximity.class);
   ProximityOperator featureproximity;
   public String query;
   public static ArrayList<Record> recordstostore;
   public long cf;
   public int df;

   public CollectorProximity() {
      super();
   }

   public CollectorProximity(ProximityOperator f) {
      super(f.retrievalmodel);
      setPhrase(f);
   }

   @Override
   public int hashCode() {
      return MathTools.hashCode(query.hashCode());
   }

   @Override
   public boolean equals(Object r) {
      if (r instanceof CollectorProximity) {
         CollectorProximity rr = (CollectorProximity) r;
         if (!query.equals(rr.query)) {
            return false;
         }
         return true;
      }
      return false;
   }

   public void setPhrase(ProximityOperator f) {
      featureproximity = f;
      query = featureproximity.postReformUnweighted();
      containedfeatures.add(f);
   }

   @Override
   public void prepareRetrieval() {
      featureproximity.doPrepareRetrieval();
   }

   @Override
   public Collection<String> getReducerIDs() {
      if (reduceInQuery()) {
         return strategy.query.getReducerID();
      } else {
         ArrayList<String> reducers = new ArrayList<String>();
         reducers.add(this.getCanonicalName());
         return reducers;
      }
   }

   public String getReducerName() {
      if (reduceInQuery()) {
         return Integer.toString(strategy.query.id);
      } else {
         return getCanonicalName();
      }
   }

   @Override
   public boolean reduceInQuery() {
      return false;
   }

   @Override
   public void setCollectedResults() {
      featureproximity.processCollected();
   }
   
   /**
    * Adds the span of occurring phrases to a distance map.
    * <p/>
    * @param d
    */
   @Override
   public void collectDocument(Document d) {
      int dist[] = featureproximity.getDist();
      if (dist != null && dist.length > 0) {
//         log.info("doc %d %d", d.docid, values.dist.length);
         df++;
         cf += dist.length;
      }
   }

   public void reuse() {
      cf = 0;
      df = 0;
   }
   
   @Override
   public void postLoadFeatures(int partition) {
   }

   @Override
   public void writeValue(StructureWriter rw) {
      rw.writeC(cf);
      rw.writeC(df);
   }

   @Override
   public void readValue(StructureReader reader) {
      try {
         cf = reader.readCLong();
         df = reader.readCInt();
         //int distcount = reader.readCInt();
         //log.info("read %d", distcount);
         //for (int d = 0; d < distcount; d++) {
         //   distmap.put(reader.readCInt(), reader.readCInt());
         //}
      } catch (EOCException ex) {
         log.fatalexception(ex, "read( %s )", reader);
      }
   }

   @Override
   public void aggregate(Collector subcollector) {
      CollectorProximity cd = (CollectorProximity) subcollector;
      cf += cd.cf;
      df += cd.df;
   }

   public ProximityStats getStoredDynamicFeature() {
      ProximityStats proximitystats = ProximityStats.get(this.getRepository());
      return proximitystats;
   }

   
   @Override
   public void streamappend() {
      this.sdf.write( createRecord() );
   }
   
   @Override
   public void streamappend(Record record) {
      this.sdf.write( record );
   }
   
   @Override
   public void streamappend(CollectorCachable c) {
      ((CollectorProximity)c).streamappend( createRecord() );
   }
   
   /**
    * @return Record with the collected data that is to be stored, or null if it
    * must not be stored
    */
   public Record getCollectedRecord() {
      //log.info("storeCollected() termid=%s", terms);
      if (query != null && query.length() > 0) { //todo switch for terms
         return createRecord();
      }
      return null;
   }

   @Override
   public Record createRecord() {
      ProximityStats proximitystats = getStoredDynamicFeature();
      Record r = (Record) proximitystats.newRecord();
      r.query = query;
      r.cf = cf;
      r.df = df;
      return r;
   }

   @Override
   public void decode() {
   }
   
   @Override
   public void writeKey(StructureWriter writer) {
      writer.write(query);
   }

   @Override
   public void readKey(StructureReader reader) throws EOCException {
      query = reader.readString();
   }
}
