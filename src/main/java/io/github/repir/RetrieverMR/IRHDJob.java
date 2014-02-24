package io.github.repir.RetrieverMR;

import io.github.repir.Retriever.Document;
import io.github.repir.Retriever.Retriever;
import io.github.repir.Retriever.Query;
import io.github.repir.tools.Lib.Log;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.output.NullOutputFormat;
import io.github.repir.Retriever.Query.Variant;
import io.github.repir.Strategy.RetrievalModel;
import io.github.repir.Strategy.Strategy;

public class IRHDJob extends Job {

   public static Log log = new Log(IRHDJob.class);
   public String path;
   public Retriever retriever;
   protected Collection<Query> queue;
   protected QueueIterator qi;

   public IRHDJob(Retriever retriever) throws IOException {
      super(retriever.repository.getConfiguration());
      this.retriever = retriever;
      setJobName("Retriever " + retriever.repository.getConfigurationString("repir.conf"));
      path = conf.get("retriever.tempdir", "") + UUID.randomUUID().toString();
      conf.setBoolean("mapred.reduce.tasks.speculative.execution", false);
      setJob();
   }
   
   public IRHDJob(Retriever retriever, String path) throws IOException {
      this(retriever);
      if (path != null) {
         this.path = path;
      }
   }
   
   public void setQueries( Collection<Query> queries ) {
      this.queue = queries;  
   }

   public void setJob() {
      setJarByClass(RetrieverMRMap.class);
      setMapOutputKeyClass(CollectorKey.class);
      setMapOutputValueClass(CollectorValue.class);
      setOutputKeyClass(NullWritable.class);
      setOutputValueClass(NullWritable.class);
      setMapperClass(RetrieverMRMap.class);
      setReducerClass(RetrieverMRReduce.class);
      setInputFormatClass(RetrieverMRInputFormat.class);
      setOutputFormatClass(NullOutputFormat.class);
      setGroupingComparatorClass(CollectorKey.FirstGroupingComparator.class);
      setSortComparatorClass(CollectorKey.FirstGroupingComparator.class);
      setPartitionerClass(CollectorKey.partitioner.class);
   }

   public void setupInputFormat() {
      RetrieverMRInputFormat.setIndex(retriever.repository);
      RetrieverMRInputFormat.clear();
   }
   
   public void addQuery(Query q) {
      RetrieverMRInputFormat.add(retriever.repository, q);
   }
   
   public void addSingleQuery(Query q) {
      RetrieverMRInputFormat.addSingle(retriever.repository, q);
   }
   
   public String[] getReducers(Collection<String> reducers) {
      return reducers.toArray(new String[ reducers.size() ]);
   }
   
   public void startThreadedJob(IRHDJobCallback callback) {
      IRHDJobManager.get().startJob(this, callback);
   }
   
   public void setReducers(Collection<String> reducers) {
      String reducerlist[] = getReducers(reducers);
      conf.setStrings("retriever.reducers", reducerlist);
      //log.info("reducers %s", reducers);
      setNumReduceTasks( reducerlist.length );
   }
   
   public void startJob() {
      try {
         conf.set("topicrun.outfile", path);
         setupInputFormat();
         //int partition = 0;
         retriever.repository.unloadStoredDynamicFeatures();
         HashSet<String> reducers = new HashSet<String>();
         for (Query q : queue) {
            //log.info("query %s", q.query);
            Strategy rm = retriever.constructStrategy(q);
            reducers.addAll(rm.reducerList());
            //q.partition = partition;
            if (rm instanceof RetrievalModel)
               addQuery(q);
            else
               addSingleQuery(q);
         }
         setReducers( reducers );
         this.submit();
      } catch (IOException ex) {
         log.exception(ex, "startJob()");
      } catch (InterruptedException ex) {
         log.exception(ex, "startJob()");
      } catch (ClassNotFoundException ex) {
         log.exception(ex, "startJob()");
      }
   }
   
   public QueueIterator getQueueIterator() {
      qi = null;
      try {
         startJob();
         this.waitForCompletion(true);
         qi = getFinalQueueIterator(queue);
      } catch (IOException ex) {
         Logger.getLogger(IRHDJob.class.getName()).log(Level.SEVERE, null, ex);
      } catch (InterruptedException ex) {
         Logger.getLogger(IRHDJob.class.getName()).log(Level.SEVERE, null, ex);
      } catch (ClassNotFoundException ex) {
         Logger.getLogger(IRHDJob.class.getName()).log(Level.SEVERE, null, ex);
      }
      return qi;
   }
   
   public void doJob(boolean wait) {
      try {
         startJob();
         if (wait)
             this.waitForCompletion(true);
      } catch (IOException ex) {
         Logger.getLogger(IRHDJob.class.getName()).log(Level.SEVERE, null, ex);
      } catch (InterruptedException ex) {
         Logger.getLogger(IRHDJob.class.getName()).log(Level.SEVERE, null, ex);
      } catch (ClassNotFoundException ex) {
         Logger.getLogger(IRHDJob.class.getName()).log(Level.SEVERE, null, ex);
      }
   }
   
   public void complete(IRHDJobCallback callback) {
      try {
         if (this.isSuccessful()) {
            qi = getFinalQueueIterator(queue);
            callback.jobWasSuccesful(qi);
         } else {
            callback.JobFailed();
         }
      } catch (IOException ex) {
         Logger.getLogger(IRHDJob.class.getName()).log(Level.SEVERE, null, ex);
      }
   }

   public QueueIterator recoverQueueIterator() {
      qi = null;
//      for (Query q : queue) {
//         addQuery(q);
//      }
      qi = getFinalQueueIterator(queue);
      return qi;
   }

   public QueueIterator getFinalQueueIterator(Collection<Query> queue) {
      return new QueueIterator(retriever, path, queue);
   }

   /**
    * @return An ArrayList of Queries with retrieved Documents
    */
   public Collection<Query> getResults( ) {
      QueueIterator qi = getQueueIterator();
      Collection<Query> results = qi.nextVariant();
      log.info("getResults()");
      for (Query q : results) {
         log.info("getResults() %d %d", q.id, q.getID());  
      }
      qi.close();
      return results;
   }
   
   public Collection<Query> recoverResults( ) {
      QueueIterator qi = recoverQueueIterator();
      Collection<Query> results = qi.nextVariant();
      for (Query q : results) {
         log.info("query %d %d %s", q.id, q.queryresults.length, q.getStrategyClass());
      }
      //qi.close();
      return results;
   }
}
