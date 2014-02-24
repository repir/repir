package io.github.repir.Strategy;

import io.github.repir.tools.Lib.Log;
import io.github.repir.Repository.Repository;
import java.util.ArrayList;

/**
 * A GraphNode is a node in an {@link GraphRoot}. The containedfeatures will form
 * a network that can process each retrieved document resulting in a document
 * process. The root containedfeatures should extract values from the document (e.g. the
 * frequency of a term). Processing containedfeatures are linked to one or more other
 * containedfeatures, from which they inspect their {@link FeatureValues} and
 * process the values into a {@link FeatureValues} of their own. The
 * GraphRoot by default Sums the scores of the leaf containedfeatures, so any
 * retrieval model that requires a different aggregation function should either
 * convert their scoring so they can be summed (e.g. log-space for language
 * models) or inserting a final leaf feature that performs the aggregation.
 */
public abstract class GraphComponent {

   public static Log log = new Log(GraphComponent.class);
   public ArrayList<GraphNode> containedfeatures = new ArrayList<GraphNode>();
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
   
   public abstract void announce(ANNOUNCEKEY key, GraphNode node);
   
   /**
    * recursively removes a feature from the contained containedfeatures
    * <p/>
    * @param remove the feature to remove
    */
   public void remove(GraphNode remove) {
      for (int f = 0; f < containedfeatures.size(); f++) {
         GraphNode feature = containedfeatures.get(f);
         if (feature == remove) {
            containedfeatures.remove(f--);
         } else {
            feature.remove(remove);
            if (!(feature instanceof Term) && feature.containedfeatures.isEmpty()) {
               containedfeatures.remove(f--);
            }
         }
      }
   }

    /**
    * @param search the GraphNode to be recursively replaced in the GraphRoot
    * @param replace a list of containedfeatures to replace every matching GraphNode
    * with
    */
   public void replace(GraphNode replace, ArrayList<GraphNode> insert) {
      for (int f = containedfeatures.size() - 1; f >= 0; f--) {
         GraphNode feature = containedfeatures.get(f);
         if (feature == replace) {
            containedfeatures.remove(f);
            if (insert != null) {
               containedfeatures.addAll(f, insert);
               for (GraphNode node : insert)
                  node.parent = this;
            }
         } else {
            feature.replace(replace, insert);
         }
      }
   }

   public void replace(GraphNode replace, GraphNode insert) {
      ArrayList<GraphNode> list = new ArrayList<GraphNode>();
      list.add(insert);
      replace(replace, list);
   }
   
   public void doAnnounceContainedFeatures() {
      for (int f = containedfeatures.size() - 1; f >= 0; f--) {
         containedfeatures.get(f).doAnnounceContainedFeatures();
      }
   }   
   
   public void doExpand() {
      for (int f = containedfeatures.size() - 1; f >= 0; f--) {
         containedfeatures.get(f).doExpand();
      }
   }   
   
   public void doConfigureContainedFeatures() {
      for (int f = containedfeatures.size() - 1; f >= 0; f--) {
         containedfeatures.get(f).doConfigureContainedFeatures();
      }
   }
   
   public void passWillBeScored( boolean willbescored ) {
      for (int f = containedfeatures.size() - 1; f >= 0; f--) {
         containedfeatures.get(f).passWillBeScored( willbescored );
      }
   }
   
   public void termPositionsNeeded( boolean positional ) {
      for (int f = containedfeatures.size() - 1; f >= 0; f--) {
         containedfeatures.get(f).termPositionsNeeded( positional );
      }
   }
   
   public void doReadStatistics( ) {
      for (int f = containedfeatures.size() - 1; f >= 0; f--) {
         containedfeatures.get(f).doReadStatistics( );
      }
   }
   
   public void doSetupCollector( ) {
      for (int f = containedfeatures.size() - 1; f >= 0; f--) {
         containedfeatures.get(f).doSetupCollector( );
      }    
   }
   
   public void doPrepareRetrieval( ) {
      for (int f = containedfeatures.size() - 1; f >= 0; f--) {
         containedfeatures.get(f).doPrepareRetrieval( );
      }    
   }
   
   /**
    * Recursive search for a feature in the GraphRoot, using equals method.
    * <p/>
    * @param needle GraphNode that has same properties as the one to search
    * for i.e. that is used in the equals method.
    * @return GraphNode that is equal to the needle, or null if not exists.
    */
   public GraphNode find(GraphNode needle) {
      GraphNode found = null;
      for (GraphNode f : containedfeatures) {
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
    * @param containedfeatures list of Features
    * @return a space separated representation of the list of containedfeatures
    */
   public static String toTermString(int id, ArrayList<GraphNode> features) {
      StringBuilder sb = new StringBuilder();
      for (int i = 0; i < features.size(); i++) {
         if ((id & (1 << i)) > 0) {
            sb.append(features.get(i).toTermString()).append(" ");
         }
      }
      return sb.toString();
   }

   /**
    * @param f GraphNode to be added to this GraphNode's contained Features.
    */
   public void add(GraphNode f) {
      containedfeatures.add(f);
      f.parent = this;
   }
   
   public void add(ArrayList<GraphNode> list) {
      for (GraphNode n : list)
         add( n );
   }
   
   public ArrayList<Term> getStopWords() {
      ArrayList<Term> results = new ArrayList<Term>();
      for (GraphNode g : containedfeatures) {
         if (g instanceof Term && ((Term)g).isstopword) {
            results.add((Term)g);
         }
      }
      return results;
   }
   
   public ArrayList<Term> getNonStopWords() {
      ArrayList<Term> results = new ArrayList<Term>();
      for (GraphNode g : containedfeatures) {
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
