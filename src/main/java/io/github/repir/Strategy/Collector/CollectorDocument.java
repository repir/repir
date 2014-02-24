package io.github.repir.Strategy.Collector;

import java.io.EOFException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.TreeSet;
import io.github.repir.Repository.ReportedUnstoredFeature;
import io.github.repir.Strategy.GraphComponent.ANNOUNCEKEY;
import io.github.repir.Strategy.GraphNode;
import io.github.repir.Strategy.Tools.ScoreFunction;
import io.github.repir.Retriever.Document;
import io.github.repir.Strategy.RetrievalModel;
import io.github.repir.Strategy.Strategy;
import io.github.repir.tools.Content.StructureReader;
import io.github.repir.tools.Content.StructureWriter;
import io.github.repir.tools.Lib.Log;

/**
 * Default mastercollector to retrieve a ranked list of documents. Each document
 * is scored by the documentPrior + sum of leaf usedfeatures. The {@link Retriever#getDefaultLimit()
 * }
 * is called to determine the maximum number of documents retrieved. The
 * query.documentlimit is used to documentlimit the retrieved list.
 * <p/>
 * @author jeroen
 */
public class CollectorDocument extends Collector {

   public static Log log = new Log(CollectorDocument.class);
   private Document sortedDocs[];
   private Comparator<Document> documentcomparator;
   private TreeSet<Document> collectedDocs;
   private ArrayList<GraphNode> scorables;
   RetrievalModel retrievalmodel;
   ScoreFunction scorefunction;
   double lowestscore = Double.MAX_VALUE;
   int limit;

   public CollectorDocument() {
      super();
   }

   public CollectorDocument(RetrievalModel rm) {
      super(rm);
      this.retrievalmodel = rm;
   }

   @Override
   public void setStrategy(Strategy strategy) {
      super.setStrategy(strategy);
      retrievalmodel = (RetrievalModel) strategy;
      documentcomparator = retrievalmodel.query.getDocumentComparator();
      collectedDocs = new TreeSet<Document>(documentcomparator);
   }

   @Override
   public int hashCode() {
      return (strategy == null)?0:strategy.query.id;
   }

   @Override
   public boolean equals(Object r) {
      if (r instanceof CollectorDocument) {
         CollectorDocument rr = (CollectorDocument) r;
         return strategy.query.id == rr.strategy.query.id;
      }
      return false;
   }

   @Override
   public Collection<String> getReducerIDs() {
      return strategy.query.getReducerID();
   }

   @Override
   public String getReducerName() {
      return Integer.toString(strategy.query.id);
   }

   @Override
   public void prepareRetrieval() {
      for (ReportedUnstoredFeature f : retrievalmodel.getFeatures().getReportedUnstoredFeatures()) {
         f.prepareRetrieval(strategy);
      }
      scorables = retrievalmodel.root.getAnnounce(ANNOUNCEKEY.SCORABLE);
      HashSet<GraphNode> list = new HashSet<GraphNode>();
      for (GraphNode n : scorables) {
         while (n.parent != retrievalmodel.root) {
            n = (GraphNode) n.parent;
         }
         list.add(n);
      }
      for (GraphNode n : list) {
         n.doPrepareRetrieval();
      }
      scorefunction = ScoreFunction.create(retrievalmodel.root);
      scorefunction.prepareRetrieval(retrievalmodel, retrievalmodel.root.getAnnounce(ANNOUNCEKEY.SCORABLE));
   }

   @Override
   public boolean reduceInQuery() {
      return true;
   }

   public String getKey() {
      return Integer.toString(strategy.query.id);
   }

   public void setCollectedResults() {
   }

   @Override
   public void prepareAggregation() {
      super.prepareAggregation();
      containedfeatures = retrievalmodel.root.containedfeatures;
      this.limit = retrievalmodel.query.documentlimit;
   }

   @Override
   public void collectDocument(Document doc) {
      //log.s("collectDocument");
      doc.score += scorefunction.score(doc);
      if (doc.score != 0) {
         addDocumentRetrieval(doc);
      }
      //log.e("collectDocument");
   }

