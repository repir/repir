package io.github.repir.Retriever.MapReduce;

import io.github.repir.Repository.Repository;
import io.github.repir.Retriever.Query;
import io.github.repir.tools.Lib.Log;
import io.github.repir.Strategy.Strategy;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import io.github.repir.Strategy.Operator.Analyzer;
import io.github.repir.Strategy.RetrievalModelAnalyze;

/**
 * An implementation of Retriever that retrieves queries using the MapReduce
 * framework. After each pass, queries that are complete (i.e. there is no
 * consecutive Strategy to run) are stored in the finalresults, and queries that
 * require an additional retrieval pass are automatically resubmitted to the
 * MapReduce framework. From the user's perspective, a Query is given, and the
 * endresult of the final run per Query is returned.
 * <p/>
 * @author jeroen
 */
public class Retriever extends io.github.repir.Retriever.Retriever {

   public static Log log = new Log(Retriever.class);
   protected Mapper.Context mappercontext;
   protected Reducer.Context reducercontext;
   private long lastprogress;
   protected RetrieverJob currentjob;
   protected ArrayList<Strategy> models;
   protected ArrayList<Query> finalresults;
   protected String jobpath;

   public Retriever(Repository repository) {
      super(repository);
   }

   /**
    * The Mapper context is used to report progress, to prevent processes form
    * being killed while still working.
    * <p/>
    * @param repository
    * @param mappercontext
    */
   public Retriever(Repository repository, org.apache.hadoop.mapreduce.Mapper.Context mappercontext) {
      super(repository);
      this.mappercontext = mappercontext;
   }

   /**
    * The Reducer context is used to report progress, to prevent processes form
    * being killed while still aggregating.
    * <p/>
    * @param repository
    * @param reducercontext
    */
   public Retriever(Repository repository, org.apache.hadoop.mapreduce.Reducer.Context reducercontext) {
      super(repository);
      this.reducercontext = reducercontext;
   }

   public RetrieverJob createJob(String path) throws IOException {
      return new RetrieverJob(this, path);
   }

   /**
    * The List of queries are retrieved using the MapReduce framework.
    * <p/>
    * @param queries
    * @return retrieved list of queries
    */
   @Override
   public ArrayList<Query> retrieveQueries(ArrayList<Query> queries) {
      models = new ArrayList<Strategy>();
      finalresults = new ArrayList<Query>();
      String path = null;
      try {
         for (Query q : queries) {
            models.add(Strategy.create(this, q));
         }
         removeDoneQueries();
         while (models.size() > 0) {
            RetrieverJob job = createJob(path);
            path = job.path;
            addQueriesToJob(job, models);
            repository.featuresWriteCache();
            Collection<Query> results = job.getResults();
            models = resultToModel(results, models);
            removeDoneQueries();
            if (models.size() > 0) {
               repository.readConfiguration();
            }
         }
      } catch (IOException ex) {
         log.exception(ex, "retrieveQueries( %s )", queries);
      }
      return finalresults;
   }

   public ArrayList<Query> recoverQueries(ArrayList<Query> queries, String path) {
      ArrayList<Strategy> models = new ArrayList<Strategy>();
      ArrayList<Query> finalresults = new ArrayList<Query>();
      try {
         for (Query q : queries) {
            models.add(Strategy.create(this, q));
         }
         if (models.size() > 0) {
            RetrieverJob job = createJob(path);
            addQueriesToJob(job, models);
            Collection<Query> results = job.recoverResults();
            models = resultToModel(results, models);
         }
         Iterator<Strategy> iter = models.iterator();
         while (iter.hasNext()) {
            Strategy rm = iter.next();
            if (rm.query.done()) {
               finalresults.add(rm.query);
               iter.remove();
            }
         }
         if (models.size() > 0) {
            log.fatal("not all results were collected");
         }
      } catch (IOException ex) {
         log.exception(ex, "retrieveQueries( %s )", queries);
      }
      return finalresults;
   }

   protected void addQueriesToJob(RetrieverJob job, ArrayList<Strategy> models) {
      ArrayList<Query> queries = new ArrayList<Query>();
      for (Strategy rm : models) {
         queries.add(rm.query);
      }
      job.setQueries(queries);
   }

