package io.github.repir.Strategy.Collector;

import java.io.EOFException;
import java.util.ArrayList;
import java.util.Collection;
import io.github.repir.Repository.PhraseStats;
import io.github.repir.Repository.SynStats;
import io.github.repir.Repository.SynStats.Record;
import io.github.repir.Strategy.FeatureSynonym;
import io.github.repir.Strategy.GraphNode;
import io.github.repir.Retriever.Document;
import io.github.repir.tools.Content.RecordSortHashRecord;
import io.github.repir.tools.Content.StructureReader;
import io.github.repir.tools.Content.StructureWriter;
import io.github.repir.tools.Lib.ArrayTools;
import io.github.repir.tools.Lib.Log;
import io.github.repir.tools.Lib.MathTools;

public class CollectorSynonym extends CollectorCachable<Record> {

   public static Log log = new Log(CollectorSynonym.class);
   FeatureSynonym syn;
   String synid;
   public long ctf = 0;
   public long cdf = 0;

   public CollectorSynonym() {
      super();
   }
   
   public CollectorSynonym(FeatureSynonym f) {
      super(f.retrievalmodel);
      setSynonym( f );
   }

   public void setSynonym( FeatureSynonym f ) {
      syn = f;
      synid = syn.getTermId();
      containedfeatures.add(f);
   }
   
   @Override
   public int hashCode() {
      return MathTools.finishHash(MathTools.combineHash(31, synid.hashCode()));
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
      ctf = 0;
      cdf = 0;
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

   @Override
   public boolean reduceInQuery() {
      return synid == null;
   }
   
   public void setCollectedResults() {
      syn.processCollected();
   }
   
   @Override
   public void collectDocument(Document d) {
      boolean exists = false;
         if (syn.featurevalues.frequency > 0) {
            exists = true;
            ctf += syn.featurevalues.frequency;
         }
      if (exists) {
         cdf++;
      }
   }

   @Override
   public void postLoadFeatures(int partition) {
   }

   @Override
   public void writeValue(StructureWriter rw) {
      //log.info("write %d", ctf);
      rw.writeC(ctf);
      rw.writeC(cdf);
   }

   @Override
   public void readValue(StructureReader reader) {
      try {
         ctf = reader.readCLong();
         cdf = reader.readCLong();
      } catch (EOFException ex) {
         log.fatalexception(ex, "read( %s )", reader);
      }
   }

   @Override
   public void aggregate(Collector subcollector) {
      CollectorSynonym cd = (CollectorSynonym) subcollector;
      ctf += cd.ctf;
      cdf += cd.cdf;
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
   public void readKey(StructureReader reader) throws EOFException {
      //key = reader.readString();
      synid = reader.readString();
   }

//   @Override
//   public String getKey() {
//      return syn.postReform();
//   }

   public SynStats getStoredDynamicFeature() {
      SynStats synstats = (SynStats) this.getRepository().getFeature("SynStats");
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
      r.syn = synid;
      r.cf = ctf;
      r.df = cdf;
      return r;
   }
}
