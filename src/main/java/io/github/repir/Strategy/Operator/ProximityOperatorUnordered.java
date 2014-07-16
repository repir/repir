package io.github.repir.Strategy.Operator;

import io.github.repir.Strategy.Tools.MatchSetPositional;
import io.github.repir.Strategy.Tools.ProximityOccurrence;
import java.util.Arrays;
import java.util.TreeSet;
import io.github.repir.Retriever.Document;
import io.github.repir.Strategy.GraphRoot;
import io.github.repir.Strategy.Tools.MatchSetLength;
import io.github.repir.Strategy.Tools.ProximitySet.ProximityTerm;
import io.github.repir.Strategy.Tools.ProximityTermList;
import io.github.repir.Strategy.Tools.ProximityUnorderedSet;
import io.github.repir.tools.Lib.Log;
import java.util.ArrayList;

/**
 * This {@link ProximityOperator} counts co-occurrence of the contained
 * {@link Operator}s in the same document, in any order. 
 * <p/>
 * @author jeroen
 */
public class ProximityOperatorUnordered extends ProximityOperator {

   public static Log log = new Log(ProximityOperatorUnordered.class);
   public static final int ZEROPOS[] = new int[0];
   public ProximityUnorderedSet termpositions;

   public ProximityOperatorUnordered(GraphRoot root, ArrayList<Operator> list) {
      super(root, list);
      this.sortContainedFeatures();
      span = Integer.MAX_VALUE;
   }

   @Override
   public Operator clone(GraphRoot newmodel) {
      ArrayList<Operator> list = new ArrayList<Operator>();
      for (Operator n : containednodes) {
         list.add(n.clone(newmodel));
      }      
      ProximityOperatorUnordered f = new ProximityOperatorUnordered(newmodel, list);
      f.parent = parent;
      return f;
   }

   @Override
   public void process(Document doc) {
      if (termpositions.hasProximityMatches(doc)) {
         MatchSetLength matches = new MatchSetLength();
         do {
            ProximityTerm last = termpositions.proximitytermlist.last();
            int firstpos = termpositions.first.current;
            int lastpos = last.current + last.span;
            if (lastpos - firstpos <= span) {
               matches.add(new ProximityOccurrence(firstpos, lastpos - firstpos));
            }
         } while( termpositions.next() );

         MatchSetPositional result = matches.purgeOccurrencesOccupyingSameSpace();
 
         pos = new int[result.size()];
         dist = new int[result.size()];
         int p = 0;
         for (ProximityOccurrence m : result) {
            pos[p] = m.pos;
            dist[p++] = m.span;
         }
         frequency = pos.length;
      } else {
         frequency = 0;
         pos = ZEROPOS;
         dist = ZEROPOS;
      }
   }

  @Override
   protected void prepareRetrieval() {
      termpositions = new ProximityUnorderedSet(new ArrayList<PositionalOperator>(containednodes));
      termpositions.setMaxSpan(span);
   }

   @Override
   public String postReformUnweighted() {
      sortContainedFeatures();
      StringBuilder sb = new StringBuilder();
      sb.append("{").append(super.postReformUnweighted()).append("}");
      return sb.toString();
   }
   
   @Override
   public String toTermString() {
      return postReformUnweighted();
   }   
}
