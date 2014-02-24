package io.github.repir.Retriever;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.TreeSet;
import io.github.repir.Strategy.Collector.Collector;
import io.github.repir.Strategy.Collector.CollectorCachable;
import io.github.repir.Extractor.Entity;
import io.github.repir.Extractor.EntityAttribute;
import io.github.repir.Extractor.Extractor;
import io.github.repir.Extractor.ExtractorQuery;
import io.github.repir.Extractor.Tools.ConvertToLowercase;
import io.github.repir.Extractor.Tools.ConvertToLowercaseQuery;
import io.github.repir.Extractor.Tools.StemTokensQuery;
import io.github.repir.Repository.ReportableFeature;
import io.github.repir.Repository.Repository;
import io.github.repir.Repository.StoredDynamicFeature;
import io.github.repir.Repository.StoredReportableFeature;
import io.github.repir.RetrieverMR.RetrieverMR;
import io.github.repir.Strategy.RetrievalModel;
import io.github.repir.Strategy.Strategy;
import io.github.repir.tools.Content.RecordHeaderDataRecord;
import io.github.repir.tools.DataTypes.TreeMapComparable;
import io.github.repir.tools.DataTypes.TreeMapComparable.TYPE;
import io.github.repir.tools.DataTypes.TreeSetComparable;
import io.github.repir.tools.Lib.Log;
import io.github.repir.tools.Stemmer.englishStemmer;

/**
 * Gives access to an existing {@link Repository.Repository}. The most common
 * way to setup an Retriever is to readValue the repository Configuration from
 * file, create an Repository object using the Repository configuration, and
 * create an Retriever using the Repository.
 * <p/>
 * The most common way to retrieveQueries a single Query, is to construct a
 * Query object using {@link #constructDefaultQuery(java.lang.String)} and call
 * {@link #retrieveQuery(Retriever.Query)}. The query object that is returned
 * contains the results.
 * <p/>
 * Under the hood, the retrieval process is separated in different objects
 * working together. The Query contains the name of the
 * {@link Strategy.Strategy} class, which is used to construct a Retrieval
 * Model. The Retrieval Model will use the query to build an inference model,
 * i.e. the query is parsed into a network of nodes, that first extract all
 * necessary usedfeatures from a document, then process those usedfeatures,
 * resulting in collected results.
 * <p/>
 */
public class Retriever {

   public static Log log = new Log(Retriever.class);
   public static Log log2 = new Log(RetrieverMR.class);
   public Repository repository;
   public ExtractorQuery extractor;
   public ArrayList<Query> queue = new ArrayList<Query>();
   public static englishStemmer stemmer = englishStemmer.get();

   /**
    * @param repository The Repository containing the location, filename,
    * usedfeatures and statistics for the repository
    */
   protected Retriever() {
   }

   public Retriever(Repository repository) {
      this.repository = repository;
   }

   public Repository getRepository() {
      return repository;
   }

   final int mask4096 = (~4095) & 0x7FFFFFFF;
   
   /**
    * Optimize reading reported containedfeatures after retrieval. The documents
    * are sorted in physical order to enable sequential read. If the estimated
    * number of byte between the feature data is large, small random reads are
    * used, otherwise the file buffer is maximized as reading the whole thing is
    * faster than random reads.
    *
    * @param docs
    * @param containedfeatures
    */
   public void readReportedStoredFeatures(Collection<Document> docs, Collection<StoredReportableFeature> features, int partition) {
      log.s("readReportedStoredFeatures");
      int MAXMEMORY = 100000000;
      TreeSet<Document> docids = new TreeSet<Document>(new Comparator<Document>() {
         public int compare(Document a, Document b) {
            return a.docid - b.docid;
         }
      });
      docids.addAll(docs);

      int memoryleft = MAXMEMORY;
      TreeMapComparable<Long, StoredReportableFeature> sizes = new TreeMapComparable<Long, StoredReportableFeature>(TYPE.DUPLICATESASCENDING);
      for (StoredReportableFeature f : features) {
         if (f.partition != partition) {
            f.setPartition(partition);
            if (!f.isReadResident())
               sizes.put(f.getBytesSize(), f);
         } else {
            memoryleft -= f.getBytesSize();
         }
      }
      int featuresleft = features.size();
      for (Map.Entry<Long, StoredReportableFeature> entry : sizes.entrySet()) {
         if (entry.getKey() < memoryleft) {
            entry.getValue().readResident();
            memoryleft -= entry.getKey();
            featuresleft--;
         } else {
            break;
         }
      }
      for (StoredReportableFeature f : features) {
         if (!f.isReadResident()) {
            f.setBufferSize((int)Math.min(50000000, f.getBytesSize()));
            f.openRead();
         } else {
            f.reuse();
         }
         for (Document d : docids) {
            f.read(d);
            ((ReportableFeature)f).report(d);
         }
         if (!f.isReadResident()) {
            f.closeRead();
         }
      }
      
      log.e("readReportedStoredFeatures");
   }

