package io.github.repir.Strategy;

import io.github.repir.tools.Content.Datafile;
import io.github.repir.Retriever.Retriever;
import io.github.repir.Retriever.Query;
import io.github.repir.tools.Lib.ClassTools;
import io.github.repir.tools.Lib.Log;
import io.github.repir.Repository.Repository;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.HashSet;
import io.github.repir.Strategy.Collector.Collector;
import io.github.repir.Strategy.Collector.MasterCollector;

/**
 * The Strategy controls the retrieval process. A Strategy is created by {@link #buildGraph(Retriever.Query)
 * }
 * which uses a {@link Query} request that is parsed into an {@link GraphRoot}. For standard
 * retrieval, the default Strategy should work fine, but alternatively, a Strategy can
 * alter the GraphRoot (e.g. {@link RetrievalModelRM3}).
 * <p/>
 * @author jeroen
 */
public abstract class Strategy {
   
   public static Log log = new Log(Strategy.class);
   public Retriever retriever;
   public Datafile fileout;
   public Query query;
   public Repository repository;
   public int partition;
   public MasterCollector collectors;

   /**
    * Use {@link #create(Retriever.Retriever, Retriever.Query)} instead.
    * <p/>
    * @param retriever
    */
   public Strategy(Retriever retriever) {
      this.retriever = retriever;
      repository = retriever.getRepository();
      collectors = new MasterCollector();
      collectors.setRepository(repository);
   }

   /**
    * Override to set a different collector for the retrieval model
    */
   public abstract void setCollector();

   /**
    * RetrievalModels should be instantiated using this function on a Query request. The Query
    * request contains the specific classes for construction of a RerievalModel and parses the query
    * String into an GraphRoot.
    * <p/>
    * @param retriever
    * @param queryrequest
    * @return Strategy
    */
   public static Strategy create(Retriever retriever, Query queryrequest, Class assignableClass) {
      //log.info("create( %s )", queryrequest.strategyclass);
      Strategy strategy = null;
      try {
         Class clazz = io.github.repir.tools.Lib.ClassTools.toClass(queryrequest.getStrategyClass(), Strategy.class.getPackage().getName());
         Constructor cons = ClassTools.getAssignableConstructor(clazz, assignableClass, Retriever.class);
         strategy = (Strategy) cons.newInstance(retriever);
         strategy.setQuery(queryrequest);
      } catch (InstantiationException ex) {
         log.fatalexception(ex, "create( %s, %s )", retriever, queryrequest);
      } catch (IllegalAccessException ex) {
         log.fatalexception(ex, "create( %s, %s )", retriever, queryrequest);
      } catch (IllegalArgumentException ex) {
         log.fatalexception(ex, "create( %s, %s )", retriever, queryrequest);
      } catch (InvocationTargetException ex) {
         log.fatalexception(ex, "create( %s, %s )", retriever, queryrequest);
      }
      return strategy;
   }
   
   public static Strategy create(Retriever retriever, Query queryrequest) {
      return create(retriever, queryrequest, Strategy.class);
      
   }
   
   public final void setQuery(Query q) {
      this.query = q;
      repository.addConfiguration(q.getConfiguration());
   }

   /**
    * Used to setup the Strategy so that results can be collected and aggregated. 
    * This is typically used in the Reducer to create a Strategy for the
    * aggregation of results collected per segments.
    */
   public final void prepareAggregation() {
      prepareAggregationDetail();
      setCollector();
      collectors.prepareAggregation();
   }
   
   public void prepareRetrieval() {
      collectors.prepareRetrieval();
   }
   
   public abstract void prepareAggregationDetail();

   public final void estimateReducers() {
      prepareAggregationDetail();
      setCollector();
   }
   
   public Collection<String> reducerList() {
      estimateReducers();
      HashSet<String> reducers = new HashSet<String>();
      for (Collector c : collectors) {
         reducers.addAll(c.getReducerIDs());
      }
      return reducers;
   }
   
   public abstract void doMapTask();

   /**
    * After the collectors are shuffled and sorted to the reducers, they are
    * aggregated if the collectors are equals(). Between aggregation and sending
    * the results to the master process (using prepareWriteReduce(), writeReduce(),
    * and finishWriteReduce()), this hook is called to allow processing in the reducer.
    */
   public abstract Query finishReduceTask();
   
   public void prepareWriteReduce(Query q) {
      fileout = new Datafile(repository.getFS(), repository.getConfigurationString("topicrun.outfile") + "_" + q.getID() + "_" + q.getVariantID());
      log.info("outfile %s", fileout.getFullPath());
      fileout.openWrite();
   }
   
   public void writeReduce(Query q) {
      q.write(fileout.rwbuffer);
   }
   
   public void finishWriteReduce() {
      fileout.closeWrite();
   }

   /* hook to modify storedfeatures when they are cloned for the next cycle */
   public GraphNode cloneFeature(GraphNode f, GraphRoot newmodel, int cycle) {
      return f.clone(newmodel);
   }
}
