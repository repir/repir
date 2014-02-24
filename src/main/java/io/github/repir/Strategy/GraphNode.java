package io.github.repir.Strategy;

import io.github.repir.Retriever.Document;
import io.github.repir.tools.Lib.ClassTools;
import io.github.repir.tools.Lib.Log;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import io.github.repir.Strategy.Collector.Collector;
import io.github.repir.Repository.Repository;
import io.github.repir.Repository.TermDocumentFeature;
import io.github.repir.tools.Lib.MathTools;
import io.github.repir.tools.Lib.StrTools;

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
public abstract class GraphNode extends GraphComponent implements Comparator<GraphNode> {

   public static Log log = new Log(GraphNode.class);
   public GraphRoot root;
   public boolean positional = false;
   
   // indicates whether a node will be scored using a ScoreFunction or not, which may affect the
   // needed stored features
   public boolean willbescored = false;
   public final FeatureValues featurevalues;
   public int span = 1;
   public int sequence = -1; // can be used to indicate it is the n-th node, not set by default

   /**
    * Features can only exists as part of an {@link GraphRoot}
    * <p/>
    * @param root
    */
   protected GraphNode(GraphRoot root) {
      super(root.retrievalmodel);
      this.root = root;
      featurevalues = new FeatureValues();
   }
   
   protected GraphNode(Repository repository) {
      super(repository);
      featurevalues = new FeatureValues();
   }
   
   /**
    * Constructs a feature with a list of contained containedfeatures.
    * <p/>
    * @param root
    * @param containedfeatures
    */
   protected GraphNode(GraphRoot root, ArrayList<GraphNode> containedfeatures) {
      this(root);
      for (GraphNode f : containedfeatures) {
         add(f);
      }
   }
   
//   @Override
//   public int hashCode() {
//      int h = 31;
//      for (GraphNode n : containedfeatures)
//         h = MathTools.combineHash(h, n.hashCode());
//      return MathTools.finishHash( h );
//   }
   
   @Override
   public boolean equals(Object o) {
      return this == o;
   }
   
   /**
    * By default, features need to collect if a ScoreFunction will be used on them and they 
    * require corpus statistics that are not available in the repository.
    */
   public boolean needsCollect() {
      return (willbescored)
              && (featurevalues.corpusfrequency < 0 || featurevalues.documentfrequency < 0);
   }
      
   /**
    * GraphNodes at root level are asked to set the dependencies of the
    * TermDocumentFeatures they use. This helps the PostingsIterator to
    * determine whether a document should be scored or not. Independent terms
    * should set the dependencies to an empty array to indicate no dependencies
    * while proximity operators set the dependencies of each term to the other
    * terms. If a document contains only a subset of terms from one proximity operator
    * the document is not evaluated because it has no scorable components.
    */
   public void setTDFDependencies() {
   }

   public ArrayList<TermDocumentFeature> getRequiredTDF() {
      return new ArrayList<TermDocumentFeature>();
   }
   
   /**
    * By default, all features required by collectors are asked to do a final
    * setup. Typically, to setup the prepareRetrieval method is overridden, there is only
    * need to override the doPrepareRetrieval to intervene in the prepareRetrieval of contained 
    * features.
    */
   @Override
   public void doPrepareRetrieval() {
       super.doPrepareRetrieval();
       prepareRetrieval();
   }
   
   /**
    * If a node receives this call, it is being used for retrieval and should 
    * prepare so. Typically, this is the method in which the node puts in 
    * requests for the StoredFeatures that are required.
    */
   protected abstract void prepareRetrieval();
   
   /**
    * Override and return true if the collected results in a pre pass will be stored
    * @return 
    */
   public boolean isCachable() {
      return false;
   }
   
   /**
    * All nodes communicate their mode of operation to the GraphRoot via their 
    * containing nodes. This allows the containing nodes to modify the mode
    * of operation for their contained nodes, e.g. terms are normally SCORABLE,
    * but a containing phrase can overrule that, to score the phrase 
    * as a unit.
    * @param key purpose of a node
    * @param node the node
    */
   @Override
   public void announce( ANNOUNCEKEY key, GraphNode node ) {
      parent.announce(key, node);
   }

   public void moveUp( ) {
      parent = parent.parent;
      parent.add(this);
   }

