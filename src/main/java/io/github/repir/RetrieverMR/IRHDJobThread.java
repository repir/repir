package io.github.repir.RetrieverMR;

import java.util.ArrayList;
import java.util.Collection;
import io.github.repir.Repository.Repository;
import io.github.repir.tools.DataTypes.Configuration;
import io.github.repir.Retriever.Query;
import io.github.repir.Retriever.Retriever;
import io.github.repir.TestSet.TestSet;

/**
 * Wrapper around a testset being run, enabling simultaneous sets to be run
 * with different configurations.
 * @author jeroen
 */
public abstract class IRHDJobThread implements RetrieverMRCallback {
   protected Configuration configuration;
   protected Repository repository;
   protected RetrieverMR retriever;
   protected TestSet testset;
   
   public IRHDJobThread( RetrieverMR retriever, ArrayList<Query> queries ) {
      this.configuration = retriever.repository.getConfiguration();
      this.repository = retriever.repository;
      this.retriever = retriever;
      RetrieverMRInputFormat.setSplitable(true);
      RetrieverMRInputFormat.setIndex(repository);
      retriever.retrieveThreadedQueries(queries, this);
   }
   
   public IRHDJobThread( Configuration conf, ArrayList<Query> queries ) {
      this(new RetrieverMR(new Repository(conf)), queries);
   }
   
   public IRHDJobThread( Repository repository ) {
      this( new RetrieverMR(repository) );
   }
   
   public IRHDJobThread( RetrieverMR retriever ) {
      this( retriever, new TestSet(retriever.repository).getQueries(retriever));
   }
   
   public IRHDJobThread( Configuration conf ) {
      this( new Repository(conf));
   }
   
}
