package io.github.repir.Retriever.MapReduce;

import io.github.htools.io.Datafile;
import io.github.repir.Retriever.Retriever;
import io.github.repir.Retriever.Query;
import io.github.htools.lib.Log;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import io.github.repir.Retriever.Document;
import io.github.repir.Retriever.MapReduce.QueueIterator.QueryVariantIterator;
import io.github.repir.Strategy.RetrievalModel;
import io.github.repir.Strategy.Strategy;

/**
 * QueueIterator is a helper class for {@link IndexReaderHD}, that can be used
 * in cases were the retrieved results are too big to keep in memory. By
 * default, the results for each Query is written by a separate Reducer to a
 * file. Instead of reading these result files in memory, a QueueIterator can be
 * returned, that enables a program to iterate over the Queue with Query
 * results.
 * <p/>
 * To doTask a QueueIterator, call
 * {@link IndexReaderHD#retrieveQueueIterator(java.util.ArrayList)}. Each
 * iteration will supply a {@link QueryIterator}, that will read the Query
 * header and enables to iterate over the retrieved Documents.
 * <p/>
 * @author jeroen
 */
public class QueueIterator implements Iterable<QueryVariantIterator>, Iterator<QueryVariantIterator> {
 
   public static Log log = new Log(QueueIterator.class);
   int currentvariant = 0;
   Iterator<Query> variants;
   QueryVariantIterator current, next;
   public Retriever retriever;
   public Collection<Query> queries;
   public String path;

   public QueueIterator(Retriever retriever, String path, Collection<Query> queries) {
      //log.info("queries %d", queries.size());
      this.retriever = retriever;
      this.path = path;
      this.queries = queries;
      for (Query q : queries) {
         if (variants == null)
            variants = q.variantIterator().iterator();
         if (q.strategy == null) {
            q.strategy = Strategy.create(retriever, q);
         }
      }
      next();
   }

   @Override
   public Iterator<QueryVariantIterator> iterator() {
      return this;
   }

   @Override
   public boolean hasNext() {
      return next != null;
   }

   @Override
   public QueryVariantIterator next() {
      current = next;
      next = null;
      if (variants.hasNext()) {
         next = new QueryVariantIterator(variants.next().getVariantID(), path);
      }
      return current;
   }

   /**
    * Deletes the results files
    */
   public void close() {
      for (Query q : queries) {
         for (Query q1 : q.variantIterator()) {
            Datafile df = new Datafile(retriever.repository.getFS(), path + "_" + q.getID() + "_" + q.getVariantID());
            df.delete();
         }
      }
   }
   
   public ArrayList<Query> nextVariant() {
      ArrayList<Query> results = new ArrayList<Query>();
      if (hasNext()) {
         QueryVariantIterator qq = next();
         for (QueryIterator qi : qq) {
           for (Document d : qi) {
              qi.query.add(d);
           }
           results.add(qi.query);
         }
      }
      return results;
   }
   
   public class QueryVariantIterator implements Iterator<QueryIterator>, Iterable<QueryIterator> {
      Iterator<Query> queue;
      Query currentquery;
      QueryIterator next, current;
      String path;
      int variant;
      
      public QueryVariantIterator( int variant, String path ) {
         queue = QueueIterator.this.queries.iterator();
         this.variant = variant;
         this.path = path;
         next();
      }
      
      public boolean hasNext() {
         return next != null;
      }

      public QueryIterator next() {
         if (current != null) {
            current.df.closeRead();
         }
         current = next;
         next = null;
         if (queue.hasNext()) {
            Query q = queue.next();
            for (Query qq : q.variantIterator()) {
               if (qq.getVariantID() == variant)
                  break;
            }

            Datafile df = new Datafile(retriever.repository.getFS(), path + "_" + q.getID() + "_" + q.getVariantID());
            //log.info("try file %s %b", df.getFilename(), df.exists());
            if (df.exists()) {
//               log.info("QueryVariant %s %s", q.strategy.getClass().getSimpleName(), q.variants.get(variant));
//               if (q.strategy.getClass().getSimpleName().equals(q.variants.get(variant))) {
//                  q = new Query(q);
//                  q.strategy = Strategy.create(retriever, q);
//               }
               df.setBufferSize(10000);
               df.openRead();
               next = new QueryIterator((RetrievalModel) q.strategy, df);
            }
         }
         return current;
      }

      public void remove() {
         throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
      }

      public Iterator<QueryIterator> iterator() {
         return this;
      }
   }
   
   
   @Override
   public void remove() {
      throw new UnsupportedOperationException("Not supported yet.");
   }

}