   /**
    * During the initialization cycle, all containedfeatures in the graph must initialize 
    * their values (i.e. set their constant values, such as corpus frequency) and
    * announce themselves (call announce with all ANNOUNCEKEY values that apply, 
    * e.g. STOPWORD, TERM). The SCORABLE announcement is made by default
    * if the feature implements the FeatureScorable interface.
    * <p/>
    * Implementing containedfeatures should recursively call contained containedfeatures as well.
    * If a feature requires a positional posting list to be retrieved, it should
    * set positional to true. Features also pass along to their nested features if they will be
    * scored using a ScoreFunction, which may require them to request additional statistics 
    * to be retrieved. E.g. FeaturePhrase and FeatureSynonym do not score their contained features
    * with ScoreFunction, but only look at their occurrence in the document.
    * <p/>
    * @param positional containedfeatures that require positional posting lists should
    * turn this to true
    */
   
   /*
    * Through this function FeatureNodes receive whether word positions
    * are needed for retrieval. By returning true, they can indicate that
    * they need word positions themselves. Nodes should obey by providing
    * word positions through their FeatureValues if requested.
    */
   public boolean positional( boolean positional ) {
      this.positional = positional;
      return positional;
   }
   
   @Override
   public final void termPositionsNeeded( boolean positional ) {
      super.termPositionsNeeded(positional( positional ));
   }
   
   @Override
   public void doExpand() {
      super.doExpand();
      if (expand()) {
         for (GraphNode g : containedfeatures)
            g.moveUp();
         root.remove(this);
      }
   }   
   
   /**
    * Prior to Graph pre-processing, nodes can expand by setting up their contained features
    * as their expand. By returning TRUE, they indicate that they should be removed 
    * from the graph and replaced by their contained features. This applies to
    * automatic expansion nodes that generate sub nodes, but also to GraphPhrase
    * and GraphSynonym that can leave the Graph if they contain less than 2
    * elements.
    * @return 
    */
   public boolean expand() {
      return false;
   }
   
   @Override
   public void doAnnounceContainedFeatures() {
      doAnnounce();
      super.doAnnounceContainedFeatures();
   }   
   
   /**
    * All nodes are requested by a call to this method to announce themselves.
    * Typically it responds with calls to parent.announce once for every 
    * appropriate ANNOUNCETYPE. By default, intermediate nodes with pass these
    * announcements to their parent until the root receives them. However, an
    * intermediate node can change/stop the announcement, e.g. Term will typically
    * announce themselves as Scorable and if appropriate as stopword, however in 
    * a FeatureProximity stopwords may be allowed and the Terms are not scored but the
    * proximity operator as a whole. Therefore FeatureProximity does not relay 
    * Scorable and Stopword announcements from its children.
    */
   public abstract void doAnnounce();
   
   @Override
   public final void doConfigureContainedFeatures() {
      super.doConfigureContainedFeatures();
      configureFeatures(); 
   }
   
   /*
    * Pre process contained features and featurevalues, e.g. set their
    * configuration correctly (e.g. the span of a phrase) or sort
    * their contained features (for synonyms)
    */
   public abstract void configureFeatures();
   
   /**
    * This methods controls whether nodes will prepare themselves to be scored.
    * By default this is called with true on nodes directly connected to the root,
    * who will return FALSE so that their contained nodes do not prepare to be scored.
    * e.g. a phrase will be scored as a whole unit and its contained terms not as
    * separate nodes. A scenario in which this should be overridden is when the
    * contained nodes need to be scored, for instance when the parent node adds
    * a separate score or changes its sub nodes values before scoring.
    */
   public boolean willBeScored( boolean willbescored ) {
      this.willbescored |= willbescored;
      return false;
   }
   
   @Override
   public final void passWillBeScored( boolean willbescored ) {
      super.passWillBeScored( willBeScored( willbescored ));
   }
   
   /**
    * If a node needs statistics that are stored in the collection, it should override
    * this method by code that reads these statistics. e.g. FeatureProximity and
    * FeatureSynonymn store their collected statistics in a separate file, 
    * so that once collected the same query does not require a pre pass.
    */
   public void readStatistics() {}
   
   @Override
   public final void doReadStatistics( ) {
      super.doReadStatistics();
      if (willbescored)
         readStatistics();
   }
   
   public void setupCollector() { }
   
   @Override
   public final void doSetupCollector( ) {
      super.doSetupCollector();
      if (this.needsCollect())
         setupCollector();
   }

   /**
    * All Features should implement a processing step of which the results are
    * stored in the GraphNode's {@link FeatureValues}.
    * <p/>
    * @param doc
    * @return
    */
   public abstract void process(Document doc);

   public void addReport(Document doc, double docscore) {
      if (featurevalues.frequency > 0) {
         doc.addReport("[%s] %e %e\n", this.toTermString(), featurevalues.frequency, docscore);
      }
   }

