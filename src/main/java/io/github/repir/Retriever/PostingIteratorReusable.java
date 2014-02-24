package io.github.repir.Retriever;

import java.util.ArrayList;
import io.github.repir.Repository.EntityStoredFeature;
import io.github.repir.Repository.TermDocumentFeature;
import io.github.repir.Strategy.RetrievalModel;
import io.github.repir.tools.Lib.Log;

/**
 * Merges the posting lists of several terms into one Document iterator. The
 * Iterator generates a {@link Document} d, with an array of term positions
 * d.pos[term nr][], of which d.pos[term nr].length is the term frequency, d.tf
 * is the size of the document and d.docid and d.partition the documents ID.
 * <p/>
 * A PostingIterator can merge one or more segments, however, it can only be
 * used with the same set of terms for all segments. can have only one set of
 * terms, and term frequencies
 * <p/>
 * @author jeroen
 */
public class PostingIteratorReusable extends PostingIterator {

   public static Log log = new Log(PostingIteratorReusable.class);
   ArrayList<TermDocumentFeature> tdf = new ArrayList<TermDocumentFeature>();
   ArrayList<EntityStoredFeature> dsf = new ArrayList<EntityStoredFeature>();
   public int partition;

   /**
    * Constructs a PostingIterator for a single partition, which is commonly
    * used in each mapper.
    * <p/>
    * @param retrievalmodel
    * @param term
    * @param partition
    */
   public PostingIteratorReusable(int partition) {
      //log.info("new PostingIteratorReusable %d",partition);
      this.partition = partition;
   }
   
   public void reuse(RetrievalModel retrievalmodel) {
      lastdocid = -1;
      this.retrievalmodel = retrievalmodel;
      tdfarray = getTDF( retrievalmodel );
      for (TermDocumentFeature t : tdfarray) {
         boolean exists = false;
         for (TermDocumentFeature t1 : tdf){
            if (t.getCanonicalName().equals(t1.getCanonicalName())) {
               exists = true;
               break;
            }
         }
         if (exists) {
            t.reuse();
            log.info("reuse %s %d %d", t.getCanonicalName(), lastdocid, t.docid);
         }else {
            t.setPartition(partition);
            //t.openRead();
            t.readResident();
            tdf.add(t);
            log.info("new %s %b", t.getCanonicalName(), t.hasNext());
         }
      }
      dsfarray = getDSF( retrievalmodel );
      for (EntityStoredFeature t : dsfarray) {
         boolean exists = false;
         for (EntityStoredFeature t1 : dsf){
            if (t.getCanonicalName().equals(t1.getCanonicalName())) {
               exists = true;
               break;
            }
         }
         if (!exists) {
            t.setPartition(partition);
            //t.openRead();
            t.readResident();
            dsf.add(t);
            log.info("new %s %b", t.getCanonicalName(), t.hasNext());
         } else if (partition != t.partition) {
            t.setPartition(partition);
            log.info("reuse %s %b", t.getCanonicalName(), t.hasNext());
         }
      }
   }
   
   @Override
   public void closeTDF( TermDocumentFeature f ) {
      log.info("dont close reuse");
   }

}