   /**
    * a wrapper for {@link #retrieveQuery(Retriever.Query) }
    * <p/>
    * @param query the string that represents the user's need
    * @return a Query object that contains the original query and the retrieved
    * documents
    */
   public Query retrieveQuery(String query) {
      Query q = constructQueryRequest(0, query);
      return retrieveQuery(q);
   }

   /**
    * @param query a string that describes the documents to retrieveQueries.
    * @return a Query object containing the query string and the current
    * settings as the search parameters.
    */
   public Query constructQueryRequest(String query) {
      return constructQueryRequest(0, query);
   }

   /**
    * @param query a string that describes the documents to retrieveQueries.
    * @return a Query object containing the query string and the current
    * settings as the search parameters.
    */
   public Query constructQueryRequest(int id, String query) {
      Query q = new Query(repository, id, query);
      q.documentlimit = repository.getConfiguration().getInt("retriever.documentlimit", 10);
      q.setStrategyClass(repository.getConfiguration().getSubString("retriever.strategy", "RetrievalModel"));
      q.setScorefunctionClass(repository.getConfiguration().getSubString("retriever.scorefunction", "ScoreFunctionKLD"));
      q.documentclass = repository.getConfiguration().getSubString("retriever.documentclass", Document.class.getCanonicalName());
      q.documentcomparatorclass = repository.getConfiguration().getSubString("retriever.documentcomparatorclass", DocumentComparator.class.getCanonicalName());
      q.performStemming = repository.getConfiguration().getBoolean("retriever.stem", false);
      q.performLowercasing = repository.getConfiguration().getBoolean("retriever.lowercase", false);
      q.removeStopwords = repository.getConfiguration().getBoolean("retriever.removestopwords", false);
      return q;
   }

   public Strategy constructStrategy(Query q) {
      return Strategy.create(this, q);
   }

   /**
    * retrieves a single query described in the Query object. The Retrieval
    * Model decides what is retrieved (i.e. only document id's or if title, url,
    * etc. are included). If necessary use readDocuments to readValue the
    * documents meta data.
    * <p/>
    * This function uses an iterative retrieval strategy that uses multi-pass
    * retrieval whenever the retrieval model for a query decides that a results
    * retrieval pass is necessary. By default, optional stemming and lowercasing
    * is only done on the first pass.
    * <p/>
    * @param q the string that represents the user's need
    * @return the passed Query object is expanded with the retrieved documents
    */
   public Query retrieveQuery(Query q) { // implements local retrieval strategy
      this.tokenizeQuery(q);
      while (!q.done()) {
         Strategy retrievalmodel = constructStrategy(q);
         q = retrieveSinglePass(retrievalmodel);
         log.info("results %s %s", q.getScorefunctionClass(), q.getStrategyClass(), q.queryresults);
      }
      return q;
   }

   public Query retrieveSinglePass(Strategy strategy) { // implements local retrieval strategy
      //log.info("retrieveSinglePass %d %s", repository.getPartitions(), strategy.query.query);
      strategy.prepareAggregation();
      HashSet<Integer> collected = new HashSet<Integer>();
      if (strategy instanceof RetrievalModel) {
         for (int i = 0; i < repository.getPartitions(); i++) {
            Strategy results = retrieveSegment(strategy.query, i);
            for (int collector = 0; collector < results.collectors.size(); collector++) {
               log.info("aggregate %d", collector);
               Collector aggregator = strategy.collectors.get(collector);
               aggregator.aggregate(results.collectors.get(collector));
               collected.add(collector);
            }
         }
      } else {
         Strategy results = retrieveSegment(strategy.query, -1);
         strategy.collectors = results.collectors;
         for (Collector c : results.collectors) {
            collected.add(results.collectors.indexOf(c));
         }
      }
      strategy.collectors.finishReduce();

      for (Collector sdf : strategy.collectors) {
         if (sdf instanceof CollectorCachable)
         repository.unloadStoredDynamicFeature(((CollectorCachable)sdf).getStoredDynamicFeature());
      }

      return strategy.finishReduceTask();
   }

