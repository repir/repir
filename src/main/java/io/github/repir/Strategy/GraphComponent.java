package io.github.repir.Strategy;

import io.github.repir.Strategy.Operator.Term;
import io.github.repir.Strategy.Operator.Operator;
import io.github.repir.tools.Lib.Log;
import io.github.repir.Repository.Repository;
import java.util.ArrayList;

/**
 * For {@link RetrievalModel} {@link Strategy}s, a Graph is constructed in which 
 * the nodes are {@link Operator}s that hierarchically process the results. 
 * The {@link GraphRoot} is not an {@link Operator}, but controls the construction
 * of the Graph and provides access and facilities for its nodes. {@link GraphComponent}
 * describes commonalities for all nodes.
 * <p/>
 * Components should announce their modes through {@link ANNOUNCEKEY}s, which
 * are communicated via {@link #announce(io.github.repir.Strategy.GraphComponent.ANNOUNCEKEY, io.github.repir.Strategy.Operator.Operator)}
 * via the edges towards the root.
 * By communicating via the edges, the parent nodes can overrule the how its
 * children are used. For instance, a Term is {@link ScoreFunction.Scorable} by
 * default, however, if the Term appears in a {@link ProximityOperator}, the entire
 * operator should be scored and not the individual terms; ProximityOperator should
 * therefore block the SCORABLE value send by its childeren.
 * <p/>
 * Some other information is passed from the {@link GraphRoot} via the edges, like
 * setWillBeScored and setTermPositionsNeeded. If setWillBeScored reaches a node
 * with true, it will know to request the statistics needed for scoring. If the children
 * of a node should not be scored, false is passed to their childeren. setTermPositions
 * is send with the value false from the root, if a node needs term positions, it 
 * should switch the value to true. The receiving terms will then know to request
 * positional postings lists. Atm term positions are always used by default.
 * <p/>
 * GraphComponent complies to some standard facilities to remove or replace 
 * nodes in the graph. By communicating these via the edges as a request, this
 * enables the nodes to override default behavior.
 */
public abstract class GraphComponent {

   public static Log log = new Log(GraphComponent.class);
   public ArrayList<Operator> containednodes = new ArrayList<Operator>();
   public GraphComponent parent;
   public RetrievalModel retrievalmodel;
   public Repository repository;

   protected GraphComponent(RetrievalModel retrievalmodel) {
      this.retrievalmodel = retrievalmodel;
      this.repository = retrievalmodel.repository;
   }
   
   protected GraphComponent(Repository repository) {
      this.repository = repository;
   }
   
   public abstract void announce(ANNOUNCEKEY key, Operator node);
   
   /**
    * recursively removes a feature from the contained containednodes
    * <p/>
    * @param remove the feature to remove
    */
   public void remove(Operator remove) {
      for (int f = 0; f < containednodes.size(); f++) {
         Operator feature = containednodes.get(f);
         if (feature == remove) {
            containednodes.remove(f--);
         } else {
            feature.remove(remove);
            if (!(feature instanceof Term) && feature.containednodes.isEmpty()) {
               containednodes.remove(f--);
            }
         }
      }
   }

    /**
    * @param search the Operator to be recursively replaced in the GraphRoot
    * @param replace a list of containednodes to replace every matching Operator
    * with
    */
   public void replace(Operator replace, ArrayList<Operator> insert) {
      for (int f = containednodes.size() - 1; f >= 0; f--) {
         Operator feature = containednodes.get(f);
         if (feature == replace) {
            containednodes.remove(f);
            if (insert != null) {
               containednodes.addAll(f, insert);
               for (Operator node : insert)
                  node.parent = this;
            }
         } else {
            feature.replace(replace, insert);
         }
      }
   }

   public void replace(Operator replace, Operator insert) {
      ArrayList<Operator> list = new ArrayList<Operator>();
      list.add(insert);
      replace(replace, list);
   }
   
   public void doAnnounceContainedFeatures() {
      for (int f = containednodes.size() - 1; f >= 0; f--) {
         containednodes.get(f).doAnnounceContainedFeatures();
      }
   }   
   
   public void doExpand() {
      for (int f = containednodes.size() - 1; f >= 0; f--) {
         containednodes.get(f).doExpand();
      }
   }   
   
   public void doConfigureContainedFeatures() {
      for (int f = containednodes.size() - 1; f >= 0; f--) {
         containednodes.get(f).doConfigureContainedFeatures();
      }
   }
   
   public void setWillBeScored( boolean willbescored ) {
      for (int f = containednodes.size() - 1; f >= 0; f--) {
         containednodes.get(f).setWillBeScored( willbescored );
      }
   }
   
   public void setTermPositionsNeeded( boolean positional ) {
      for (int f = containednodes.size() - 1; f >= 0; f--) {
         containednodes.get(f).setTermPositionsNeeded( positional );
      }
   }
   
   public void doReadStatistics( ) {
      for (int f = containednodes.size() - 1; f >= 0; f--) {
         containednodes.get(f).doReadStatistics( );
      }
   }
   
   public void doSetupCollector( ) {
      for (int f = containednodes.size() - 1; f >= 0; f--) {
         containednodes.get(f).doSetupCollector( );
      }    
   }
   
   public void doPrepareRetrieval( ) {
      for (int f = containednodes.size() - 1; f >= 0; f--) {
         containednodes.get(f).doPrepareRetrieval( );
      }    
   }
   
   /**
    * Recursive search for a feature in the GraphRoot, using equals method.
    * <p/>
    * @param needle Operator that has same properties as the one to search
    * for i.e. that is used in the equals method.
    * @return Operator that is equal to the needle, or null if not exists.
    */
   public Operator find(Operator needle) {
      Operator found = null;
      for (Operator f : containednodes) {
         if (f.getClass().equals(needle.getClass()) && f.equals(needle)) {
            found = f;
            break;
         } else {
            found = f.find(needle);
         }
         if (found != null) {
            break;
         }
      }
      return found;
   }

   /**
    * @param containednodes list of Features
    * @return a space separated representation of the list of containednodes
    */
   public static String toTermString(int id, ArrayList<Operator> features) {
      StringBuilder sb = new StringBuilder();
      for (int i = 0; i < features.size(); i++) {
         if ((id & (1 << i)) > 0) {
            sb.append(features.get(i).toTermString()).append(" ");
         }
      }
      return sb.toString();
   }

   /**
    * @param f Operator to be added to this Operator's contained Features.
    */
   public void add(Operator f) {
      containednodes.add(f);
      f.parent = this;
   }
   
   public void add(ArrayList<Operator> list) {
      for (Operator n : list)
         add( n );
   }
   
   public ArrayList<Term> getStopWords() {
      ArrayList<Term> results = new ArrayList<Term>();
      for (Operator g : containednodes) {
         if (g instanceof Term && ((Term)g).isstopword) {
            results.add((Term)g);
         }
      }
      return results;
   }
   
   public ArrayList<Term> getNonStopWords() {
      ArrayList<Term> results = new ArrayList<Term>();
      for (Operator g : containednodes) {
         if (g instanceof Term && !((Term)g).isstopword) {
            results.add((Term)g);
         }
      }
      return results;
   }
   
   public enum ANNOUNCEKEY {

      TERM,
      STOPWORD, // with intent to remove
      UNUSED, // with intent to remove
      NONEXIST, // with intent to remove
      NEEDSCOLLECT,
      NEEDSCACHECOLLECT,
      REMOVE,
      COMPLETED,
      SCORABLE
   }
}