   public void reuse() {
      collectedDocs = new TreeSet<Document>(documentcomparator);
      sortedDocs = new Document[0];
   }

   /**
    * Add a document during document processing. The ReportedUnstoredFeatures
    * are reported.
    *
    * @param doc
    */
   protected void addDocumentRetrieval(Document doc) {
      if (collectedDocs.size() < limit) {
         collectedDocs.add(doc);
         for (ReportedUnstoredFeature f : retrievalmodel.getFeatures().getReportedUnstoredFeatures()) {
            f.report(doc);
         }
         if (doc.score < lowestscore) {
            lowestscore = doc.score;
         }
      } else if (doc.score > lowestscore) {
         collectedDocs.pollLast();
         collectedDocs.add(doc);
         lowestscore = collectedDocs.last().score;
         for (ReportedUnstoredFeature f : retrievalmodel.getFeatures().getReportedUnstoredFeatures()) {
            f.report(doc);
         }
      }
   }

   @Override
   public void writeValue(StructureWriter rw) {
      rw.write(collectedDocs.size());
      for (Document d : collectedDocs) {
         d.write(rw);
      }
   }

   @Override
   public void readValue(StructureReader reader) {
      try {
         int doccount = reader.readInt();
         collectedDocs = null;
         sortedDocs = new Document[doccount];
         for (int d = 0; d < doccount; d++) {
            Document doc = new Document();
            doc.read(reader);
            sortedDocs[d] = doc;
         }
      } catch (EOFException ex) {
         log.fatalexception(ex, "read( %s )", reader);
      }
   }

   public Document[] getRetrievedDocs() {
      return sortedDocs;
   }

   @Override
   public void finishSegmentRetrieval() {
      sortedDocs = collectedDocs.toArray(new Document[collectedDocs.size()]);
   }

   @Override
   public void postLoadFeatures(int partition) {
      if (!retrievalmodel.root.needsPrePass()) {
         retriever.readReportedStoredFeatures(collectedDocs, retrievalmodel.getFeatures().getReportedStoredFeatures(), partition);
      }
   }

   @Override
   public void aggregate(Collector subcollector) {
      CollectorDocument cd = (CollectorDocument) subcollector;
      if (this.sortedDocs == null || sortedDocs.length == 0) {
         if (cd.sortedDocs != null) {
            sortedDocs = cd.sortedDocs;
            for (Document d : sortedDocs) {
               d.setRetrievalModel(retrievalmodel);
            }
         }
      } else if (cd.sortedDocs != null && cd.sortedDocs.length > 0) {
         Document newlist[] = new Document[Math.min(sortedDocs.length + cd.sortedDocs.length,
                 retrievalmodel.query.documentlimit)];
         int p2 = 0, p3 = 0;
         for (int p1 = 0; p1 < newlist.length; p1++) {
            if (documentcomparator.compare(sortedDocs[p2], cd.sortedDocs[p3]) < 0) {
               newlist[p1] = sortedDocs[p2];
               if (++p2 == sortedDocs.length) {
                  for (p1++; p1 < newlist.length; p1++) {
                     newlist[p1] = cd.sortedDocs[p3++];
                     newlist[p1].setRetrievalModel(retrievalmodel);
                  }
               }
            } else {
               newlist[p1] = cd.sortedDocs[p3];
               newlist[p1].setRetrievalModel(retrievalmodel);
               if (++p3 == cd.sortedDocs.length) {
                  for (p1++; p1 < newlist.length; p1++) {
                     newlist[p1] = sortedDocs[p2++];
                  }
               }
            }
         }
         sortedDocs = newlist;
      }
   }

   @Override
   public void decode() {
      for (Document doc : sortedDocs) {
         doc.setRetrievalModel(retrievalmodel);
         doc.decode();
      }
   }

   @Override
   public void writeID(StructureWriter writer) {
      writer.write(strategy.query.id);
   }

   @Override
   public void writeKey(StructureWriter writer) {
   }

   @Override
   public void readKey(StructureReader reader) throws EOFException {
   }

   @Override
   public void readID(StructureReader reader) throws EOFException {
      reader.readInt();
   }
}
