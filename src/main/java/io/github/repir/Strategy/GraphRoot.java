package io.github.repir.Strategy;

import io.github.repir.Strategy.Operator.Term;
import io.github.repir.Strategy.Operator.ProximityOperator;
import io.github.repir.Strategy.Operator.Operator;
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
 * An GraphRoot is responsible for constructing a graph of {@link Operator}s, that
 * will process retrieval according to the {@link Query}. 
 * <p/>
 * A graph is used by default for {@link RetrievalModel}s, i.e. {@link Strategy}s that
 * return a ranked list of {@link Document}s. The graph is constructed by parsing 
 * the specified {@link Query}, by {@link #buildGraph(Retriever.Query) using the
 * {@link QueryParser} to construct a bi-directional graph. The leaf nodes are 
 * usually {@link Term} {@link Operator}s, which are seeded with postings lists of
 * term features ({@link TermInverted}), and that operator nodes nearest the root are the nodes
 * that contribute directly to the score assigned to the Document being processed.
 * <p/>
 * After the graph has been built, the GraphRoot will prepare retrieval using a 
 * number of phases: (1) Expansion phase asking nodes to modify the graph if they 
 * need to expand or be replaced, (2) Announce phase requesting all nodes to 
 * announce their potential use to their parent node until the announcements reach
 * the root, (3) Cleanse phase in which stop words and operators for non existing 
 * store features are removed, (4) ConfigureContainedFeatures in which complex features
 * configure their settings, e.g. weight or span, (5) propagate setTermPositionsNeeded
 * across the graph, which is set to true if any feature needs term positions to 
 * ensure these are passed during processing (6) ReadStatistics in which features 
 * read their required statistics, e.g. collection/document frequency, (7)
 * needsPass() in which any operator can signal that they need a prepass to pre collect
 * additional information before being able to operate during the final pass, in which
 * case during (8) SetupCollector they install a {@link Collector} to obtain these results.
 * <p/> 
 * @author jeroen
 */
public class GraphRoot extends GraphComponent {

   public static Log log = new Log(GraphRoot.class);
   private HashMap<ANNOUNCEKEY, ArrayList<Operator>> nodelists = new HashMap<ANNOUNCEKEY, ArrayList<Operator>>();
   //public static englishStemmer stemmer = englishStemmer.get();
   public Retriever retriever;
   public Query query;
   //public Query queryrequest;
   public double documentpriorfrequency;
   public boolean removenonexist;
   public Class phraseclass = ProximityOperator.class;

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
    * and then parsed using {@link QueryParser}, which converts the query into a Graph of
    * containednodes that extract and process documents into the data is collected. the query
    * request is parsed using {@link QueryParser}.
    * <p/>
    * @param queryrequest
    */
   public final void buildGraph() {
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
    * containednodes in the model are then initialized using the statistics for the extracted
    * containednodes.
    */
   public void prepareRetrieval() {
      this.doExpand();
      this.doAnnounceContainedFeatures();
      this.cleanseModel();
      this.doConfigureContainedFeatures();
      this.setWillBeScored(true);
      this.setTermPositionsNeeded(false);
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
      for (Operator g : containednodes)
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
      ArrayList<Operator> list;
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
      ArrayList<Operator> list = nodelists.get(ANNOUNCEKEY.NEEDSCOLLECT);
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
      for (Operator f : containednodes) {
         sb.append(f.postReform()).append(" ");
      }
      return sb.toString();
   }

   /**
    * @param remove feature to be recursively removed from the GraphRoot
    */
   @Override
   public void remove(Operator remove) {
      super.remove(remove);
      for (Map.Entry<ANNOUNCEKEY, ArrayList<Operator>> entry : nodelists.entrySet()) {
         Iterator<Operator> iter = entry.getValue().iterator();
         while (iter.hasNext()) {
            if (iter.next() == remove) {
               iter.remove();
               break;
            }
         }
      }
   }

   /**
    * Construct a custom Operator class. This enables query formulation using "featureclass:(
    * contained containednodes )". This will go horribly wrong if this class does not exist, or
    * if it cannot be constructed with an GraphRoot and ArrayList<Feature> as parameters.
    * <p/>
    * @param featureclassname case-sensitive classname of the Operator
    * @param root the GraphRoot to addQueue the Operator to.
    * @param terms a list of contained containednodes
    * @return the new Operator
    */
   public Operator construct(String featureclassname, ArrayList<Operator> terms) {
      Class featureclass = ClassTools.toClass(featureclassname, Operator.class.getPackage().getName());
      Constructor cons = ClassTools.getAssignableConstructor(featureclass, Operator.class, GraphRoot.class, ArrayList.class);
      Operator f = (Operator) ClassTools.construct(cons, this, terms);
      return f;
   }

   /**
    * Inverse of construct, to formulate a String that embeds a querystring in a custom Operator
    * parent, so that when parsed, the elements in the querystring become elements of the Operator
    * parent.
    * <p/>
    * @param parent parent GRaphNode class
    * @param query query string to pass as elements
    * @return new query string
    */
   public static String reformulate(Class parent, String query) {
      String classname = parent.getCanonicalName();
      classname = StrTools.removeOptionalStart(classname, Operator.class.getPackage().getName() + ".");
      return classname + ":(" + query + ")";
   }

   public static String reformulate(Operator r) {
      String rf = reformulateUnweighted(r);
      if (r.featurevalues.queryweight != 1 && r.featurevalues.queryweight != 0) {
         rf = PrintTools.sprintf("%s#%g", rf, r.featurevalues.queryweight);
      }
      return rf;
   }

   public static String reformulateWeighted(Operator r) {
      StringBuilder sb = new StringBuilder();
      sb.append(r.getName()).append(":(");
      for (Operator n : r.containednodes)
         sb.append(n.postReform()).append(" ");
      if (r.featurevalues.queryweight != 1 && r.featurevalues.queryweight != 0) {
         sb.append(PrintTools.sprintf("#%g", r.featurevalues.queryweight));
      }
      return sb.toString();
   }

   public static String reformulateUnweighted(Operator r) {
      StringBuilder sb = new StringBuilder();
      sb.append(r.getName()).append(":(");
      for (Operator n : r.containednodes)
         sb.append(n.postReformUnweighted()).append(" ");
      sb.append(")").toString();
      return sb.toString();
   }

   /**
    * For debug purposes, recursively prints the GraphRoot
    */
   public void print() {
      for (Operator f : containednodes) {
         log.printf("%s", f.printRecursive(2));
      }
   }

   public ArrayList<Operator> getAnnounce(ANNOUNCEKEY key) {
      ArrayList<Operator> list = nodelists.get(key);
      if (list == null) {
         list = new ArrayList<Operator>();
         nodelists.put(key, list);
      }
      return list;
   }
   
   public void announce(ANNOUNCEKEY key, Operator node) {
      ArrayList<Operator> list = getAnnounce(key);
      list.add(node);
   }
   

}
