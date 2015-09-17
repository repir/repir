package io.github.repir.Strategy.Operator;

import java.util.ArrayList;
import io.github.repir.Strategy.Collector.CollectorProximity;
import io.github.repir.Repository.ProximityStats;
import io.github.repir.Repository.ProximityStats.Record;
import io.github.repir.Repository.TermDocumentFeature;
import io.github.repir.Strategy.GraphRoot;
import io.github.htools.lib.ArrayTools;
import io.github.htools.lib.Log;
import io.github.htools.lib.PrintTools;

/**
 * The {@link ProximityOperator} counts co-occurrence of the contained
 * {@link Operator}s in the same document. The contained operators are typically
 * single {@link Term} objects, but can be any operator that has a position and
 * a span, e.g. proximity operators, synonym operators.
 * <p/>
 * Note: two co-occurrences in the same document cannot overlap. If an operator
 * position could belong to multiple candidate co-occurrences, the occurrence
 * with the smallest word span or the left most of candidate occurrences is
 * used.
 * <p/>
 * A ProximityOperator can be configured to be ordered by using {@link #setordered(1)
 * } and limited to a span by using {@link #setspan(int) }.
 * <p/>
 * Processing a ProximityOperator results in a OperatorValues that contains the
 * frequency of the featureproximity in the document, and position and dist
 * arrays that contain the position and span of each occurrence.
 * <p/>
 * A ProximityOperator can be given a collection and document frequency for
 * scoring (i.e. df=? cf=? in the Query), or retrieve this from
 * {@link ProximityStats} if present. If these statistics are undetermined, the
 * ProximityOperator will request a pre-pass to collect these statistics and
 * store them in {@link ProximityStats} for future reuse.
 * <p/>
 * @author jeroen
 */
public abstract class ProximityOperator extends CachableOperator {

   public static Log log = new Log(ProximityOperator.class);
   public static final int ZEROPOS[] = new int[0];
   protected int[] dist;
   public boolean keepstopwords = false;
   public ProximityStats proximitystats;
   public CollectorProximity collector;

   public ProximityOperator(GraphRoot root, ArrayList<Operator> terms) {
      super(root, terms);
   }

   @Override
   public boolean positional(boolean positional) {
      return true;
   }

   @Override
   public boolean expand() {
      return containednodes.size() < 2;
   }

   @Override
   public void doAnnounce() {
      super.announce(ANNOUNCEKEY.SCORABLE, this);
   }

   /**
    * Prohibit the announcement of contained terms as SCORABLE, because the
    * ProximityOperator will be scored only at the featureproximity level.
    * <p/>
    * @param key
    * @param node
    */
   @Override
   public void announce(ANNOUNCEKEY key, Operator node) {
      if (key != ANNOUNCEKEY.SCORABLE && key != ANNOUNCEKEY.STOPWORD) {
         super.announce(key, node);
      }
   }

   /**
    * Sets the span of the featureproximity, i.e. the maximum number of word
    * position that is considered to be a featureproximity occurrence.
    * <p/>
    * @param span
    */
   public void setspan(Long span) {
      this.span = span.intValue();
   }
   
   public int[] getDist() {
      return dist;
   }
   
   public void setkeepstopwords(Long value) {
      this.keepstopwords = value == 1;
   }

   @Override
   public void configureFeatures() {
      int newspan = 0;
      for (Operator f : containednodes) {
         newspan += f.span;
      }
      span = Math.max(span, newspan);
   }

   @Override
   public void setupCollector() {
      collector = new CollectorProximity(this);
   }

   public ProximityStats getCache() {
      if (proximitystats == null) {
         proximitystats = ProximityStats.get(repository);
         proximitystats.openRead();
      }
      return proximitystats;
   }

   /**
    * Unless set manually, phrases need a pre-pass to determine their corpus
    * statistics, in the reformulation the obtained corpus statistics are
    * included in the query. If the obtained cf is 0, the featureproximity is
    * left out.
    * <p/>
    * @return
    */
   @Override
   public String postReform() {
      String rf = postReformUnweighted();
      if (queryweight != 1 && queryweight != 0) {
         return PrintTools.sprintf("%s#%e", rf, queryweight);
      } else {
         return rf;
      }
   }

   @Override
   public String postReformUnweighted() {
      StringBuilder sb = new StringBuilder();
      if (span != Integer.MAX_VALUE) {
         sb.append("span=").append(span).append(" ");
      }
      if (keepstopwords) {
         sb.append("keepstopwords=1 ");
      }
      if (cf > -1) {
         sb.append(PrintTools.sprintf("cf=%d ", cf));
      }
      if (df > -1) {
         sb.append(PrintTools.sprintf("df=%d ", df));
      }
      for (Operator f : containednodes) {
         sb.append(f.postReformUnweighted()).append(" ");
      }
      return sb.toString();
   }

   @Override
   public void readCachedData() {
      ProximityStats cache = getCache();
      Record s = createRecord();
      Record r = cache.find(s);
      if (r != null) {
         cf = r.cf;
         df = r.df;
      }
   }

   /**
    * At the end of the pre pass this method is called by the collector to
    * process the obtained statistics in such a way that by reformulating the
    * query, the required statistics are written in the query.
    */
   public void processCollected() {
      cf = collector.cf;
      df = collector.df;
      if (cf == 0 && root.removenonexist) {
         root.remove(this);
      }
   }

   public Record createRecord() {
      ProximityStats cache = getCache();
      Record r = (Record) cache.newRecord();
      r.query = postReformUnweighted();
      return r;
   }

   @Override
   public void setTDFDependencies() {
      ArrayList<TermDocumentFeature> list = new ArrayList<TermDocumentFeature>();
      if (this.needsCollect() || !root.needsPrePass()) {
         for (Operator g : containednodes) {
            list.addAll(g.getRequiredTDF());
         }
         for (TermDocumentFeature f : list) {
            f.setDependencies((TermDocumentFeature[]) ArrayTools.arrayOfOthers(list, f));
         }
      }
   }

   @Override
   public ArrayList<TermDocumentFeature> getRequiredTDF() {
      ArrayList<TermDocumentFeature> list = new ArrayList<TermDocumentFeature>();
      for (Operator g : containednodes) {
         list.addAll(g.getRequiredTDF());
      }
      return list;
   }
}
