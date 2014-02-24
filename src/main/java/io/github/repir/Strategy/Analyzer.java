package io.github.repir.Strategy;

/**
 * The Analyzer fakes a retrieval process, to do a different type of
 * analysis that does not use a GraphRoot, using the IREF logistics to
 * distribute the task over the Hadoop cluster and collect and cache some 
 * kind of data.
 * <p/>
 * @author jeroen
 */
public interface Analyzer {
   
   public abstract void setCollector();
   
   public abstract void doMapTask();
}
