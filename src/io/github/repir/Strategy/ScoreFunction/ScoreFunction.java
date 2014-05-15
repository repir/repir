package io.github.repir.Strategy.ScoreFunction;

import io.github.repir.tools.Content.BufferDelayedWriter;
import io.github.repir.tools.Content.BufferReaderWriter;
import io.github.repir.Retriever.Document;
import io.github.repir.tools.Lib.Log;
import io.github.repir.Repository.ReportedUnstoredFeature;
import io.github.repir.Repository.Repository;
import io.github.repir.Strategy.ScoreFunction.ScoreFunction.Scorable;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collection;
import io.github.repir.Strategy.Operator.Operator;
import io.github.repir.Strategy.GraphRoot;
import io.github.repir.Strategy.RetrievalModel;
import io.github.repir.Strategy.Strategy;

/**
 * An abstract class for functions that assign a score to rank retrieved
 * documents. Implementations should provide an extension to the
 * {@link Scorable} class, which is used to wrap a scorable {@link Opeator} in,
 * along with values that are {@link Document} independent. The {@link #createScorable(io.github.repir.Strategy.Operator.Operator)
 * }
 * method should wrap the supplied operator in a Scorable. During {@link #prepareRetrieve()} it should
 * set the {@link Operator} independent parameters. During a retrieval pass, documents
 * are eventually scored with the result obtained from {@link #score(io.github.repir.Retriever.Document) }
 * in which the {@link ScoreFunction} should combine the score over all {@link Scorable}s
 * that were assigned to it. Typically, the scores of {@link Scorable}s are summed.
 * <p/>
 * @author jeroen
 * @param <S>
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
      super(repository);
   }

   public final void prepareRetrieval(Strategy rm) {
      log.fatal("Cannot call prepareRetrieval(rm) on a scorefunction, use prepareRetrieval(rm, Collection<GraphNode>) instead");
   }

   /**
    * During the prepareRetrieval phase, the collector calls the ScoreFunction
    * and passes a list of nodes to be scored. A scoreFunction should extend the
    * Scorable subclass that sets document independent (constant) values needed
    * to score the Operator. During the {@link #prepareRetrieve()} phase,
    *
    * @param root
    * @param nodes
    */
   public final void prepareRetrieval(RetrievalModel rm, Collection<Operator> nodes) {
      super.prepareRetrieval(rm);
      this.retrievalmodel = rm;
      report = repository.configuredBoolean("scorefunction.report", false);
      scorables = new ArrayList<S>();
      for (Operator n : nodes) {
         this.scorables.add(createScorable(n));
      }
      prepareRetrieve();
   }

   public abstract void prepareRetrieve();

   public abstract S createScorable(Operator feature);

   public abstract double score(Document doc);

   public static ScoreFunction create(GraphRoot root) {
      ScoreFunction scorefunction = null;
      Class clazz = io.github.repir.tools.Lib.ClassTools.toClass(root.retrievalmodel.getScorefunctionClass(), ScoreFunction.class.getPackage().getName());
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
   public void decode(Document d, int reportid) {
      reader.setBuffer((byte[]) d.getReportedFeature(reportid));
      d.setReportedFeature(reportid, reader.readDouble());
   }

   @Override
   public void encode(Document d, int reportid) {
      Double score = valueReported(d, reportid);
      writer.write(score);
   }

   @Override
   public void report(Document doc, int reportid) {
      doc.setReportedFeature(reportid, score);
   }

   @Override
   public Double valueReported(Document doc, int reportid) {
      return (Double) doc.getReportedFeature(reportid);
   }

   /**
    * The Scorable class contains a FeatureScorable together with the prepared
    * values needed by the ScoreFunction. Any implementation of ScoreFunction
    * should extends this subclass and pass it as the class' generic. The main
    * class will set up an array of Scorable items, for the {@link #score(IndexReader.Document)
    * } and {@link #prepareRetrieval(Strategy.GraphRoot) }
    * methods to iterate over.
    */
   public abstract class Scorable {

      Operator feature;

      public Scorable(Operator feature) {
         this.feature = feature;
      }
   }
}
