package io.github.repir.Strategy.Operator;

import io.github.repir.Strategy.Tools.MatchSetPositional;
import io.github.repir.Strategy.Tools.ProximityOccurrence;
import io.github.repir.Strategy.Tools.MatchSetLength;
import io.github.repir.Retriever.Document;
import io.github.repir.Strategy.GraphRoot;
import io.github.repir.Strategy.Tools.ProximityOrderedSet;
import io.github.repir.tools.Lib.Log;
import java.util.ArrayList;

/**
 * This {@link ProximityOperator} counts ordered co-occurrence of the contained
 * {@link Operator}s in the same document. 
 * <p/>
 * @author jeroen
 */
public class ProximityOperatorOrdered extends ProximityOperator {

   public static Log log = new Log(ProximityOperatorOrdered.class);
   ProximityOrderedSet termpositions;
   
   public ProximityOperatorOrdered(GraphRoot root, ArrayList<Operator> list) {
      super(root, list);
   }

   @Override
   public Operator clone(GraphRoot newmodel) {
      ArrayList<Operator> list = new ArrayList<Operator>();
      for (Operator n : containednodes) {
         list.add(n.clone(newmodel));
      }
      ProximityOperatorOrdered f = new ProximityOperatorOrdered(newmodel, list);
      featurevalues.copy(f.featurevalues);
      f.parent = parent;
      return f;
   }

   @Override
   public void process(Document doc) {
      if (termpositions.hasProximityMatches(doc)) {
         MatchSetLength matches = new MatchSetLength();
         do {
            if (termpositions.tpi[containednodes.size() - 1].current - termpositions.tpi[0].current
                 <= span - termpositions.tpi[containednodes.size() - 1].span) {
               matches.add(new ProximityOccurrence(termpositions.tpi[0].current, termpositions.tpi[containednodes.size() - 1].current - termpositions.tpi[0].current + 1));
            }
         } while( termpositions.next() );

         MatchSetPositional result = matches.purgeOccurrencesOccupyingSameSpace();

         featurevalues.pos = new int[result.size()];
         featurevalues.dist = new int[result.size()];
         int pos = 0;
         for (ProximityOccurrence m : result) {
            featurevalues.pos[pos] = m.pos;
            featurevalues.dist[pos++] = m.span;
         }
         featurevalues.frequency = featurevalues.pos.length;
      } else {
         featurevalues.pos = ZEROPOS;
         featurevalues.dist = ZEROPOS;
         featurevalues.frequency = 0;
      }
   }

  @Override
   protected void prepareRetrieval() {
      termpositions = new ProximityOrderedSet(containednodes);
      termpositions.setMaxSpan(span);
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
