package io.github.repir.Retriever.MultiThread;

import java.util.ArrayList;
import java.util.Collection;
import io.github.repir.Repository.Repository;
import io.github.repir.tools.DataTypes.Configuration;
import io.github.repir.Retriever.Query;
import io.github.repir.Retriever.MapReduce.RetrieverMRInputFormat;
import io.github.repir.TestSet.TestSet;

/**
 * Wrapper around a testset being run, enabling simultaneous sets to be run
 * with different configurations.
 * @author jeroen
 */
public abstract class JobProcess implements JobThreadCallback {
   protected Configuration configuration;
   protected Repository repository;
   protected Retriever retriever;
   protected TestSet testset;
   
   public JobProcess( Retriever retriever, ArrayList<Query> queries ) {
      this.configuration = retriever.repository.getConfiguration();
      this.repository = retriever.repository;
      this.retriever = retriever;
      RetrieverMRInputFormat.setSplitable(true);
      RetrieverMRInputFormat.setIndex(repository);
      retriever.retrieveThreadedQueries(queries, this);
   }
   
   public JobProcess( Configuration conf, ArrayList<Query> queries ) {
      this(new Retriever(new Repository(conf)), queries);
   }
   
   public JobProcess( Repository repository ) {
      this( new Retriever(repository) );
   }
   
   public JobProcess( Retriever retriever ) {
      this( retriever, new TestSet(retriever.repository).getQueries(retriever));
   }
   
   public JobProcess( Configuration conf ) {
      this( new Repository(conf));
   }
   
}
