package io.github.repir.Strategy.Collector;

import java.util.ArrayList;
import java.util.Collection;
import io.github.repir.Repository.SynStats;
import io.github.repir.Repository.SynStats.Record;
import io.github.repir.Strategy.Operator.SynonymOperator;
import io.github.repir.Retriever.Document;
import io.github.repir.tools.io.EOCException;
import io.github.repir.tools.io.struct.StructureReader;
import io.github.repir.tools.io.struct.StructureWriter;
import io.github.repir.tools.lib.Log;
import io.github.repir.tools.lib.MathTools;

/**
 * Collects the collection and document frequency of {@link SynonymOperator}s. 
 * Typically, a {@link Repository} has no pre-recorded synonym data, 
 * which must be obtained in a pre-pass to allow scoring of {@link SynonymOperator}s. 
 * The collected frequencies are stored in {@link SynStats} so that
 * once obtained these can be reused without the need for pre-passes.
 * <p/>
 * @author jeroen
 */
public class CollectorSynonym extends CollectorCachable<Record> {

   public static Log log = new Log(CollectorSynonym.class);
   SynonymOperator syn;
   String synid;
   public long cf = 0;
   public long df = 0;

   public CollectorSynonym() {
      super();
   }
   
   public CollectorSynonym(SynonymOperator f) {
      super(f.retrievalmodel);
      setSynonym( f );
   }

   public void setSynonym( SynonymOperator f ) {
      syn = f;
      synid = syn.postReformUnweighted();
      containedfeatures.add(f);
   }
   
   @Override
   public int hashCode() {
      return MathTools.finishHash(MathTools.hashCode(synid.hashCode()));
   }

   @Override
   public boolean equals(Object r) {
      if (r instanceof CollectorSynonym) {
         CollectorSynonym rr = (CollectorSynonym) r;
         if (!synid.equals( rr.synid ) ) {
            return false;
         }
         return true;
      }
      return false;
   }

   public void prepareRetrieval() {
       syn.doPrepareRetrieval();
   }
   
   public void reuse() {
      cf = 0;
      df = 0;
   }
   
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
   
   public void setCollectedResults() {
      syn.processCollected();
   }
   
   @Override
   public void collectDocument(Document d) {
      boolean exists = false;
         if (syn.getFrequency() > 0) {
            exists = true;
            cf += syn.getFrequency();
         }
      if (exists) {
         df++;
      }
   }

   @Override
   public void postLoadFeatures(int partition) {
   }

   @Override
   public void writeValue(StructureWriter rw) {
      //log.info("write %d", cf);
      rw.writeC(cf);
      rw.writeC(df);
   }

   @Override
   public void readValue(StructureReader reader) {
      try {
         cf = reader.readCLong();
         df = reader.readCLong();
      } catch (EOCException ex) {
         log.fatalexception(ex, "read( %s )", reader);
      }
   }

   @Override
   public void aggregate(Collector subcollector) {
      CollectorSynonym cd = (CollectorSynonym) subcollector;
      cf += cd.cf;
      df += cd.df;
   }

   @Override
   public void decode() {
   }

   @Override
   public void writeKey(StructureWriter writer) {
      //writer.write(key());
      writer.write(synid);
   }

   @Override
   public void readKey(StructureReader reader) throws EOCException {
      //key = reader.readStringUntil();
      synid = reader.readString();
   }

//   @Override
//   public String getKey() {
//      return query.postReform();
//   }

   public SynStats getStoredDynamicFeature() {
      SynStats synstats = SynStats.get(this.getRepository());
      return synstats;
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
      ((CollectorSynonym)c).streamappend( createRecord() );
   }
   
   public Record getCollectedRecord() {
      if (synid != null) {
         return createRecord();
      }
      return null;
   }

   public Record createRecord() {
      SynStats synstats = getStoredDynamicFeature();
      Record r = (Record) synstats.newRecord();
      r.query = synid;
      r.cf = cf;
      r.df = df;
      return r;
   }

   @Override
   public boolean reduceInQuery() {
      return false;
   }
}
