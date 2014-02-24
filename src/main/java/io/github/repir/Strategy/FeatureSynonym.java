package io.github.repir.Strategy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.TreeSet;
import io.github.repir.Strategy.Collector.CollectorSynonym;
import io.github.repir.Repository.SynStats;
import io.github.repir.Repository.SynStats.Record;
import io.github.repir.Repository.TermDocumentFeature;
import io.github.repir.Retriever.Document;
import io.github.repir.tools.Lib.ArrayTools;
import io.github.repir.tools.Lib.Log;
import io.github.repir.tools.Lib.PrintTools;

/**
 * Resolves the contained containedfeatures as if they are all occurrences of the same imaginary
 * symbol. By default, this GraphNode will request a pre-pass to aggregate the contained
 * FeatureValues to determine the correct FeatureValues for the Synonym GraphNode as a whole.
 * <p/>
 * @author jeroen
 */
public class FeatureSynonym extends GraphNodeCachable {

   public static Log log = new Log(FeatureSynonym.class);
   public CollectorSynonym collector;
   public SynStats synstats;

   public FeatureSynonym(GraphRoot im, ArrayList<GraphNode> list) {
      this(im);
      for (GraphNode f : list) {
         add(f);
      }
   }

   public FeatureSynonym(GraphRoot im) {
      super(im);
   }

   @Override
   public boolean expand() {
      return containedfeatures.size() < 2;
   }
   
   public void doAnnounce() {
      super.announce( ANNOUNCEKEY.SCORABLE, this );
   }
   
   @Override
   public void configureFeatures() {
      if (containedfeatures.size() == 1) {
         root.replace(this, containedfeatures);
      } else {
      ArrayList<GraphNode> sorted = new ArrayList<GraphNode>();
      for (GraphNode g : containedfeatures) {
         int pos = 0;
         while (pos < sorted.size()) {
            GraphNode h = sorted.get(pos);
            if (g instanceof Term && !(h instanceof Term))
               break;
            if (g instanceof Term && ((Term)g).termid < ((Term)h).termid)
               break;
            if (g instanceof FeatureProximity && h instanceof FeatureProximity) {
               if (g.postReform().compareTo(h.postReform()) >= 0)
                  break;
            }
            pos++;
         }
         sorted.add(pos, g);
      }
      containedfeatures = sorted;
      }
   }
   
   @Override
   public void setTDFDependencies() {
       for (GraphNode g : containedfeatures) 
          g.setTDFDependencies();
   }
   
   @Override
   public ArrayList<TermDocumentFeature> getRequiredTDF() {
      return new ArrayList<TermDocumentFeature>();
   }
   
  /**
    * Prohibit the announcement of contained syn as SCORABLE, because the FeatureProximity will be
    * scored only at the phrase level.
    * <p/>
    * @param key
    * @param node
    */
   public void announce(ANNOUNCEKEY key, GraphNode node) {
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
      for (GraphNode f : containedfeatures) {
         f.process(doc);
         FeatureValues nodevalues = f.featurevalues;
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
   public GraphNode clone(GraphRoot newmodel) {
      FeatureSynonym f = new FeatureSynonym(newmodel, containedfeatures);
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
      if (featurevalues.corpusfrequency > -1) {
         sb.append(io.github.repir.tools.Lib.PrintTools.sprintf("tf=%d ", featurevalues.corpusfrequency));
      }
      if (featurevalues.documentfrequency > -1) {
         sb.append(io.github.repir.tools.Lib.PrintTools.sprintf("df=%d ", featurevalues.documentfrequency));
      }
      for (GraphNode f : containedfeatures) {
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
      featurevalues.corpusfrequency = collector.ctf;
      featurevalues.documentfrequency = collector.cdf;
      if (featurevalues.corpusfrequency == 0 && root.removenonexist)
         root.remove(this);
   }

   @Override
   public String toTermString() {
      return "[" + toTermString(containedfeatures) + ']';
   }

   public SynStats getCache() {
      if (synstats == null) {
         synstats = (SynStats) repository.getFeature("SynStats");
         synstats.openRead();
      }
      return synstats;
   }
   
   public Record createRecord() {
      SynStats cache = getCache();
      Record r = (Record) cache.newRecord();
      r.syn = getTermId();
      return r;
   }

   public String getTermId() {
      return this.postReform();
   }

   @Override
   public void readCachedData() {
      SynStats cache = getCache();
      Record s = createRecord();
      Record r = cache.find(s);
      if (r != null) {
         featurevalues.corpusfrequency = r.cf;
         featurevalues.documentfrequency = r.df;
      }
   }

   @Override
   public void prepareRetrieval() { }

   class FeaturePositions implements Comparable<FeaturePositions> {

      int pos[];
      int p = 0;

      public FeaturePositions(FeatureValues s) {
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
      return io.github.repir.tools.Lib.PrintTools.sprintf("FeatureSynonym[%d] weight %f\n", this.containedfeatures.size(), this.featurevalues.queryweight);
   }
}
