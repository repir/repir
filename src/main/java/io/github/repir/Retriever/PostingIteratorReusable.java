package io.github.repir.Retriever;

import io.github.repir.Repository.EntityStoredFeature;
import io.github.repir.Repository.ResidentFeature;
import io.github.repir.Repository.TermDocumentFeature;
import io.github.repir.Strategy.RetrievalModel;
import io.github.repir.tools.lib.Log;
import java.util.ArrayList;

/**
 * Resuable version of PostingIterator, that loads as much data in memory as
 * possible, and resets the pointers to repeat retrieval for the same 
 * retrievalmodel, but for instance with different parameter settings or statistics.
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
         }else {
            t.setPartition(partition);
            t.readResident();
            tdf.add(t);
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
            ((ResidentFeature)t).readResident();
            dsf.add(t);
         } else if (partition != t.partition) {
            t.setPartition(partition);
         }
      }
   }
   
   @Override
   public void closeTDF( TermDocumentFeature f ) {
      //log.info("dont close reuse");
   }

}
