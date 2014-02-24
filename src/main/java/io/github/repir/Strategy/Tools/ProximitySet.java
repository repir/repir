package io.github.repir.Strategy.Tools;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import io.github.repir.Retriever.Document;
import io.github.repir.Strategy.FeatureValues;
import io.github.repir.Strategy.GraphNode;
import io.github.repir.Strategy.Term;
import io.github.repir.tools.Lib.ArrayTools;
import io.github.repir.tools.Lib.Log;
import io.github.repir.tools.Lib.MathTools;

/**
 *
 * @author Jeroen Vuurens
 */
public abstract class ProximitySet {

   public static Log log = new Log(ProximitySet.class);
   public ProximityTerm[] tpi;
   public long[] dependency;
   ArrayList<GraphNode> containedfeatures;
   public ProximityTermDupl duplicateof[];
   final protected ProximityTermList ZEROLIST = new ProximityTermList();
   public ProximityTermList proximitytermlist;
   public ProximityTerm first;
   public long presentterms;

   public ProximitySet(ArrayList<GraphNode> containedfeatures) {
      this.containedfeatures = containedfeatures;
      tpi = new ProximityTerm[containedfeatures.size()];
      duplicateof = new ProximityTermDupl[containedfeatures.size()];
      dependency = getDependence();
      for (int i = 0; i < containedfeatures.size() - 1; i++) {
         if (tpi[i] == null && containedfeatures.get(i) instanceof Term) {
            Term ti = (Term) containedfeatures.get(i);
            for (int j = i + 1; j < containedfeatures.size(); j++) {
               if (containedfeatures.get(j) instanceof Term && ((Term) containedfeatures.get(j)).storefeature == ti.storefeature) {
                  if (tpi[i] == null) {
                     tpi[i] = new ProximityTermDupl(containedfeatures.get(i).getFeatureValues(), i, containedfeatures.get(i).span);
                     duplicateof[i] = (ProximityTermDupl) tpi[i];
                     duplicateof[i].dupl = new ArrayList<ProximityTermDupl>();
                     duplicateof[i].dupl.add(duplicateof[i]);
                  }
                  tpi[j] = new ProximityTermDupl(containedfeatures.get(j).getFeatureValues(), j, containedfeatures.get(j).span);
                  duplicateof[j] = (ProximityTermDupl) tpi[i];
                  duplicateof[i].dupl.add((ProximityTermDupl) tpi[j]);
               }
            }
            if (tpi[i] != null) {
               for (ProximityTermDupl d : duplicateof[i].dupl) {
                  d.setDuplicates(duplicateof[i]);
               }
            }
         }
      }
      for (int i = 0; i < containedfeatures.size(); i++) {
         if (tpi[i] == null) {
            tpi[i] = new ProximityTerm(containedfeatures.get(i).getFeatureValues(), i, containedfeatures.get(i).span);
         }
      }
      for (ProximityTerm t : tpi) {
         if (t instanceof ProximityTermDupl) {
            ProximityTermDupl d = (ProximityTermDupl) t;
            d.setDuplicateDependency();
            for (int i = 0; i < d.dependency.length; i++) {
               d.dependency[i] = this.convertDuplicatesInPattern(d.dependency[i], d.sequence);
            }
         }
      }
      for (ProximityTerm t : tpi) {
         t.setDependency(dependency[t.sequence]);
      }
   }

   /**
    * transforms a dependency pattern, shifting ids of duplicates so that each
    * number of n contained duplicates remains the same, but is converted to the
    * first n of that set of duplicates. This is necessary because of two
    * duplicates, the second begins at the second position in the document.
    *
    * @param id
    * @return
    */
   public long convertDuplicatesInPattern(long id) {
      return convertDuplicatesInPattern(id, -1);
   }

   public long convertDuplicatesInPattern(long id, int sequence) {
      long modifiedid = id;
      HashMap<Integer, Integer> duplicates = new HashMap<Integer, Integer>();
      long bit = 1;
      for (int p = 0; p < containedfeatures.size(); p++, bit <<= 1) {
         if ((id & bit) != 0 && tpi[p] instanceof ProximityTermDupl) {
            // for duplicate terms we have to make sure to use the first values in the list
            // i.e. transform the id mask so that the n occurences of term x are
            // replaced with the first n occurrences of term x
            int firstid = ((ProximityTermDupl)tpi[p]).first.sequence;
            Integer count = duplicates.get(firstid);
            if (count == null) {
               duplicates.put(firstid, 1);
            } else {
               duplicates.put(firstid, count + 1);
            }
            modifiedid -= bit;
         }
      }
      for (Map.Entry<Integer, Integer> e : duplicates.entrySet()) {
         boolean foundmyself = false;
         ProximityTermDupl d = (ProximityTermDupl) tpi[e.getKey()];
         if (e.getKey() != sequence) {
            modifiedid |= d.bitsequence;
         } else {
            foundmyself = true;
         }
         for (int i = 1; i < e.getValue(); i++) {
            if (d.sequence != sequence) {
               modifiedid |= d.dupl.get(i).bitsequence;
            } else {
               foundmyself = true;
            }
         }
         if (foundmyself && e.getValue() < d.dupl.size()) {
            modifiedid |= d.dupl.get(e.getValue()).bitsequence;
         }
      }
      return modifiedid;
   }

   protected long[] getDependence() {
      return new long[containedfeatures.size()];
   }

   public abstract boolean hasProximityMatches(Document doc);

   public abstract boolean next();

   protected void pollFirst() {
      first = proximitytermlist.pollFirst();
   }

   public int size() {
      return tpi.length;
   }

