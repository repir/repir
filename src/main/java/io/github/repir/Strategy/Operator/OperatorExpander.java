package io.github.repir.Strategy.Operator;

import io.github.repir.Retriever.Document;
import io.github.repir.Strategy.GraphRoot;
import io.github.htools.lib.Log;
import java.util.ArrayList;

/**
 * An expander is used to describe a list of features that is converted into a set of
 * new features before retrieval. An example is adding all possible combinations
 * of two or more query terms as a {@link ProxmityOperator}. The expander must implement
 * {@link #replaceWith()} to return the list of {@link Operator}s to use in its place. 
 * The expander itself will be removed at the end of the expand phase of the 
 * initialization cycle of the {@link GraphRoot}.
 * <p/>
 * @author jeroen
 */
public abstract class OperatorExpander extends Operator {

   public static Log log = new Log(OperatorExpander.class);
   double weight_fi;

   public OperatorExpander(GraphRoot im, ArrayList<Operator> list) {
      super(im, list);
   }

   public abstract ArrayList<Operator> replaceWith();
   
   public void doAnnounce() {
      log.fatal("FeatureExpander cannot remain in the Graph");
   }
   
   @Override
   public boolean expand() {
      ArrayList<Operator> list = replaceWith();
      containednodes = new ArrayList<Operator>();
      for (Operator n : list) { 
         this.add(n);
      }
      return true;
   }

   @Override
   public void process(Document doc) {
      log.fatal("An expander cannot remain in the retrieval model, but must be removed at the end if init()");
   }

   @Override
   public Operator clone(GraphRoot newmodel) {
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
