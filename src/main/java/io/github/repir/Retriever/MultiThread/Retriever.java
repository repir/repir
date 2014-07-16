package io.github.repir.Retriever.MultiThread;

import io.github.repir.Retriever.MapReduce.*;
import io.github.repir.Repository.Repository;
import io.github.repir.Retriever.Query;
import io.github.repir.tools.Lib.Log;
import io.github.repir.Strategy.Strategy;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import io.github.repir.Strategy.Operator.Analyzer;
import io.github.repir.Strategy.RetrievalModelAnalyze;

/**
 * An implementation of Retriever that retrieves queries using the MapReduce
 * framework. After each pass, queries that are complete (i.e. there is no
 * consecutive Strategy to run) are stored in the finalresults, and queries that
 * require an additional retrieval pass are automatically resubmitted to the MapReduce
 * framework. From the user's perspective, a Query is given, and the endresult of
 * the final run per Query is returned.
 * <p/>
 * @author jeroen
 */
public class Retriever extends io.github.repir.Retriever.MapReduce.Retriever implements JobCallback {

   public static Log log = new Log(Retriever.class);
   private long lastprogress;
   Job currentjob;
   JobThreadCallback callback;

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

   @Override
   public Job createJob(String path) throws IOException {
      return new Job(this, path);
   }

   public void retrieveThreadedQueries(ArrayList<Query> queries, JobThreadCallback callback) {
      this.callback = callback;
      models = new ArrayList<Strategy>();
      finalresults = new ArrayList<Query>();
      for (Query q : queries) {
         models.add(Strategy.create(this, q));
      }
      removeDoneQueries();
      runThreadedQueries();
   }

   private void runThreadedQueries() {
      try {
         currentjob = createJob(jobpath);
         jobpath = currentjob.path;
         addQueriesToJob(currentjob, models);
         currentjob.startThreadedJob(this);
      } catch (IOException ex) {
         callback.JobFailed( );
      }
   }

   @Override
   public void jobWasSuccesful(QueueIterator qi) {
      Collection<Query> results = qi.nextVariant();
      resultToModel(results, models);
      Iterator<Strategy> iter = models.iterator();
      while (iter.hasNext()) {
         Strategy rm = iter.next();
         if (rm instanceof Analyzer || rm instanceof RetrievalModelAnalyze) {
            iter.remove();
         }
      }
      removeDoneQueries();
      if (models.size() > 0) {
         repository.readConfiguration();
         runThreadedQueries();
      } else {
         callback.jobWasSuccesful( finalresults);
      }
   }

   @Override
   public void JobFailed() {
      callback.JobFailed();
   }
}
