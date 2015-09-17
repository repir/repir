package io.github.repir.Strategy.Operator;

import io.github.repir.Strategy.Tools.MatchSetPositional;
import io.github.repir.Strategy.Tools.ProximityOccurrence;
import io.github.repir.Retriever.Document;
import io.github.repir.Strategy.GraphRoot;
import io.github.repir.Strategy.Tools.MatchSetLength;
import io.github.repir.Strategy.Tools.ProximitySet.ProximityTerm;
import io.github.repir.Strategy.Tools.ProximityUnorderedSet;
import io.github.htools.lib.Log;
import java.util.ArrayList;

/**
 * This {@link ProximityOperator} counts co-occurrence of the contained
 * {@link Operator}s in the same document, in any order. 
 * <p/>
 * @author jeroen
 */
public class ProximityOperatorUnorderedOverlap extends ProximityOperatorUnordered {

   public static Log log = new Log(ProximityOperatorUnorderedOverlap.class);

   public ProximityOperatorUnorderedOverlap(GraphRoot root, ArrayList<Operator> list) {
      super(root, list);
      termpositions.dontoverlap = false;
   }

   @Override
   public Operator clone(GraphRoot newmodel) {
      ArrayList<Operator> list = new ArrayList<Operator>();
      for (Operator n : containednodes) {
         list.add(n.clone(newmodel));
      }      
      ProximityOperatorUnorderedOverlap f = new ProximityOperatorUnorderedOverlap(newmodel, list);
      f.parent = parent;
      return f;
   }  
}
