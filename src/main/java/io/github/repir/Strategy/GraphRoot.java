package io.github.repir.Strategy;

import io.github.repir.Strategy.Tools.StopWords;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RecognitionException;
import io.github.repir.Strategy.Collector.MasterCollector;
import io.github.repir.QueryParser.QueryLexer;
import io.github.repir.QueryParser.QueryParser;
import io.github.repir.Repository.StoredFeature;
import io.github.repir.Repository.TermDocumentFeature;
import io.github.repir.Retriever.Query;
import io.github.repir.Retriever.Retriever;
import io.github.repir.tools.Lib.ClassTools;
import io.github.repir.tools.Lib.Log;
import io.github.repir.tools.Lib.PrintTools;
import io.github.repir.tools.Lib.StrTools;

/**
 * An GraphRoot contains a network of feature nodes, that collect and process data for a retrieved
 * document. Construction of an GraphRoot is commonly done by parsing a query request using {@link #buildGraph(Retriever.Query)
 * }. The GraphRoot will contain a directed graph, with feature extraction nodes in the roots, which
 * will extract containedfeatures from the document to be processed, processing feature nodes, which
 * will convert the extracted containedfeatures into whatever is required. The Strategy can
 * include {@link MasterCollector} objects that collect the results to be returned. For a standard
 * query that can be a list of ranked documents, but for multi-pass retrieval strategies it can also
 * be used to gather statistics.
 * <p/>
 * <
 * p/> @author jeroen
 */
public class GraphRoot extends GraphComponent {

   public static Log log = new Log(GraphRoot.class);
   private HashMap<ANNOUNCEKEY, ArrayList<GraphNode>> nodelists = new HashMap<ANNOUNCEKEY, ArrayList<GraphNode>>();
   //public static englishStemmer stemmer = englishStemmer.get();
   public Retriever retriever;
   public Query query;
   //public Query queryrequest;
   public double documentpriorfrequency;
   public boolean removenonexist;
   public Class phraseclass = FeatureProximity.class;

   public GraphRoot(RetrievalModel rm) {
      super(rm);
      this.query = rm.query;
      this.retriever = rm.retriever;
      this.removenonexist = repository.getConfigurationBoolean("retriever.removenonexist", true);
   }

   /**
    * Builds an GraphRoot from a {@link Retriever.Query} request. The query string is tokenized
    * using {@link Retriever.Retriever#tokenizeString(Retriever.Query)
    * }
    * and then parsed using {@link QueryParser}, which converts the query into a network of
    * containedfeatures that extract and process documents into the data is collected. the query
    * request is parsed using {@link QueryParser}.
    * <p/>
    * @param queryrequest
    */
   public final void buildGraph() {
      build();
   }
   
   private final void build() {
      try {
         //log.info("query %s", query.stemmedquery);
         ANTLRInputStream input = new ANTLRInputStream(query.query);
         QueryLexer lexer = new QueryLexer(input);
         CommonTokenStream tokens = new CommonTokenStream(lexer);
         QueryParser parser = new QueryParser(tokens);
         parser.root = this;
         parser.prog();
      } catch (RecognitionException ex) {
         log.exception(ex, "build() queryrequest %s", query.query);
      }
   }

   /**
    * This method should be called prior to retrieval. It assembles a list of Features that are to
    * extracted from the retrieved documents, which are necessary for the retrieval process. All
    * containedfeatures in the model are then initialized using the statistics for the extracted
    * containedfeatures.
    */
   public void prepareRetrieval() {
      this.doExpand();
      this.doAnnounceContainedFeatures();
      this.cleanseModel();
      this.doConfigureContainedFeatures();
      this.passWillBeScored(true);
      this.termPositionsNeeded(false);
      this.doReadStatistics();
      if ( this.needsPrePass() ) {
         doSetupCollector();
      } else {
         
      }
   }

   /**
    * Called before creating a PostingsIterator, to set dependencies between
    * terms used to skip documents that are not scorable.
    */
   public void setTDFDependencies() {
      for ( StoredFeature f : retrievalmodel.getUsedFeatures() )
         if (f instanceof TermDocumentFeature)
            ((TermDocumentFeature)f).resetDependencies();
      for (GraphNode g : containedfeatures)
         if (!this.needsPrePass() || g.needsCollect())
            g.setTDFDependencies();
   }
   
   public Term getTerm(String termstring) {
      return new Term(this, termstring, termstring);
   }

   /**
    * @param term
    * @return true if GraphRoot is configured to remove stopwords and term occurs in the isstopword
    * list
    */
   public boolean isStemmedStopWord(String term) {
      //log.info("isstopword %s %b", term, queryrequest.removeStopwords && StopWords.isStemmedStopWord(term));
      return query.removeStopwords && StopWords.get(repository).isStemmedStopWord(term);
   }

