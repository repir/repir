package io.github.repir.RetrieverTuner;

import io.github.repir.Retriever.PostingIteratorReusable;
import java.io.IOException;
import io.github.repir.Repository.Repository;
import io.github.repir.RetrieverMR.RetrieverMR;
import io.github.repir.tools.Lib.Log; 

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
 * necessary storedfeatures from a document, then process those storedfeatures,
 * resulting in collected results.
 * <p/>
 */
public class RetrieverTuner extends RetrieverMR {

   public static Log log = new Log(RetrieverTuner.class);
   protected PostingIteratorReusable postingiterator;
   

   public RetrieverTuner(Repository repository) {
      super(repository);
   }

    public RetrieverTuner(Repository repository, org.apache.hadoop.mapreduce.Mapper.Context mappercontext) {
      super(repository, mappercontext);
   }

   public RetrieverTuner(Repository repository, org.apache.hadoop.mapreduce.Reducer.Context reducercontext) {
      super(repository, reducercontext);
   }
   
   @Override
   public IRHDJobTuner createIRHDJob(String path) throws IOException {
      return new IRHDJobTuner( this, path );  
   }
}
