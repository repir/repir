package io.github.repir.Strategy.Collector;

import java.io.EOFException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import io.github.repir.Repository.PhraseStats;
import io.github.repir.Repository.PhraseStats.Record;
import io.github.repir.Strategy.FeatureProximity;
import io.github.repir.Strategy.FeatureValues;
import io.github.repir.Retriever.Document;
import io.github.repir.RetrieverMR.CollectorKey;
import io.github.repir.Strategy.FeatureProximityOrdered;
import io.github.repir.tools.Content.RecordSortHashRecord;
import io.github.repir.tools.Content.StructureReader;
import io.github.repir.tools.Content.StructureWriter;
import io.github.repir.tools.Lib.ArrayTools;
import io.github.repir.tools.Lib.Log;
import io.github.repir.tools.Lib.MathTools;

/**
 * Collects the distance distribution for Phrase occurrences. Typically, this is
 * done on a unigram index, that has no record of co-occurrences, to accurately
 * obtain the distance distribution of a single FeaturePhraseOld used for query
 * reformulation or analysis.
 * <p/>
 * @author jeroen
 */
public class CollectorPhrase extends CollectorCachable<Record> {

   public static Log log = new Log(CollectorPhrase.class);
   FeatureProximity phrase;
   public int terms[];
   public boolean isordered;
   public int span;
   public static ArrayList<Record> recordstostore;
   //public HashMap<Integer, Integer> distmap = new HashMap<Integer, Integer>();
   public long tf;
   public int df;

   public CollectorPhrase() {
      super();
   }

   public CollectorPhrase(FeatureProximity f) {
      super(f.retrievalmodel);
      setPhrase(f);
   }

   @Override
   public int hashCode() {
      return MathTools.finishHash(MathTools.combineHash(MathTools.combineHash(31, isordered ? 1 : 0, span), terms));
   }

   @Override
   public boolean equals(Object r) {
      if (r instanceof CollectorPhrase) {
         CollectorPhrase rr = (CollectorPhrase) r;
         if (!ArrayTools.equals(terms, rr.terms) || span != rr.span || isordered != rr.isordered) {
            return false;
         }
         return true;
      }
      return false;
   }

   public void setPhrase(FeatureProximity f) {
      phrase = f;
      terms = phrase.getTermIds();
      span = phrase.span;
      isordered = phrase instanceof FeatureProximityOrdered;
      containedfeatures.add(f);
   }

   @Override
   public void prepareRetrieval() {
      phrase.doPrepareRetrieval();
   }

//   public String getKey() {
//      return phrase.postReform();
//   }

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
      return terms == null;
   }

   @Override
   public void setCollectedResults() {
      phrase.processCollected();
   }
   
   /**
    * Adds the span of occurring phrases to a distance map.
    * <p/>
    * @param d
    */
   @Override
   public void collectDocument(Document d) {
      FeatureValues values = phrase.featurevalues;
      //log.info("process %d", values.frequency);
      if (values.dist != null && values.dist.length > 0) {
         df++;
         tf += values.dist.length;
         //for (int dist : values.dist) {
         //   Integer freq = distmap.get(dist);
         //   if (freq == null) {
         //      distmap.put(dist, 1);
         //   } else {
         //      distmap.put(dist, freq + 1);
         //   }
         //}
      }
   }

   public void reuse() {
      tf = 0;
      df = 0;
   }
   
   @Override
   public void postLoadFeatures(int partition) {
   }

   @Override
   public void writeValue(StructureWriter rw) {
      //log.info("write %d", distmap.size());
      rw.writeC(tf);
      rw.writeC(df);
      //rw.writeC(distmap.size());
      //for (Map.Entry<Integer, Integer> entry : distmap.entrySet()) {
      //   rw.writeC(entry.getKey());
      //   rw.writeC(entry.getValue());
      //}
   }

   @Override
   public void readValue(StructureReader reader) {
      try {
         tf = reader.readCLong();
         df = reader.readCInt();
         //int distcount = reader.readCInt();
         //log.info("read %d", distcount);
         //for (int d = 0; d < distcount; d++) {
         //   distmap.put(reader.readCInt(), reader.readCInt());
         //}
      } catch (EOFException ex) {
         log.fatalexception(ex, "read( %s )", reader);
      }
   }

   //public HashMap<Integer, Integer> getDist() {
   //   return distmap;
   //}

   @Override
   public void aggregate(Collector subcollector) {
      CollectorPhrase cd = (CollectorPhrase) subcollector;
      tf += cd.tf;
      df += cd.df;
      //for (Map.Entry<Integer, Integer> entry : cd.distmap.entrySet()) {
      //   Integer freq = distmap.get(entry.getKey());
      //   if (freq == null) {
      //      distmap.put(entry.getKey(), entry.getValue());
      //   } else {
      //      distmap.put(entry.getKey(), freq + entry.getValue());
      //   }
      //}
      //log.info("distmap %s", distmap);
   }

   public PhraseStats getStoredDynamicFeature() {
      PhraseStats phrasestats = (PhraseStats) this.getRepository().getFeature("PhraseStats");
      return phrasestats;
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
      ((CollectorPhrase)c).streamappend( createRecord() );
   }
   
   /**
    * @return Record with the collected data that is to be stored, or null if it
    * must not be stored
    */
   public Record getCollectedRecord() {
      //log.info("storeCollected() termid=%s", terms);
      if (terms != null) { //todo switch for terms
         return createRecord();
      }
      return null;
   }

   public Record createRecord() {
      PhraseStats phrasestats = getStoredDynamicFeature();
      Record r = (Record) phrasestats.newRecord();
      r.terms = terms;
      r.span = span;
      r.ordered = isordered;
      r.cf = tf;
      r.df = df;
      return r;
   }

   @Override
   public void decode() {
   }

   @Override
   public void writeKey(StructureWriter writer) {
      //writer.write(key());
      writer.writeC(terms);
      writer.writeC(phrase.span);
      writer.write(phrase instanceof FeatureProximityOrdered);
   }

   @Override
   public void readKey(StructureReader reader) throws EOFException {
      //key = reader.readString();
      terms = reader.readCIntArray();
      span = reader.readCInt();
      isordered = reader.readBoolean();
   }
}
