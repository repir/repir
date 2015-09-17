package io.github.repir.Strategy.Operator;

import io.github.repir.Retriever.Retriever;
import io.github.repir.Retriever.Query;
import io.github.htools.lib.Log;
import java.util.Collection;
import java.util.HashSet;
import io.github.repir.Strategy.Collector.Collector;
import io.github.repir.Strategy.Strategy;

/**
 * The Analyze {@link Strategy} processes features in a single pass, that
 * processes features in the repository in some custom way, using a {@link Collector}
 * to aggregate results. The collector is responsible for storing the retrieved
 * results in an appropriate way.
 * <p/>
 * @author jeroen
 */
public abstract class Analyze extends Strategy implements Analyzer {
   
   public static Log log = new Log(Analyze.class);

   public Analyze(Retriever retriever) {
      super( retriever );
   }

   @Override
   public abstract void setCollector();
   
   public abstract void doMapTask();

   /**
    * By default, the Analyze does a single pass and does not return any
    * results but rather stored the results in the repository.
    */
   @Override
   public Query finishReduceTask() {
      query.setStrategyClassname(null);
      return query;
   }
   
   public Query postReform() {
      return query;
   }
}