   protected ArrayList<Strategy> resultToModel(Collection<Query> results, ArrayList<Strategy> models) {
      ArrayList<Strategy> newmodels = new ArrayList<Strategy>();
      NEXT:
      for (Strategy rm : models) {
         for (Query q : results) {
            if (rm.query.id == q.id) {
               rm.query = q;
               if (q.done()) {
                  rm.query = q;
                  newmodels.add(rm);
               } else {
                  newmodels.add(Strategy.create(this, q));
               }
               continue NEXT;
            }
         }
         if (!(rm instanceof Analyzer) && !(rm instanceof RetrievalModelAnalyze)) {
            newmodels.add(rm);
         }
      }
      return newmodels;
   }

   /**
    * For the retrieval of large results sets that may not fit into memory, this
    * retrieves a list of queries, and returns an {@link QueueIterator} that
    * allows to read the returned results as a stream. Each value of the stream
    * returns a QueryIterator
    * <p/>
    * @param queries
    * @return
    */
   public QueueIterator retrieveQueueIterator(ArrayList<Query> queries) {
      QueueIterator fqi = null;
      String path = null;
      try {
         ArrayList<Query> finalresults = new ArrayList<Query>();
         ArrayList<Query> results;
         if (queries.size() > 0) {
            repository.readConfiguration(); // have to reread in case values were added to the repository
            RetrieverJob job = createJob(path);
            path = job.path;
            job.setQueries(queries);
            queries = new ArrayList<Query>();
            fqi = job.getQueueIterator();
         }
      } catch (IOException ex) {
         log.exception(ex, "retrieveQueueIterator( %s )", queries);
      }
      return fqi;
   }

   public void doJobDontWait(ArrayList<Query> queries) {
      doJob(queries, false);
   }

   public void doJobDontWait(final Query q) {
      doJobDontWait(new ArrayList<Query>() {{ add(q); }} );
   }

   public void doJob(ArrayList<Query> queries) {
      doJob(queries, true);
   }

   public void doJob(final Query q) {
      doJob(new ArrayList<Query>() {{ add(q); }} );
   }

   private void doJob(ArrayList<Query> queries, boolean wait) {
      try {
         if (queries.size() > 0) {
            repository.readConfiguration(); // have to reread in case values were added to the repository
            RetrieverJob job = createJob("");
            job.setQueries(queries);
            job.doJob(wait);
         } else {
            log.info("no queries");
         }
      } catch (IOException ex) {
         log.exception(ex, "doJob( %s )", queries);
      }
   }

   /**
    * For the retrieval of large results sets that may not fit into memory, this
    * retrieves the queries in the queue, and returns an {@link QueueIterator}
    * that allows to read the returned results as a stream. Each value of the
    * stream returns a QueryIterator
    * <p/>
    * @return An Iterator that allows to read the results as a stream.
    */
   public QueueIterator retrieveQueueIterator() {
      return this.retrieveQueueIterator(getQueue());
   }

   @Override
   public void mapperProgress() {
      if (mappercontext != null && System.currentTimeMillis() - lastprogress > 300000) {
         mappercontext.progress();
         lastprogress = System.currentTimeMillis();
      }
   }

   @Override
   public void reducerProgress() {
      if (reducercontext != null && System.currentTimeMillis() - lastprogress > 300000) {
         reducercontext.progress();
         lastprogress = System.currentTimeMillis();
      }
   }

   /**
    * Retrieves a single query described in the Query object using the Mapreduce
    * framework.
    * <p/>
    * @param q the string that represents the user's need
    * @return the passed Query object is expanded with the retrieved documents
    */
   @Override
   public Query retrieveQuery(Query q) { // implements physical retrieval strategy
      ArrayList<Query> queries = new ArrayList<Query>();
      queries.add(q);
      ArrayList<Query> results = retrieveQueries(queries);
      return results.get(0);
   }

   protected void removeDoneQueries() {
      Iterator<Strategy> iter = models.iterator();
      while (iter.hasNext()) {
         Strategy rm = iter.next();
         if (rm.query.done()) {
            finalresults.add(rm.query);
            iter.remove();
         }
      }
   }
}
