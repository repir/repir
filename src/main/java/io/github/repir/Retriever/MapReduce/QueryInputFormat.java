package io.github.repir.Retriever.MapReduce;

import io.github.repir.Repository.Repository;
import io.github.repir.Retriever.Query;
import io.github.repir.tools.lib.Log;
import io.github.repir.MapReduceTools.MRInputFormat;
import io.github.repir.MapReduceTools.MRInputSplit;

/**
 * A custom implementation of Hadoop's InputFormat, that holds the InputSplits
 * that are to be retrieved. This class should be used as static, using
 * {@link #setIndex(Repository.Repository)} to initialize and 
 * {@link #add(Repository.Repository, IndexReader.Query) }
 * to add Query requests to the MapReduce job. Internally, a separate InputSplit
 * is created for each repository partition. Whenever a Query request is added,
 * it is added to each Split.
 * <p/>
 * When cansplit==true, then the InputSplits are divided over 2 * nodes in cluster
 * (as defined in cluster.nodes), to divide the workload more evenly.
 * 
 * @author jeroen
 */
public class QueryInputFormat extends MRInputFormat<QueryWritable, Query> {

   public static Log log = new Log(QueryInputFormat.class);

   public QueryInputFormat() {}
   
   public QueryInputFormat(Repository repository) {
      super(repository);
   }
   
   @Override
   public MRInputSplit<QueryWritable, Query> createIS(Repository repository, int partition) {
      return new QueryInputSplit(repository, partition);
   }
}