   public String toString() {
      StringBuilder sb = new StringBuilder();
      for (int i = 0; i < containedfeatures.size(); i++) {
         sb.append(containedfeatures.get(i)).append(" ").append(tpi[i]);
      }
      return sb.toString();

   }

   public class ProximityTerm implements Comparable<ProximityTerm> {

      FeatureValues featurevalues;
      int[] position;
      public int sequence;
      public long bitsequence;
      public long alldependency;
      public int span;
      int previous;
      public int current;
      public int next;
      public long dependency;
      int p;

      protected ProximityTerm(FeatureValues values, int sequence, int span) {
         this.featurevalues = values;
         this.sequence = sequence;
         this.bitsequence = (1l << sequence);
         this.span = span;
      }

      public void setDependency( long dependency ) {
         this.dependency = dependency;
         this.alldependency = dependency | bitsequence;
      }
      
      protected void reset() {
         this.position = featurevalues.pos;
         p = 0;
         previous = Integer.MIN_VALUE;
         if (position.length == 0) {
            current = Integer.MAX_VALUE;
            next = Integer.MAX_VALUE;
         } else if (position.length == 1) {
            current = position[p++];
            next = Integer.MAX_VALUE;
         } else {
            current = position[p++];
            next = position[p++];
         }
      }

      public boolean satisfiesDependency(long pattern) {
         return (dependency & pattern) == dependency;
      }
      
      public int peek() {
         return next;
      }
      
      protected int next() {
         previous = current;
         current = next;
         next = (p < position.length) ? position[p++] : Integer.MAX_VALUE;
         return current;
      }

      final protected int move() {
         previous = current;
         current = next;
         next = (p < position.length) ? position[p++] : Integer.MAX_VALUE;
         return current;
      }

      protected void moveFirstBelowNext() {
         int nextpos = proximitytermlist.first().current;
         while (next < nextpos) {
            next();
         }
      }

      @Override
      public int compareTo(ProximityTerm o) {
         //log.info("compareTo %d %d", current, o.current);
         if (current == Integer.MAX_VALUE) {
            if (o.current == Integer.MAX_VALUE) {
               return 0;
            } else {
               return 1;
            }
         } else {
            if (o.current == Integer.MAX_VALUE) {
               return -1;
            } else {
               return current - o.current;
            }
         }
      }

      @Override
      public String toString() {
         return Integer.toString(current);
      }
   }

   public class ProximityTermDupl extends ProximityTerm {

      public ArrayList<ProximityTermDupl> dupl;
      protected long[] dependency;
      ProximityTermDupl first, previousdupl, nextdupl, last;
      private int initshift = 0;

      protected ProximityTermDupl(FeatureValues values, int sequence, int span) {
         super(values, sequence, span);
      }

      protected void setDuplicates(ProximityTermDupl first) {
         this.first = first;
         this.last = first.dupl.get(first.dupl.size() - 1);
         int pos = 0;
         for (; pos < first.dupl.size() && first.dupl.get(pos) != this; pos++) {
         }
         if (pos > 0) {
            previousdupl = first.dupl.get(pos - 1);
         }
         initshift = pos;
         if (++pos < first.dupl.size()) {
            nextdupl = first.dupl.get(pos);
         }
      }

      public int peek() {
         return last.next;  
      }
      
      protected void setDuplicateDependency() {
         ArrayList<ProximityTermDupl> list = first.dupl;
         HashSet<Long> dep = new HashSet<Long>();
         for (int i = (1 << first.dupl.size()) - 1; i > 0; i--) {
            if (MathTools.numberOfSetBits(i) == (initshift + 1)) {
               long pattern = 0;
               int b = 1;
               for (int j = 0; b <= i; j++, b <<= 1) {
                  if ((i & b) != 0) {
                     pattern |= ProximitySet.this.dependency[first.dupl.get(j).sequence];
                  }
               }
               dep.add(convertDuplicatesInPattern(pattern, sequence));
            }
         }
         HashSet<Long> dep1 = new HashSet<Long>();
         SKIP:
         for (long d : dep ) {
            for (long m : dep) {
               if (m != d && (d & m) == m) 
                  continue SKIP;
            }
            dep1.add(d);
         }         
         dependency = ArrayTools.toLongArray(dep1);
      }

      public boolean satisfiesDependency(long pattern) {
         for (long d : dependency) {
            if ((pattern & d) == d) {
               return true;
            }
         }
         return false;
      }

      @Override
      protected void reset() {
         position = featurevalues.pos;
         p = initshift;
         previous = (p < position.length && p > 0) ? position[p - 1] : Integer.MIN_VALUE;
         current = (p < position.length) ? position[p++] : Integer.MAX_VALUE;
         next = (p < position.length) ? position[p++] : Integer.MAX_VALUE;
      }

      @Override
      protected int next() {
         if (dupl != null) {
            for (ProximityTermDupl t : dupl) {
               if (t != this && t.current < Integer.MAX_VALUE) {
                  proximitytermlist.remove(t);
                  t.next();
                  if (t.current < Integer.MAX_VALUE) {
                     proximitytermlist.add(t);
                  } else if (proximitytermlist.size() == 0)
                     return Integer.MAX_VALUE;
               }
            }
         }
         return super.next();
      }

      @Override
      protected void moveFirstBelowNext() {
         int nextpos = proximitytermlist.first().current;
         if (next == this.nextdupl.current) {
            int count = 0;
            ProximityTerm target = null;
            for (ProximityTerm t : proximitytermlist) {
               if (++count == dupl.size()) {
                  target = t;
                  break;
               }
               if (duplicateof[t.sequence] != this) {
                  break;
               }
            }
            if (target != null) {
               while (last.next < target.current) {
                  next();
               }
            }
         }
      }
   }
}
