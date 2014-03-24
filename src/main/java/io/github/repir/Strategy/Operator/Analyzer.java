package io.github.repir.Strategy.Operator;

/**
 * The Analyze {@link Strategy} processes features in a single pass, that
 * processes features in the repository in some custom way, using a {@link Collector}
 * to aggregate results. The collector is responsible for storing the retrieved
 * results in an appropriate way.
 * <p/>
 * Classes that extend Analyzer return no results to the {@link Retriever}, 
 * therefore the Retriever will stop after one iteration pass.
 * @author jeroen
 */
public interface Analyzer {
   
   public abstract void setCollector();
   
   public abstract void doMapTask();
}
