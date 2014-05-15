package io.github.repir.Retriever.MapReduce;

import io.github.repir.tools.MapReduce.Job;
import io.github.repir.Retriever.Retriever;
import io.github.repir.Retriever.Query;
import io.github.repir.tools.Lib.Log;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.UUID;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.mapreduce.lib.output.NullOutputFormat;
import io.github.repir.Strategy.RetrievalModel;
import io.github.repir.Strategy.Strategy;

/**
 * Extension of Hadoop Job, used by JobManager to start multi-threaded 
 * rerieval.
 * @author jer
 */
public class RetrieverJob extends Job {

   public static Log log = new Log(RetrieverJob.class);
   public String path;
   public Retriever retriever;
   protected Collection<Query> queue;
   protected QueueIterator qi;
   protected QueryInputFormat inputformat;

   public RetrieverJob(Retriever retriever) throws IOException {
      super(retriever.repository, "Retriever " + retriever.repository.configuredString("rr.conf"));
      this.retriever = retriever;
      inputformat = new QueryInputFormat(retriever.repository);
      path = conf.get("retriever.tempdir", "") + UUID.randomUUID().toString();
      conf.setBoolean("mapred.reduce.tasks.speculative.execution", false);
      setJob();
   }
   
   public RetrieverJob(Retriever retriever, String path) throws IOException {
      this(retriever);
      if (path != null) {
         this.path = path;
      }
   }
   
   public void setQueries( Collection<Query> queries ) {
      this.queue = queries;  
   }

   public void setJob() {
      setMapOutputKeyClass(CollectorKey.class);
      setMapOutputValueClass(CollectorValue.class);
      setOutputKeyClass(NullWritable.class);
      setOutputValueClass(NullWritable.class);
      setMapperClass(RetrieverMRMap.class);
      setReducerClass(RetrieverMRReduce.class);
      setInputFormatClass(inputformat.getClass());
      setOutputFormatClass(NullOutputFormat.class);
      setGroupingComparatorClass(CollectorKey.FirstGroupingComparator.class);
      setSortComparatorClass(CollectorKey.FirstGroupingComparator.class);
      setPartitionerClass(CollectorKey.partitioner.class);
   }
   
   public void addQuery(Query q) {
      inputformat.add(q);
   }
   
   public void addSingleQuery(Query q) {
      inputformat.addSingle(q);
   }
   
   public String[] getReducers(Collection<String> reducers) {
      return reducers.toArray(new String[ reducers.size() ]);
   }
   
   public void setReducers(Collection<String> reducers) {
      String reducerlist[] = getReducers(reducers);
      conf.setStrings("retriever.reducers", reducerlist);
      setNumReduceTasks( reducerlist.length );
   }
   
   public void startJob() {
      try {
         conf.set("topicrun.outfile", path);
         retriever.repository.unloadStoredDynamicFeatures();
         HashSet<String> reducers = new HashSet<String>();
         for (Query q : queue) {
            Strategy rm = retriever.constructStrategy(q);
            reducers.addAll(rm.reducerList());
            if (rm instanceof RetrievalModel)
               addQuery(q);
            else
               addSingleQuery(q);
         }
         setReducers( reducers );
         this.submit();
      } catch (Exception ex) {
         log.exception(ex, "startJob()");
      }
   }
   
   public QueueIterator getQueueIterator() {
      qi = null;
      try {
         startJob();
         this.waitForCompletion(true);
         qi = getFinalQueueIterator(queue);
      } catch (Exception ex) {
         log.fatalexception(ex, "getQueueIterator()");
      }
      return qi;
   }
   
   public void doJob(boolean wait) {
      try {
         startJob();
         if (wait)
             this.waitForCompletion(true);
      } catch (Exception ex) {
         log.fatalexception(ex, "doJob()");
      }
   }

   public QueueIterator recoverQueueIterator() {
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
      qi.close();
      return results;
   }
   
   /**
    * When a retrieval process was started, the MapReduce finished ok, but the 
    * calling process died, this routine can help recover the results, that were 
    * written to result files on HDFS.
    * @return 
    */
   public Collection<Query> recoverResults( ) {
      QueueIterator qi = recoverQueueIterator();
      Collection<Query> results = qi.nextVariant();
      return results;
   }
}
