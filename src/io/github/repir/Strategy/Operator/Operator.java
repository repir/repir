package io.github.repir.Strategy.Operator;

import io.github.repir.Retriever.Document;
import io.github.repir.tools.Lib.ClassTools;
import io.github.repir.tools.Lib.Log;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import io.github.repir.Repository.Repository;
import io.github.repir.Repository.TermDocumentFeature;
import io.github.repir.Strategy.GraphComponent;
import io.github.repir.Strategy.GraphRoot;
import io.github.repir.Strategy.Strategy;
import io.github.repir.tools.Lib.StrTools;
import java.util.Collections;

/**
 * A Operator is a node in graph, with {@link GraphRoot} as its root. The containednodes will 
 * form a tree that can process each retrieved document resulting in a document
 * process. The leaf (and optionally intermediary) nodes set their values according to
 * the values stored as {@link StoredFeature}s for the document that is currently inspected (e.g. the
 * frequency of a term). Intermediary nodes can use the values of their containednodes 
 * to set their value for the current document, e.g. ProximityOperator. The values
 * of an {@link Operator} are kept in {@link OperatorValues}. Through the announce
 * phase, the GraphRoot learns which {@link Operator}s directly contribute to a {@link Document}'s
 * score, which are used by the {@link CollectorDocument}.
 */
public abstract class Operator extends GraphComponent implements Comparator<Operator>, Comparable<Operator>, PositionalOperator {

   public static Log log = new Log(Operator.class);
   public GraphRoot root;
   public boolean positional = false;
   
   // indicates whether a node will be scored using a ScoreFunction or not, which may affect the
   // needed stored features
   public boolean willbescored = false;
   protected double queryweight = 1;
   protected double documentprior = 1;
   protected ArrayList<Double> frequencylist;
   protected double frequency = 0;
   protected double secondaryfrequency = 0;
   protected long cf = -1;
   protected long df = -1;
   protected int pos[];
   public int span = 1;
   public int sequence = -1; // can be used to indicate it is the n-th node, not set by default

   /**
    * Features can only exists as part of an {@link GraphRoot}
    * <p/>
    * @param root
    */
   protected Operator(GraphRoot root) {
      super(root.retrievalmodel);
      this.root = root;
   }
   
   protected Operator(Repository repository) {
      super(repository);
   }
   
   /**
    * Constructs a feature with a list of contained containednodes.
    * <p/>
    * @param root
    * @param containednodes
    */
   protected Operator(GraphRoot root, ArrayList<Operator> containedfeatures) {
      this(root);
      for (Operator f : containedfeatures) {
         add(f);
      }
   }
   
   protected ArrayList<Operator> combineDuplicates( ArrayList<Operator> list ) {
      ArrayList<Operator> remove = new ArrayList<Operator>();
      for (Operator f : list) {
            for (Operator ff : list) {
               if (ff == f)
                  break;
               if (f.equals(ff)) {
                  f.setweight(ff.getQueryWeight() + f.getQueryWeight());
                  remove.add(ff);
                  break;
               }
            }
      }
      list.removeAll(remove);
      return list;
   }
   
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
              && (cf < 0 || df < 0);
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
   
   public double getQueryWeight() {
      return queryweight;
   }
   
   public double getFrequency() {
      return frequency;
   }
   
   public double getDocumentPrior() {
      return documentprior;
   }
   
   public boolean isStopword() {
      return false;
   }
   
   public long getCF() {
      return cf;
   }
   
   public long getDF() {
      return df;
   }
   
   public int[] getPos() {
      return pos;
   }
   
   public int getSpan() {
      return span;
   }
   
   public double getSecondaryFrequency() {
      return secondaryfrequency;
   }
   
   public ArrayList<Double> getFrequencyList() {
      return frequencylist;
   }
   
   public void setFrequency(double frequency) {
      this.frequency = frequency; 
   }
   
   public void setSecondaryFrequency(double frequency) {
      this.secondaryfrequency = frequency; 
   }
   
   public void clearFrequencyList() {
      frequencylist = new ArrayList<Double>();
   }
   
   public void setQueryWeight(double weight) {
      this.queryweight = weight; 
   }
   
   public void setDocumentPrior(double prior) {
      this.documentprior = prior; 
   }
   
