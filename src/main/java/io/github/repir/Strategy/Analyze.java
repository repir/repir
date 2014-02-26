package io.github.repir.Strategy;

import io.github.repir.Retriever.Retriever;
import io.github.repir.Retriever.Query;
import io.github.repir.tools.Lib.Log;
import java.util.Collection;
import java.util.HashSet;
import io.github.repir.Strategy.Collector.Collector;

/**
 * The Analyze fakes a retrieval process, to do a different type of
 * analysis that does not use a GraphRoot, using the RepIR logistics to
 * distribute the task over the Hadoop cluster and collect and cache some 
 * kind of data.
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
      query.setStrategyClass(null);
      return query;
   }
   
   public Query postReform() {
      return query;
   }
}