   /**
    * removes all FeatureExtractors with non-existing terms, for which 
    * {@link #isStemmedStopWord(java.lang.String) } returns true, and empty Features.
    */
   public void cleanseModel() {
      ArrayList<GraphNode> list;
      if (query.removeStopwords) {
         // remove stopwords
         list = this.getAnnounce(ANNOUNCEKEY.STOPWORD);
         if (list != null) {
            for (int f = list.size() - 1; f >= 0; f--) {
               this.remove(list.get(f));
            }
         }
      }

      // remove words that are not in the vocabulary
      list = this.getAnnounce(ANNOUNCEKEY.NONEXIST);
      if (list != null) {
         for (int f = list.size() - 1; f >= 0; f--) {
            this.remove(list.get(f));
         }
      }
   }

   /**
    * @return true if any of the nodes needs to do a pre-pass to collect feature data
    */
   public boolean needsPrePass() {
      ArrayList<GraphNode> list = nodelists.get(ANNOUNCEKEY.NEEDSCOLLECT);
      if (list != null && list.size() > 0) {
         return true;
      }
      list = nodelists.get(ANNOUNCEKEY.NEEDSCACHECOLLECT);
      if (list != null && list.size() > 0) {
         return true;
      }
      return false;
   }

   /**
    * @return the reformulated query after retrieval using collected feature data
    */
   public String postReform() {
      StringBuilder sb = new StringBuilder();
      for (GraphNode f : containedfeatures) {
         sb.append(f.postReform()).append(" ");
      }
      return sb.toString();
   }

   /**
    * @param remove feature to be recursively removed from the GraphRoot
    */
   @Override
   public void remove(GraphNode remove) {
      super.remove(remove);
      for (Map.Entry<ANNOUNCEKEY, ArrayList<GraphNode>> entry : nodelists.entrySet()) {
         Iterator<GraphNode> iter = entry.getValue().iterator();
         while (iter.hasNext()) {
            if (iter.next() == remove) {
               iter.remove();
               break;
            }
         }
      }
   }

   /**
    * Construct a custom GraphNode class. This enables query formulation using "featureclass:(
    * contained containedfeatures )". This will go horribly wrong if this class does not exist, or
    * if it cannot be constructed with an GraphRoot and ArrayList<Feature> as parameters.
    * <p/>
    * @param featureclassname case-sensitive classname of the GraphNode
    * @param root the GraphRoot to addQueue the GraphNode to.
    * @param terms a list of contained containedfeatures
    * @return the new GraphNode
    */
   public GraphNode construct(String featureclassname, ArrayList<GraphNode> terms) {
      Class featureclass = ClassTools.toClass(featureclassname, Strategy.class.getPackage().getName());
      Constructor cons = ClassTools.getAssignableConstructor(featureclass, GraphNode.class, GraphRoot.class, ArrayList.class);
      GraphNode f = (GraphNode) ClassTools.construct(cons, this, terms);
      return f;
   }

   /**
    * Inverse of construct, to formulate a String that embeds a querystring in a custom GraphNode
    * parent, so that when parsed, the elements in the querystring become elements of the GraphNode
    * parent.
    * <p/>
    * @param parent parent GRaphNode class
    * @param query query string to pass as elements
    * @return new query string
    */
   public static String reformulate(Class parent, String query) {
      String classname = parent.getCanonicalName();
      classname = StrTools.removeOptionalStart(classname, Strategy.class.getPackage().getName() + ".");
      return classname + ":(" + query + ")";
   }

   public static String reformulate(GraphNode r) {
      String rf = reformulateUnweighted(r);
      if (r.featurevalues.queryweight != 1 && r.featurevalues.queryweight != 0) {
         rf = PrintTools.sprintf("%s#%g", rf, r.featurevalues.queryweight);
      }
      return rf;
   }

   public static String reformulateWeighted(GraphNode r) {
      StringBuilder sb = new StringBuilder();
      sb.append(r.getName()).append(":(");
      for (GraphNode n : r.containedfeatures)
         sb.append(n.postReform()).append(" ");
      if (r.featurevalues.queryweight != 1 && r.featurevalues.queryweight != 0) {
         sb.append(PrintTools.sprintf("#%g", r.featurevalues.queryweight));
      }
      return sb.toString();
   }

   public static String reformulateUnweighted(GraphNode r) {
      StringBuilder sb = new StringBuilder();
      sb.append(r.getName()).append(":(");
      for (GraphNode n : r.containedfeatures)
         sb.append(n.postReformUnweighted()).append(" ");
      sb.append(")").toString();
      return sb.toString();
   }

   /**
    * For debug purposes, recursively prints the GraphRoot
    */
   public void print() {
      for (GraphNode f : containedfeatures) {
         log.printf("%s", f.printRecursive(2));
      }
   }

   public ArrayList<GraphNode> getAnnounce(ANNOUNCEKEY key) {
      ArrayList<GraphNode> list = nodelists.get(key);
      if (list == null) {
         list = new ArrayList<GraphNode>();
         nodelists.put(key, list);
      }
      return list;
   }
   
   public void announce(ANNOUNCEKEY key, GraphNode node) {
      ArrayList<GraphNode> list = getAnnounce(key);
      list.add(node);
   }
   

}
