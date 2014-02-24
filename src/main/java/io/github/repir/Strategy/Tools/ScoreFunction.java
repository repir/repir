package io.github.repir.Strategy.Tools;

import io.github.repir.tools.Content.BufferDelayedWriter;
import io.github.repir.tools.Content.BufferReaderWriter;
import io.github.repir.Retriever.Document;
import io.github.repir.Retriever.Query;
import io.github.repir.tools.Lib.Log;
import io.github.repir.Repository.ReportedUnstoredFeature;
import io.github.repir.Repository.Repository;
import io.github.repir.Strategy.GraphComponent.ANNOUNCEKEY;
import io.github.repir.Strategy.Tools.ScoreFunction.Scorable;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collection;
import io.github.repir.Strategy.GraphNode;
import io.github.repir.Strategy.GraphRoot;
import io.github.repir.Strategy.RetrievalModel;
import io.github.repir.Strategy.Strategy;

/**
 * An interface for functions that assign a score to rank retrieved documents
 * <p/>
 * @author jeroen
 */
public abstract class ScoreFunction<S extends Scorable> extends ReportedUnstoredFeature<Double> {

   public static Log log = new Log(ScoreFunction.class);
   public BufferDelayedWriter writer = new BufferDelayedWriter();
   public BufferReaderWriter reader = new BufferReaderWriter();
   public RetrievalModel retrievalmodel;
   public double score;
   public boolean report;
   public ArrayList<S> scorables;
   public double documentpriorweight;

   public ScoreFunction(Repository repository) {
      super( repository );
   }
   
   public final void prepareRetrieval( Strategy rm ) {
      log.fatal("Cannot call prepareRetrieval(rm) on a scorefunction, use prepareRetrieval(rm, Collection<GraphNode>) instead");
   }
   
   /**
    * During the prepareRetrieval phase, the collector calls the ScoreFunction
    * and passes a list of nodes to be scored. A scoreFunction should extend the
    * Scorable subclass that contains document independent data needed to score
    * the GraphNode.
    * @param root
    * @param nodes 
    */
   public final void prepareRetrieval( RetrievalModel rm, Collection<GraphNode> nodes ) {
      super.prepareRetrieval(rm);
      this.retrievalmodel = rm;
      scorables = new ArrayList<S>();
      report = repository.getConfigurationBoolean("scorefunction.report", false);
      for (GraphNode n : nodes)
         this.scorables.add( create(n) );
      prepareRetrieve();
   }
   
   public abstract void prepareRetrieve();
   
   public abstract S create( GraphNode feature );

   public abstract double score( Document doc );

   public static ScoreFunction create(GraphRoot root) {
      ScoreFunction scorefunction = null;
      Class clazz = io.github.repir.tools.Lib.ClassTools.toClass(root.query.getScorefunctionClass(), ScoreFunction.class.getPackage().getName());
      Constructor cons = io.github.repir.tools.Lib.ClassTools.getAssignableConstructor(clazz, ScoreFunction.class, Repository.class);
      scorefunction = (ScoreFunction) io.github.repir.tools.Lib.ClassTools.construct(cons, root.repository);
      return scorefunction;
   }

   @Override
   public String getCanonicalName() {
      return getClass().getCanonicalName();
   }

   @Override
   public String getLabel() {
      return getClass().getSimpleName();
   }

   @Override
   public void decode(Document d) {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   @Override
   public void encode(Document d) {
      Double score = valueReported( d );
      writer.write( score );
      
   }

   @Override
   public void report(Document doc) {
      doc.setReportedFeature(reportid, score);
   }

   @Override
   public Double valueReported(Document doc) {
      return (Double)doc.getReportedFeature(reportid);
   }
   
   /**
    * The Scorable class contains a FeatureScorable together with the
    * prepared values needed by the ScoreFunction. Any implementation of
    * ScoreFunction should extends this subclass and pass it as the
    * class' generic. The main class will set up an array of Scorable
    * items, for the {@link #score(IndexReader.Document) } and {@link #prepareRetrieval(Strategy.GraphRoot) }
    * methods to iterate over.
    */
   public abstract class Scorable {
      GraphNode feature;
      
      public Scorable( GraphNode feature ) {
         this.feature = feature;
      }
   }
}
