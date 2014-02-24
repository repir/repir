package io.github.repir.Strategy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.TreeSet;
import io.github.repir.Strategy.Collector.CollectorPhrase;
import io.github.repir.Repository.PhraseStats;
import io.github.repir.Repository.PhraseStats.Record;
import io.github.repir.Repository.TermDocumentFeature;
import io.github.repir.tools.Lib.ArrayTools;
import io.github.repir.tools.Lib.Log;
import io.github.repir.tools.Lib.PrintTools;

/**
 * This GraphNode counts the co-occurrence of the contained containedfeatures in
 * the same document. The contained containedfeatures are typically single
 * {@link Term2} objects, but can also be other phrases, synonyms, or whatever
 * other feature that has a position and a span. During document processing,
 * each occurring sub feature can only be part of a single phrase occurrence,
 * which is the occurrence with the smallest word span if there are multiple
 * possibilities.
 * <p/>
 * A FeatureProximity can be configured to be ordered by using {@link #setordered(1)
 * } and limited to a span by using {@link #setspan(int) }.
 * <p/>
 * Processing a FeaturePhraseOld results in a FeatureValues that contains the
 * frequency of the phrase in the document, and position and dist arrays that
 * contain the position and span of each occurrence.
 * <p/>
 * If a FeaturePhraseOld is not given the corpus and document frequency, by
 * default, it will collect the corpus frequency on the first pass, reformulate
 * to a phrase that includes the collected corpus and documents frequencies
 * (i.e. "(frequency=? df=? term term...)").
 * <p/>
 * @author jeroen
 */
public abstract class FeatureProximity extends GraphNodeCachable {

   public static Log log = new Log(FeatureProximity.class);
   public static final int ZEROPOS[] = new int[0];
   public boolean keepstopwords = false;
   public PhraseStats phrasestats;
   public CollectorPhrase collector;

   public FeatureProximity(GraphRoot root) {
      super(root);
   }

   //public FeatureProximity(GraphRoot im, ArrayList<Term> list) {
   //   this(im);
   //   for (Term t : list) {
   //      add(t);
   //   }
   //}
   @Override
   public boolean positional(boolean positional) {
      return true;
   }

   @Override
   public boolean expand() {
      return containedfeatures.size() < 2;
   }

   @Override
   public void doAnnounce() {
      super.announce(ANNOUNCEKEY.SCORABLE, this);
   }

   /**
    * Prohibit the announcement of contained terms as SCORABLE, because the
    * FeatureProximity will be scored only at the phrase level.
    * <p/>
    * @param key
    * @param node
    */
   @Override
   public void announce(ANNOUNCEKEY key, GraphNode node) {
      if (key != ANNOUNCEKEY.SCORABLE && key != ANNOUNCEKEY.STOPWORD) {
         super.announce(key, node);
      }
   }

   /**
    * Sets the span of the phrase, i.e. the maximum number of word position that
    * is considered to be a phrase occurrence.
    * <p/>
    * @param span
    */
   public void setspan(Long span) {
      this.span = span.intValue();
   }

   public void setkeepstopwords(Long value) {
      this.keepstopwords = value == 1;
   }

   @Override
   public void configureFeatures() {
      int newspan = 0;
      if (containedfeatures.size() == 1) {
         containedfeatures.get(0).parent = this.parent;

         root.replace(this, containedfeatures);
      } else {
         for (GraphNode f : containedfeatures) {
            newspan += f.span;
         }
         span = Math.max(span, newspan);
      }
   }

   @Override
   public void setupCollector() {
      collector = new CollectorPhrase(this);
   }

   public PhraseStats getCache() {
      if (phrasestats == null) {
         phrasestats = (PhraseStats) repository.getFeature("PhraseStats");
         phrasestats.openRead();
      }
      return phrasestats;
   }

   /**
    * Unless set manually, phrases need a pre-pass to determine their corpus
    * statistics, in the reformulation the obtained corpus statistics are
    * included in the query. If the obtained corpusfrequency is 0, the phrase is
    * left out.
    * <p/>
    * @return
    */
   @Override
   public String postReform() {
      String rf = postReformUnweighted();
      if (featurevalues.queryweight != 1 && featurevalues.queryweight != 0) {
         return PrintTools.sprintf("%s#%e", rf, featurevalues.queryweight);
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
      if (featurevalues.corpusfrequency > -1) {
         sb.append(io.github.repir.tools.Lib.PrintTools.sprintf("tf=%d ", featurevalues.corpusfrequency));
      }
      if (featurevalues.documentfrequency > -1) {
         sb.append(io.github.repir.tools.Lib.PrintTools.sprintf("df=%d ", featurevalues.documentfrequency));
      }
      for (GraphNode f : containedfeatures) {
         sb.append(f.postReformUnweighted()).append(" ");
      }
      return sb.toString();
   }

   @Override
   public void readCachedData() {
      for (GraphNode n : containedfeatures) {
         if (!(n instanceof Term)) {
            return;
         }
      }
      PhraseStats cache = getCache();
      Record s = createRecord();
      Record r = cache.find(s);
      if (r != null) {
         featurevalues.corpusfrequency = r.cf;
         featurevalues.documentfrequency = r.df;
      }
   }

   /**
    * At the end of the pre pass this method is called by the collector to 
    * process the obtained statistics in such a way that
    * by reformulating the query, the required statistics are written in the query.
    */
   public void processCollected() {
      featurevalues.corpusfrequency = collector.tf;
      featurevalues.documentfrequency = collector.df;
      if (featurevalues.corpusfrequency == 0 && root.removenonexist)
         root.remove(this);
   }

   public Record createRecord() {
      PhraseStats cache = getCache();
      Record r = (Record) cache.newRecord();
      r.ordered = this instanceof FeatureProximityOrdered;
      r.terms = getTermIds();
      r.span = span;
      return r;
   }

   public int[] getTermIds() {
      for (GraphNode f : containedfeatures) {
         if (!(f instanceof Term)) {
            return null;
         }
      }
      int r[] = new int[containedfeatures.size()];
      for (int i = 0; i < containedfeatures.size(); i++) {
         r[i] = ((Term) containedfeatures.get(i)).termid;
      }
      return r;
   }
   
   @Override
   public void setTDFDependencies() {
      ArrayList<TermDocumentFeature> list = new ArrayList<TermDocumentFeature>();
      if (this.needsCollect() || !root.needsPrePass()) {
         for (GraphNode g : containedfeatures) {
            list.addAll(g.getRequiredTDF());
         }
         for (TermDocumentFeature f : list) {
            f.setDependencies((TermDocumentFeature[])ArrayTools.arrayOfOthers(list, f));
         }
      }
   }
   
   @Override
   public ArrayList<TermDocumentFeature> getRequiredTDF() {
      ArrayList<TermDocumentFeature> list = new ArrayList<TermDocumentFeature>();
      for (GraphNode g : containedfeatures) {
         list.addAll(g.getRequiredTDF());
      }
      return list;
   }
   

}
