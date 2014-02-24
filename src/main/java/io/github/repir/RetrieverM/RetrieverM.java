package io.github.repir.RetrieverM;

import io.github.repir.Repository.Repository;
import io.github.repir.tools.Lib.Log;
import java.io.IOException;
import io.github.repir.RetrieverMulti.IRHDJobMulti;

/**
 * An implementation of Retriever that retrieves queries using the MapReduce
 * framework. After each pass, queries that are complete (i.e. there is no
 * consecutive Strategy to run) are stored in the finalresults, and
 * queries that require an additional retrieval pass are resubmitted to the
 * MapReduce framework. The queries can therefore have
 * <p/>
 * @author jeroen
 */
public class RetrieverM extends io.github.repir.RetrieverMR.RetrieverMR {

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
   public IRHDJobM createIRHDJob(String path) throws IOException {
      return new IRHDJobM( this, path );  
   }
}
