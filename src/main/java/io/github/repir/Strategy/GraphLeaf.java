package io.github.repir.Strategy;

import io.github.repir.Retriever.Document;

/**
 * A rootnode represents a stored feature that was retrieved, which is converted
 * into a GraphNode for processing, but is not used to score directly. Rather,
 * it is contained within another feature (e.g. FeatureTerm, FeaturePhrase) for
 * scoring. The same GraphLeaf can be reused within multiple other FeatureNodes.
 * For text retrieval, Term is a commonly used implementation of GraphLeaf.
 * @author jeroen
 */
public abstract class GraphLeaf extends GraphNode {

   protected GraphLeaf(GraphRoot root) {
      super(root);
   }

   /**
    * A Term cannot contain other features.
    */
   @Override
   public void add(GraphNode f) {
      log.fatal("Cannot add a feature to an GraphLeaf");
   }

//   @Override
//   public Double score(Document doc) {
//      log.info("cannot call Score(doc) on Term");
//      this.root.print();
//      log.crash();
//      return null;
//   }
   
   public void prepareRetrieval() {}
   
   /**
    * @return false, i.e. no statistics have to be collected for this
    * feature, as these usually directly correspond to a stored feature.
    */
   @Override
   public boolean needsCollect() {
      return false;
   }
}
