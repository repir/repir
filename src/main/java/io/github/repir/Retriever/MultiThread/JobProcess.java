package io.github.repir.Retriever.MultiThread;

import java.util.ArrayList;
import java.util.Collection;
import io.github.repir.Repository.Repository;
import io.github.repir.MapReduceTools.RRConfiguration;
import io.github.repir.Retriever.Query;
import io.github.repir.Retriever.MapReduce.QueryInputFormat;
import io.github.repir.TestSet.TestSet;

/**
 * Wrapper around a testset being run, enabling simultaneous sets to be run
 * with different configurations.
 * @author jeroen
 */
public abstract class JobProcess implements JobThreadCallback {
   protected RRConfiguration configuration;
   protected Repository repository;
   protected Retriever retriever;
   protected TestSet testset;
   protected QueryInputFormat inputformat;
   
   public JobProcess( Retriever retriever, ArrayList<Query> queries ) {
      this.configuration = retriever.repository.getConfiguration();
      inputformat = new QueryInputFormat(repository);
      this.repository = retriever.repository;
      this.retriever = retriever;
      retriever.retrieveThreadedQueries(queries, this);
   }
   
   public JobProcess( RRConfiguration conf, ArrayList<Query> queries ) {
      this(new Retriever(new Repository(conf)), queries);
   }
   
   public JobProcess( Repository repository ) {
      this( new Retriever(repository) );
   }
   
   public JobProcess( Retriever retriever ) {
      this( retriever, new TestSet(retriever.repository).getQueries(retriever));
   }
   
   public JobProcess( RRConfiguration conf ) {
      this( new Repository(conf));
   }
   
}
