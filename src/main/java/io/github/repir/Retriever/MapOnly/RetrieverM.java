package io.github.repir.Retriever.MapOnly;

import io.github.repir.Repository.Repository;
import io.github.repir.tools.Lib.Log;
import java.io.IOException;

/**
 * An implementation of Retriever that retrieves queries using only a Mapper. 
 * This is not the recommended mode to operate, but can optionally be used
 * if Reducers have a heavy load. 
 * @author jeroen
 */
public class RetrieverM extends io.github.repir.Retriever.MapReduce.Retriever {

   public static Log log = new Log(RetrieverM.class);

   public RetrieverM(Repository repository) {
      super(repository);
   }

   /**
    * The Mapper context is used to report progress, to prevent processes form
    * being killed while still working.
    * <p/>
    * @param repository
    * @param mappercontext
    */
   public RetrieverM(Repository repository, org.apache.hadoop.mapreduce.Mapper.Context mappercontext) {
      super( repository, mappercontext );
   }
   
   @Override
   public IRHDJobM createJob(String path) throws IOException {
      return new IRHDJobM( this, path );  
   }
}
