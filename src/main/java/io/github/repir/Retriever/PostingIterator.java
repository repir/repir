package io.github.repir.Retriever;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.TreeSet;
import io.github.repir.Repository.EntityStoredFeature;
import io.github.repir.Repository.StoredFeature;
import io.github.repir.Repository.StoredReportableFeature;
import io.github.repir.Repository.TermDocumentFeature;
import io.github.repir.Strategy.RetrievalModel;
import io.github.repir.tools.Lib.Log;
import io.github.repir.tools.Lib.ArrayTools;

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
public class PostingIterator {

   public static Log log = new Log(PostingIterator.class);
   TermDocumentFeature tdfarray[];
   EntityStoredFeature dsfarray[];
   TermDocumentFeature inuse[];
   int usedTDF = 0;
   RetrievalModel retrievalmodel;
   private int MAXMEMORY;
   int docfeaturebuffer;
   int lastdocid;
   int partition;

   /**
    * Constructs a PostingIterator for a single partition, which is commonly
    * used in each mapper.
    * <p/>
    * @param retrievalmodel
    * @param term
    * @param partition
    */
   public PostingIterator(RetrievalModel retrievalmodel, int partition) {
      this.retrievalmodel = retrievalmodel;
      lastdocid = -1;
      this.partition = partition;
      for (StoredFeature t : retrievalmodel.requestedfeatures.values()) {
         if (t instanceof TermDocumentFeature) {
            ((TermDocumentFeature) t).setPartition(partition);
         } else if (t instanceof EntityStoredFeature) {
            ((EntityStoredFeature) t).setPartition(partition);
         }
      }

      tdfarray = getTDF(retrievalmodel);
      dsfarray = getDSF(retrievalmodel);
      manageMemory();
   }
   int mask4096 = (~4095) & 0x7FFFFFFF;

   protected void manageMemory() {
      MAXMEMORY = retrievalmodel.repository.getConfigurationInt("retriever.iteratormaxmem", 500000000);
      //MAXMEMORY = 1000 * 4096;
      int memoryneeded = 0;
      ArrayList<StoredReportableFeature> sortedfeatures = sortByLength(dsfarray);
      int i = 0;
      for (; i < sortedfeatures.size(); i++) {
         StoredReportableFeature f = sortedfeatures.get(i);
         if (f.isReadResident()) {
            memoryneeded += f.getBytesSize();
            f.reuse();
         } else if (memoryneeded + f.getBytesSize() < MAXMEMORY / 2) {
            log.info("resident %s", f.getCanonicalName());
            f.readResident();
            memoryneeded += f.getBytesSize();
         } else {
            break;
         }
      }
      if (i < sortedfeatures.size()) {
         int memoryleft = (MAXMEMORY - memoryneeded) / (sortedfeatures.size() - i + tdfarray.length);
         int mempart = (memoryleft & mask4096) + 4096;
         for (; i < sortedfeatures.size(); i++) {
            StoredReportableFeature f = sortedfeatures.get(i);
            if (!f.isReadResident()) {
               f.setBufferSize(mempart);
               f.openRead();
            }
         }
      }
      sortedfeatures = sortByLength(tdfarray);
      for (i = 0; i < sortedfeatures.size(); i++) {
         StoredReportableFeature f = sortedfeatures.get(i);
         if (f.isReadResident()) {
            memoryneeded += f.getBytesSize();
            f.reuse();
         } else if (memoryneeded + f.getBytesSize() < MAXMEMORY) {
            f.readResident();
            memoryneeded += f.getBytesSize();
            f.next();
         } else {
            break;
         }
      }
      if (i < sortedfeatures.size()) {
         int memoryleft = (MAXMEMORY - memoryneeded) / (sortedfeatures.size() - i);
         int mempart = (memoryleft & mask4096) + 4096;
         for (; i < sortedfeatures.size(); i++) {
            StoredReportableFeature f = sortedfeatures.get(i);
            if (!f.isReadResident()) {
               f.setBufferSize(mempart);
               f.openRead();
               f.next();
            }
         }
      }
   }

