package io.github.repir.Strategy;

import io.github.repir.Strategy.Tools.MatchSetPositional;
import io.github.repir.Strategy.Tools.ProximityOccurrence;
import io.github.repir.Strategy.Tools.MatchSetLength;
import io.github.repir.Retriever.Document;
import io.github.repir.Strategy.Tools.ProximityOrderedSet;
import io.github.repir.tools.Lib.Log;

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
public class FeatureProximityOrdered extends FeatureProximity {

   public static Log log = new Log(FeatureProximityOrdered.class);
   ProximityOrderedSet termpositions;
   
   public FeatureProximityOrdered(GraphRoot root) {
      super(root);
   }

   @Override
   public GraphNode clone(GraphRoot newmodel) {
      FeatureProximityOrdered f = new FeatureProximityOrdered(newmodel);
      for (GraphNode n : containedfeatures) {
         f.add(n.clone(newmodel));
      }
      featurevalues.copy(f.featurevalues);
      f.parent = parent;
      return f;
   }

   @Override
   public void process(Document doc) {
      featurevalues.frequency = 0;
      featurevalues.pos = ZEROPOS;
      if (termpositions.hasProximityMatches(doc)) {
         MatchSetLength matches = new MatchSetLength();
         do {
            if (termpositions.tpi[containedfeatures.size() - 1].current - termpositions.tpi[0].current
                 <= span - termpositions.tpi[containedfeatures.size() - 1].span) {
               matches.add(new ProximityOccurrence(termpositions.tpi[0].current, termpositions.tpi[containedfeatures.size() - 1].current - termpositions.tpi[0].current + 1));
            }
         } while( termpositions.next() );

         MatchSetPositional result = matches.purgeOccurrencesOccupyingSameSpace();
        if (doc.docid == 477919) {
            for (ProximityOccurrence p : matches)
               log.info("%d %d", p.pos, p.span);
            log.info("results %d", result.size());
         }

         featurevalues.pos = new int[result.size()];
         featurevalues.dist = new int[result.size()];
         int pos = 0;
         for (ProximityOccurrence m : result) {
            featurevalues.pos[pos] = m.pos;
            featurevalues.dist[pos++] = m.span;
         }
      }
      featurevalues.frequency = featurevalues.pos.length;
   }

  @Override
   protected void prepareRetrieval() {
      termpositions = new ProximityOrderedSet(containedfeatures);
   }

   @Override
   public String postReformUnweighted() {
      StringBuilder sb = new StringBuilder();
      sb.append("(").append(super.postReformUnweighted()).append(")");
      return sb.toString();
   }
   
   @Override
   public String toTermString() {
      return postReformUnweighted();
   }
}