   /**
    * low level method that retrieves data from a single partition. Typically,
    * this method is used by Map-Reduce processes to retrieveQueries results on
    * one node.
    * <p/>
    * @param query the query object that contains the request
    * @param partition the id of the repository partition
    * @return a set of collectors containing the retrieved results.
    */
   public Strategy retrieveSegment(Query query, int segment) {
      //log.info("retrieveSegment( %d )", segment);
      Strategy strategy = constructStrategy(query);
      strategy.partition = segment;
      retrieveSegment(strategy);
      return strategy;
   }
   
   public void retrieveSegment(Strategy strategy) {
      strategy.prepareAggregation();
      strategy.prepareRetrieval();
      strategy.doMapTask();
      strategy.collectors.postLoadFeatures(strategy.partition);
      strategy.collectors.finishSegmentRetrieval();
   }
   
   public PostingIterator getPostingIterator(RetrievalModel strategy, int partition) {
      strategy.root.setTDFDependencies();
      return new PostingIterator(strategy, partition);
   }

   /**
    * retrieves all queued queries.
    * <p/>
    * @param queue A list of Queries to retrieveQueries
    * @return ArrayList with results for all queued queries.
    */
   public ArrayList<Query> retrieveQueries(ArrayList<Query> queue) {
      for (Query q : queue) {
         Query retrieveQuery = this.retrieveQuery(q);
      }
      return queue;
   }

   public ArrayList<Query> retrieveQueue() {
      return retrieveQueries(queue);
   }

   public Extractor getExtractor() {
      if (extractor == null) {
         extractor = new ExtractorQuery(repository);
      }
      return extractor;
   }

   public void tokenizeQuery(Query q) {
      q.stemmedquery = tokenizeString(q);
      q.performLowercasing = false;
      q.performStemming = false;
   }

   /**
    * tokenize the string in the query request. The configuration file is used
    * to configure the extractor similar to the one used for indexing, except
    * the removal of special query characters and long numbers. For stemming and
    * lowercasing the setting in the query object are used rather than the
    * repository configuration. FunctionNames: are not lowercased.
    * <p/>
    * @param q the query request that contains the query string.
    * @return a String that contains the tokenized version.
    */
   public String tokenizeString(Query q) {
      //log.info("tokenizeString %d %s %b %b %b", q.id, q.originalquery, q.performLowercasing, q.performStemming, q.removeStopwords);
      getExtractor();
      if (!q.performLowercasing)
         extractor.removeProcessor("irefquery", ConvertToLowercaseQuery.class);
      if (!q.performStemming)
         extractor.removeProcessor("irefquery", StemTokensQuery.class);
      String query = q.originalquery.replaceAll("[!/]", " ").replaceAll("\\s+", " ");
      Entity entity = new Entity();
      ConvertToLowercase lc = new ConvertToLowercase(extractor, "");
      entity.content = query.getBytes();
      extractor.process(entity);
      StringBuilder sb = new StringBuilder();
      ArrayList<String> finalterms = new ArrayList<String>();
      EntityAttribute channel = entity.get("irefquery");
      for (String chunk : channel) {
         char last = sb.length() == 0 ? 0 : sb.charAt(sb.length() - 1);
         char first = chunk.length() == 0 ? 0 : chunk.charAt(0);
         if ("#=:|\0-".indexOf(first) < 0 && "#=:|\0-".indexOf(last) < 0) {
            sb.append(" ");
         }
         sb.append(chunk);
      }
      String result = sb.toString().trim().replaceAll("\\s+", " ");
      return result;
   }

   public void mapperProgress() {
   }

   public void reducerProgress() {
   }

   /**
    * adds a query to the queue, for batch-wise retrieval.
    * <p/>
    * @param q Query object that contains the query request.
    */
   public void addQueue(Query q) {
      queue.add(q);
   }

   /**
    * adds all Queries in the ArrayList to the queue for batch-wise retrieval.
    * <p/>
    * @param list List of Queries that contain the query requests.
    */
   public void addQueue(ArrayList<Query> list) {
      queue.addAll(list);
   }

   public void setQueue(ArrayList<Query> list) {
      queue = (ArrayList<Query>)list.clone();
   }

   /**
    * returns the queue for debugging purposes, but does not retrieveQueries
    * anything.
    * <p/>
    * @return the queued queries
    */
   public ArrayList<Query> getQueue() {
      return queue;
   }
}
