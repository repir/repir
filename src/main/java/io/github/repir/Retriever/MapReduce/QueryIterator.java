package io.github.repir.Retriever.MapReduce;

import io.github.repir.tools.Content.Datafile;
import io.github.repir.Retriever.Document;
import io.github.repir.Retriever.Retriever;
import io.github.repir.Retriever.Query;
import io.github.repir.tools.Lib.Log;
import java.util.Iterator;
import io.github.repir.Strategy.RetrievalModel;
import io.github.repir.tools.Content.EOCException;

/**
 * QueryIterator is a helper class for IndexReaderHD, that can be used in cases
 * were the retrieved results are too big to keep in memory. By default, the
 * results for each Query is written by a separate Reducer to a file. Instead of
 * reading these result files in memory, a QueueIterator can be returned, using
 * {@link IndexReaderHD#retrieveQueueIterator(java.util.ArrayList)} to iterate
 * over the results. Each iteration a {@link QueryIterator} is obtained that can
 * be used to access the {@link Query} header, and to iterate over the retrieved
 * {@link Document}s.
 * <p/>
 * Alternatively, when 
 * @author jeroen
 */
public class QueryIterator implements Iterable<Document>, Iterator<Document> {

   public static Log log = new Log(QueryIterator.class);
   public Query query;
   public Datafile df;
   int documentcount;
   Retriever retriever;
   RetrievalModel retrievalmodel;

   public QueryIterator(RetrievalModel rm, Datafile datafile) {
      //log.info("new QueryIterator length %d", datafile.getLength());
      this.retrievalmodel = rm;
      this.retriever = rm.retriever;
      this.df = datafile;
      query = new Query();
      try {
         query.readHeader(datafile);
         documentcount = datafile.readInt();
      } catch (EOCException ex) {
         log.fatalexception(ex, "QueryIterator( %s, %s, %s )", retriever, datafile, query);
      }
   }

   @Override
   public Iterator<Document> iterator() {
      return this;
   }

   @Override
   public boolean hasNext() {
      return documentcount > 0;
   }

   @Override
   public Document next() {
      Document doc = null;
      if (documentcount-- > 0) {
         doc = query.createDocument();
         doc.read(df);
         doc.setRetrievalModel(retrievalmodel);
         doc.decode();
      }
      return doc;
   }

   public Query readAll() {
      for (Document d : this) {
         query.add(d);
      }
      query.setQueryResults();
      return query;
   }
   
   @Override
   public void remove() {
      throw new UnsupportedOperationException("Not supported yet.");
   }
}