   public abstract GraphNode clone(GraphRoot newmodel);

   /**
    * @return a string of the reformulated GraphNode, that may be an altered
    * or expanded version of the input.
    */
   public String postReform() {
      return GraphRoot.reformulate(this);
   }

   /*
    * @return like postReform(), but no weights are added. This can be used to
    * reform GraphNodes inside other nodes.
    */
   public String postReformUnweighted() {
      return GraphRoot.reformulateUnweighted(this);
   }

   // clone recursively, using the cloned objects as returned by the Strategy
   // feedback function
   public GraphNode clone(GraphRoot newmodel, int cycle) {
      GraphNode newfeature = root.retrievalmodel.cloneFeature(this, newmodel, cycle);
      if (newfeature != null) {
         //log.info("%s %d", newfeature.toClass().getCanonicalName(), containedfeatures.size());
         for (GraphNode f : containedfeatures) {
            //log.info("clone() %s %s", this.toTermString(), containedfeatures);
            GraphNode nf = root.retrievalmodel.cloneFeature(f, newmodel, cycle);
            if (nf != null) {
               newfeature.add(nf);
            }
            //log.info("%s", containedfeatures);
         }
      }
      return newfeature;
   }

   /**
    * @param containedfeatures list of Features
    * @return a space separated representation of the list of containedfeatures
    */
   public static String toTermString(ArrayList<GraphNode> features) {
      StringBuilder sb = new StringBuilder();
      Iterator<GraphNode> fi = features.iterator();
      while (fi.hasNext()) {
         sb.append(fi.next().toTermString());
         if (fi.hasNext()) {
            sb.append(' ');
         }
      }
      return sb.toString();
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

   public static String toTermString(long id, ArrayList<GraphNode> features) {
      StringBuilder sb = new StringBuilder();
      for (int i = 0; i < features.size(); i++) {
         if ((id & (1l << i)) > 0) {
            sb.append(features.get(i).toTermString()).append(" ");
         }
      }
      return sb.toString();
   }

   public String getName() {
      String clazz = getClass().getCanonicalName();
      clazz = StrTools.removeOptionalStart(clazz, Strategy.class.getPackage().getName() + ".");
      return clazz;
   }
   
   /**
    * @param containedfeatures
    * @return A space separated reformulation of the listed containedfeatures
    */
   public static String postReform(ArrayList<GraphNode> features) {
      StringBuilder sb = new StringBuilder();
      for (GraphNode f : features) {
         sb.append(f.postReform()).append(' ');
      }
      return sb.toString();
   }

   /**
    * @return a space separated representation of contained containedfeatures
    */
   public String toTermString() {
      return toTermString(containedfeatures);
   }

   /**
    * @param tab recursively increases the offset to show the hierarchy of
    * Features
    * @return a String that contains the printed hierarchy of contained Features
    */
   public String printRecursive(int tab) {
      StringBuilder sb = new StringBuilder();
      sb.append(io.github.repir.tools.Lib.PrintTools.sprintf("%" + tab + "s%s", "", toString()));
      for (GraphNode f : containedfeatures) {
         sb.append(f.printRecursive(tab + 2));
      }
      return sb.toString();
   }

   public FeatureValues getFeatureValues() {
      return featurevalues;
   }

   /**
    * @param weight
    */
   public void setweight(Double weight) {
      featurevalues.documentprior = featurevalues.queryweight = weight;
   }

   public void setchannel(String channel) {
      for (GraphNode f : containedfeatures)
         f.setchannel(channel);
   }

   public void settf(Long termfrequency) {
      featurevalues.corpusfrequency = termfrequency;
   }

   public void setdf(Long documentfrequency) {
      featurevalues.documentfrequency = documentfrequency;
   }

   public void setGenericD(String variable) {
      double value = Double.parseDouble(variable.substring(variable.indexOf('=') + 1));
      variable = variable.substring(0, variable.indexOf('='));
      Method method = ClassTools.getMethod(this.getClass(), "set" + variable, Double.class);
      ClassTools.invoke(method, this, value);
   }

   public void setGenericL(String variable) {
      long value = Long.parseLong(variable.substring(variable.indexOf('=') + 1));
      variable = variable.substring(0, variable.indexOf('='));
      Method method = ClassTools.getMethod(this.getClass(), "set" + variable, Long.class);
      ClassTools.invoke(method, this, value);
   }

   @Override
   public int compare(GraphNode o1, GraphNode o2) {
      return o1.equals(o2) ? 0 : 1;
   }
}
