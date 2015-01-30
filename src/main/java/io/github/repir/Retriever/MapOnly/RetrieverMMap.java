package io.github.repir.Retriever.MapOnly;

import io.github.repir.Repository.Repository;
import io.github.repir.Retriever.MapReduce.QueryWritable;
import io.github.repir.Retriever.MapReduce.QueryInputSplit;
import io.github.repir.Retriever.Query;
import io.github.repir.Retriever.Retriever;
import io.github.repir.Strategy.RetrievalModel;
import io.github.repir.Strategy.Strategy;
import io.github.repir.tools.lib.Log;
import java.io.IOException;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.mapreduce.Mapper;

/**
 * The mapper is generic, and collects data for a query request, using the
 * passed retrieval model, scoring function and query string. The common
 * approach is that each node processes all queries for one index partition. The
 * collected results are reshuffled to one reducer per query where all results
 * for a single query are aggregated.
 * <p/>
 * @author jeroen
 */
public class RetrieverMMap extends Mapper<IntWritable, QueryWritable, NullWritable, NullWritable> {

   public static Log log = new Log(RetrieverMMap.class);
   Context context;
   private Repository repository;
   private Retriever retriever;
   QueryInputSplit split;
   String retrievalmodel;

   @Override
   protected void setup(Context context) throws IOException, InterruptedException {
      this.context = context;
      repository = new Repository(context.getConfiguration());
      retriever = new Retriever(repository);
      split = (QueryInputSplit) context.getInputSplit();
   }

   @Override
   public void map(IntWritable inkey, QueryWritable invalue, Context context) throws IOException, InterruptedException {
      //log.info("query %d %s %s %d", invalue.query.id, invalue.query.query, invalue.query.retrievalmodelclass, invalue.query.limit);
      Query q = invalue.getQuery(repository);
      Query retrieveQuery = retriever.retrieveQuery(q); // collect results for query from one index partition
      Strategy strategy = new RetrievalModel( retriever );
      strategy.prepareWriteReduce(retrieveQuery);
      strategy.writeReduce(retrieveQuery);
      strategy.finishWriteReduce();
   }

   @Override
   protected void cleanup(Context context) throws IOException, InterruptedException {
   }
}
