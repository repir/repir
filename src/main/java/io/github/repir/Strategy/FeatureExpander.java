package io.github.repir.Strategy;

import io.github.repir.Retriever.Document;
import io.github.repir.tools.Lib.Log;
import java.util.ArrayList;

/**
 * An expander is used to describe a list of features that is converted into a set of
 * new features before retrieval. An example is adding all possible combinations
 * of two or more query terms as a phrase. The expander uses the list of contained features
 * (usually received from the query parser), and implements the expand() method to 
 * return a list of generated new features. The expander itself will be removed at the end
 * of the initialization cycle.
 * <p/>
 * @author jeroen
 */
public abstract class FeatureExpander extends GraphNode {

   public static Log log = new Log(FeatureExpander.class);
   double weight_fi;

   public FeatureExpander(GraphRoot im, ArrayList<GraphNode> list) {
      super(im, list);
   }

   public abstract ArrayList<GraphNode> replaceWith();
   
   @Override
   public void doAnnounceContainedFeatures() {
      doAnnounce();
   }
   
   public void doAnnounce() {
      log.fatal("FeatureExpander cannot remain in the Graph");
   }
   
   @Override
   public boolean expand() {
      ArrayList<GraphNode> list = replaceWith();
      containedfeatures = new ArrayList<GraphNode>();
      for (GraphNode n : list) { 
         this.add(n);
      }
      return true;
   }

   @Override
   public void process(Document doc) {
      log.fatal("An expander cannot remain in the retrieval model, but must be removed at the end if init()");
   }

   @Override
   public GraphNode clone(GraphRoot newmodel) {
      log.fatal("An expander cannot remain in the retrieval model, but must be removed at the end if init()");
      return null;
   }

   @Override
   public String postReform() {
      return super.postReform();
   }
   
   @Override
   public String postReformUnweighted() {
      return super.postReformUnweighted();
   }   
   
   @Override
   public void prepareRetrieval() {
      throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
   }

   @Override
   public void configureFeatures() {
      throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
   }
}