   protected PostingIterator() {
   }

   protected ArrayList<StoredReportableFeature> sortByLength(StoredReportableFeature[] features) {
      ArrayList<StoredReportableFeature> sortedfeatures = new ArrayList<StoredReportableFeature>();
      for (StoredReportableFeature t : features) {
         sortedfeatures.add(t);
      }
      Collections.sort(sortedfeatures, new Comparator<StoredReportableFeature>() {
         public int compare(StoredReportableFeature o1, StoredReportableFeature o2) {
            return (o1.getBytesSize() < o2.getBytesSize()) ? -1 : 1;
         }
      });
      return sortedfeatures;
   }

   public EntityStoredFeature[] getDSF(RetrievalModel retrievalmodel) {
      int countdsf = 0;
      for (StoredFeature t : retrievalmodel.requestedfeatures.values()) {
         if (t instanceof EntityStoredFeature) {
            countdsf++;
         }
      }
      EntityStoredFeature[] dsfarray = new EntityStoredFeature[countdsf];
      for (StoredFeature t : retrievalmodel.requestedfeatures.values()) {
         if (t instanceof EntityStoredFeature) {
            EntityStoredFeature tt = (EntityStoredFeature) t;
            dsfarray[--countdsf] = tt;
         }
      }
      return dsfarray;
   }

   public TermDocumentFeature[] getTDF(RetrievalModel retrievalmodel) {
      int counttdf = 0;
      for (StoredFeature t : retrievalmodel.requestedfeatures.values()) {
         if (t instanceof TermDocumentFeature) {
            counttdf++;
         }
      }
      TermDocumentFeature[] tdfarray = new TermDocumentFeature[counttdf];
      for (StoredFeature t : retrievalmodel.requestedfeatures.values()) {
         if (t instanceof TermDocumentFeature) {
            TermDocumentFeature tt = (TermDocumentFeature) t;
            tdfarray[--counttdf] = tt;
         }
      }
      inuse = new TermDocumentFeature[tdfarray.length];
      for (int i = 0; i < tdfarray.length; i++) {
         tdfarray[i].sequence = i;
         inuse[i] = tdfarray[i];
      }
      usedTDF = tdfarray.length;
      return tdfarray;
   }

   public void closeTDF(TermDocumentFeature f) {
      f.closeRead();
   }

   public Document next() {
      Document d = null;
      int mindocid;
      boolean valid = false;
      do {
         mindocid = Integer.MAX_VALUE;
         for (int i = usedTDF - 1; i >= 0; i--) {
            TermDocumentFeature f = inuse[i];
            if (f.docid == lastdocid && !f.next()) {
               if (i < usedTDF - 1) {
                  ArrayTools.swap(inuse, i, usedTDF - 1);
                  usedTDF--;
               }
               closeTDF(f);
               continue;
            }
            if (f.docid > -1 && f.docid < mindocid) {
               if (f.docid < mindocid) {
                  mindocid = f.docid;
                  valid = f.meetsDependencies();
               } else if (f.docid == mindocid) {
                  valid |= f.meetsDependencies();
               }
            }
         }
         if (!valid) {
            NEXTTDF:
            for (int i = usedTDF - 1; i >= 0; i--) {
               TermDocumentFeature f = inuse[i];
               if (f.docid == mindocid && f.meetsDependencies()) {
                  valid = true;
                  break;
               }
            }
         }
         lastdocid = mindocid;
      } while (mindocid < Integer.MAX_VALUE && !valid);
      if (mindocid < Integer.MAX_VALUE) {
         d = retrievalmodel.createDocument(mindocid, retrievalmodel.partition);
         for (EntityStoredFeature dsf : dsfarray) {
            dsf.read(d);
         }
      }
      return d;
   }

   class PostingsIterator extends TreeSet<TermDocumentFeature> {
   }
}