   public ArrayList<Double> noFrequencyList() {
      return frequencylist = null;
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
   public void announce( ANNOUNCEKEY key, Operator node ) {
      parent.announce(key, node);
   }

   public void moveUp( ) {
      parent = parent.parent;
      parent.add(this);
   }

   /**
    * During the initialization cycle, all containednodes in the graph must initialize 
    * their values (i.e. set their constant values, such as corpus frequency) and
    * announce themselves (call announce with all ANNOUNCEKEY values that apply, 
    * e.g. STOPWORD, TERM). The SCORABLE announcement is made by default
    * if the feature implements the FeatureScorable interface.
    * <p/>
    * Implementing containednodes should recursively call contained containednodes as well.
    * If a feature requires a positional posting list to be retrieved, it should
    * set positional to true. Features also pass along to their nested features if they will be
    * scored using a ScoreFunction, which may require them to request additional statistics 
    * to be retrieved. E.g. FeaturePhrase and FeatureSynonym do not score their contained features
    * with ScoreFunction, but only look at their occurrence in the document.
    * <p/>
    * @param positional containednodes that require positional posting lists should
    * turn this to true
    */
   
   /*
    * Through this function FeatureNodes receive whether word positions
    * are needed for retrieval. By returning true, they can indicate that
    * they need word positions themselves. Nodes should obey by providing
    * word positions through their OperatorValues if requested.
    */
   public boolean positional( boolean positional ) {
      this.positional = positional;
      return positional;
   }
   
   @Override
   public final void setTermPositionsNeeded( boolean positional ) {
      super.setTermPositionsNeeded(positional( positional ));
   }
   
   @Override
   public void doExpand() {
      super.doExpand();
      if (expand()) {
         for (Operator g : containednodes)
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
   public final void setWillBeScored( boolean willbescored ) {
      super.setWillBeScored( willBeScored( willbescored ));
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
    * stored in the Operator's {@link OperatorValues}.
    * <p/>
    * @param doc
    * @return
    */
   public abstract void process(Document doc);

   public void addReport(Document doc, double docscore) {
      if (frequency > 0) {
         doc.addReport("[%s] %e %e\n", this.toTermString(), frequency, docscore);
      }
   }

   public abstract Operator clone(GraphRoot newmodel);

   /**
    * @return a string of the reformulated Operator, that may be an altered
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
   public Operator clone(GraphRoot newmodel, int cycle) {
      Operator newfeature = root.retrievalmodel.cloneFeature(this, newmodel, cycle);
      if (newfeature != null) {
         //log.info("%s %d", newfeature.toClass().getCanonicalName(), containednodes.size());
         for (Operator f : containednodes) {
            //log.info("clone() %s %s", this.toTermString(), containednodes);
            Operator nf = root.retrievalmodel.cloneFeature(f, newmodel, cycle);
            if (nf != null) {
               newfeature.add(nf);
            }
            //log.info("%s", containednodes);
         }
      }
      return newfeature;
   }

   /**
    * @param containednodes list of Features
    * @return a space separated representation of the list of containednodes
    */
   public static String toTermString(ArrayList<Operator> features) {
      StringBuilder sb = new StringBuilder();
      Iterator<Operator> fi = features.iterator();
      while (fi.hasNext()) {
         sb.append(fi.next().toTermString());
         if (fi.hasNext()) {
            sb.append(' ');
         }
      }
      return sb.toString();
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

   public static String toTermString(long id, ArrayList<Operator> features) {
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
    * @param containednodes
    * @return A space separated reformulation of the listed containednodes
    */
   public static String postReform(ArrayList<Operator> features) {
      StringBuilder sb = new StringBuilder();
      for (Operator f : features) {
         sb.append(f.postReform()).append(' ');
      }
      return sb.toString();
   }

   /**
    * @return a space separated representation of contained containednodes
    */
   public String toTermString() {
      return toTermString(containednodes);
   }

   /**
    * @param tab recursively increases the offset to show the hierarchy of
    * Features
    * @return a String that contains the printed hierarchy of contained Features
    */
   public String printRecursive(int tab) {
      StringBuilder sb = new StringBuilder();
      sb.append(io.github.repir.tools.Lib.PrintTools.sprintf("%" + tab + "s%s", "", toString()));
      for (Operator f : containednodes) {
         sb.append(f.printRecursive(tab + 2));
      }
      return sb.toString();
   }

   /**
    * @param weight
    */
   public void setweight(Double weight) {
      documentprior = queryweight = weight;
   }

   public void setchannel(String channel) {
      for (Operator f : containednodes)
         f.setchannel(channel);
   }

   public void setcf(Long termfrequency) {
      cf = termfrequency;
   }

   public void setdf(Long documentfrequency) {
      df = documentfrequency;
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

   protected void sortContainedFeatures() {
      Collections.sort(containednodes);
   }
   
   @Override
   public int compare(Operator o1, Operator o2) {
      return o1.equals(o2) ? 0 : 1;
   }
   
   @Override
   public int compareTo(Operator o) {
      int comp = postReform().compareTo(o.postReform());
      return (comp != 0)?comp:1;
   }
}
