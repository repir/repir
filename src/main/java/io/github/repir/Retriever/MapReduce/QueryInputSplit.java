package io.github.repir.Retriever.MapReduce;

import io.github.repir.Repository.Repository;
import io.github.repir.Retriever.Query;
import io.github.repir.tools.io.buffer.BufferDelayedWriter;
import io.github.repir.tools.io.buffer.BufferReaderWriter;
import io.github.repir.tools.lib.Log;
import io.github.repir.MapReduceTools.MRInputSplit;

/**
 * A custom implementation of Hadoop's InputSplit used by RetrieverMR. 
 * Each Split holds a set of Queries tasks that must all belong to the same partition. 
 * <p/>
 * @author jeroen
 */
public class QueryInputSplit extends MRInputSplit<QueryWritable, Query> {

   public static Log log = new Log(QueryInputSplit.class);

   public QueryInputSplit() {
   }

   public QueryInputSplit(Repository repository, int partition) {
      super(repository, partition);
   }

   @Override
   public QueryWritable convert(Query q) {
      return new QueryWritable(q);
   }

   @Override
   public void writeKey(BufferDelayedWriter out, Query key) {
      key.write(out);
   }

   @Override
   public Query readKey(BufferReaderWriter reader) {
      Query q = new Query();
      q.read(reader);
      return q;
   }


}
