package io.github.repir.Strategy.Operator;

import java.util.ArrayList;
import java.util.TreeSet;
import io.github.repir.Strategy.Collector.CollectorSynonym;
import io.github.repir.Repository.SynStats;
import io.github.repir.Repository.SynStats.Record;
import io.github.repir.Repository.TermDocumentFeature;
import io.github.repir.Retriever.Document;
import io.github.repir.Strategy.GraphRoot;
import io.github.repir.tools.Lib.ArrayTools;
import io.github.repir.tools.Lib.Log;
import io.github.repir.tools.Lib.PrintTools;

/**
 * Resolves the contained operators as if they are all occurrences of the same imaginary
 * symbol. Scoring of a synonym is done using the collection and document frequencies 
 * of this imaginary symbol, e.g. "[animal beast]" will have a collection frequency that is
 * the sum of the collection frequencies of "animal" and "beast", and the document frequency
 * is an exact count of the document in which at least one of these words appear. 
 * If the required statistics are not present in {@link SynStats}, this Operator will 
 * request a pre-pass to obtain its statistics and storing them in {@link SynStats}
 * for future reuse.
 * <p/>
 * @author jeroen
 */
public class SynonymOperator extends CachableOperator {

   public static Log log = new Log(SynonymOperator.class);
   public CollectorSynonym collector;
   public SynStats synstats;

   public SynonymOperator(GraphRoot im, ArrayList<Operator> list) {
      super(im, list);
      this.sortContainedFeatures();
   }

   @Override
   public boolean expand() {
      return containednodes.size() < 2;
   }
   
   public void doAnnounce() {
      super.announce( ANNOUNCEKEY.SCORABLE, this );
   }
   
   @Override
   public void configureFeatures() {
      if (containednodes.size() == 1) {
         root.replace(this, containednodes);
      } else {
      ArrayList<Operator> sorted = new ArrayList<Operator>();
      for (Operator g : containednodes) {
         int pos = 0;
         while (pos < sorted.size()) {
            Operator h = sorted.get(pos);
            if (g instanceof Term && !(h instanceof Term))
               break;
            if (g instanceof Term && ((Term)g).termid < ((Term)h).termid)
               break;
            if (g instanceof ProximityOperator && h instanceof ProximityOperator) {
               if (g.postReform().compareTo(h.postReform()) >= 0)
                  break;
            }
            pos++;
         }
         sorted.add(pos, g);
      }
      containednodes = sorted;
      }
   }
   
   @Override
   public void setTDFDependencies() {
       for (Operator g : containednodes) 
          g.setTDFDependencies();
   }
   
   @Override
   public ArrayList<TermDocumentFeature> getRequiredTDF() {
      return new ArrayList<TermDocumentFeature>();
   }
   
  /**
    * Prohibit the announcement of contained query as SCORABLE, because the ProximityOperator will be
    * scored only at the phrase level.
    * <p/>
    * @param key
    * @param node
    */
   public void announce(ANNOUNCEKEY key, Operator node) {
      if (key != ANNOUNCEKEY.SCORABLE) {
         super.announce(key, node);
      }
   }
   
   @Override
   public void setupCollector() {
      collector = new CollectorSynonym(this);
   }

   @Override
   public void process(Document doc) {
      featurevalues.frequency = 0;
      TreeSet<Integer> list = new TreeSet<Integer>();
      int size = 0;
      for (Operator f : containednodes) {
         f.process(doc);
         OperatorValues nodevalues = f.featurevalues;
         featurevalues.frequency += nodevalues.frequency;
         if (nodevalues.pos != null && nodevalues.pos.length > 0) {
            for (int pos : nodevalues.pos)
               list.add(pos);
         }
      }
      int p = 0;
      featurevalues.pos = ArrayTools.toIntArray(list);
   }

   @Override
   public Operator clone(GraphRoot newmodel) {
      SynonymOperator f = new SynonymOperator(newmodel, containednodes);
      featurevalues.copy(f.featurevalues);
      f.parent = parent;
      return f;
   }

   @Override
   public String postReform() {
      String rf = postReformUnweighted();
      if (featurevalues.queryweight != 1 && featurevalues.queryweight != 0) {
         return PrintTools.sprintf("%s#%e", rf, featurevalues.queryweight);
      } else
         return rf;
   }

   @Override
   public String postReformUnweighted() {
      StringBuilder sb = new StringBuilder();
      sb.append("[");
      if (featurevalues.cf > -1) {
         sb.append(io.github.repir.tools.Lib.PrintTools.sprintf("cf=%d ", featurevalues.cf));
      }
      if (featurevalues.df > -1) {
         sb.append(io.github.repir.tools.Lib.PrintTools.sprintf("df=%d ", featurevalues.df));
      }
      for (Operator f : containednodes) {
         sb.append(f.postReformUnweighted()).append(" ");
      }
      return sb.append("]").toString();
   }

   /**
    * At the end of the pre pass this method is called by the collector to 
    * process the obtained statistics in such a way that
    * by reformulating the query, the required statistics are written in the query.
    */
   public void processCollected() {
      featurevalues.cf = collector.cf;
      featurevalues.df = collector.df;
      if (featurevalues.cf == 0 && root.removenonexist)
         root.remove(this);
   }

   @Override
   public String toTermString() {
      return "[" + toTermString(containednodes) + ']';
   }

   public SynStats getCache() {
      if (synstats == null) {
         synstats = (SynStats) repository.getFeature(SynStats.class);
         synstats.openRead();
      }
      return synstats;
   }
   
   public Record createRecord() {
      SynStats cache = getCache();
      Record r = (Record) cache.newRecord();
      r.query = postReformUnweighted();
      return r;
   }

   @Override
   public void readCachedData() {
      SynStats cache = getCache();
      Record s = createRecord();
      Record r = cache.find(s);
      if (r != null) {
         featurevalues.cf = r.cf;
         featurevalues.df = r.df;
      }
   }

   @Override
   public void prepareRetrieval() { }

   class FeaturePositions implements Comparable<FeaturePositions> {

      int pos[];
      int p = 0;

      public FeaturePositions(OperatorValues s) {
         this.pos = s.pos;
      }

      public boolean hasNext() {
         return p < pos.length;
      }

      public int next() {
         int last = pos[p++];
         return last;
      }

      @Override
      public int compareTo(FeaturePositions o) {
         return pos[p] - o.pos[o.p];
      }
   }

   @Override
   public String toString() {
      return io.github.repir.tools.Lib.PrintTools.sprintf("FeatureSynonym[%d] weight %f\n", this.containednodes.size(), this.featurevalues.queryweight);
   }
}
