package io.github.repir.Retriever;

import io.github.repir.tools.Content.BufferDelayedWriter;
import io.github.repir.tools.Content.StructureReader;
import io.github.repir.tools.Content.StructureWriter;
import io.github.repir.tools.Lib.Log;
import io.github.repir.Repository.DocForward;
import io.github.repir.Repository.DocLiteral;
import io.github.repir.Repository.DocTF;
import io.github.repir.Repository.ReportableFeature;
import io.github.repir.Strategy.Strategy;
import java.io.EOFException;
import io.github.repir.Strategy.RetrievalModel;

/**
 * Data class to contain values of retrieved documents. These object are to be
 * persistently stored in a metadatafile that contains literal string
 * storedfeatures and statistics and a directfile that contains positional
 * storedfeatures.
 */
public class Document implements BufferDelayedWriter.Serialize {

   public static Log log = new Log(Document.class);
   public static DocTF doctf;
   public static DocForward docforward;
   public RetrievalModel retrievalmodel;
   /**
    * partition/partition where the document is stored
    */
   public int partition;
   /**
    * internal document id, unique per partition
    */
   public int docid;
   /**
    * the literal storedfeatures of the documents literal[ literalchannel ] e.g.
    * literal[0] is the collection ID, and other common literals are URL and
    * title
    */
   /**
    * term frequency of the document (i.e. length in terms)
    */
   public int tf = -1;
   /**
    * Score assigned to the document for ranking
    */
   public double score;
   /**
    * For debug purposes, this is often used to mapOutput each storedfeatures
    * attribution to the score this is not communicated through the MapReduce
    * framework
    */
   public StringBuilder report;
   public Object reportdata[];
   private String collectionID;

   public Document() {
   }

   public Document(RetrievalModel rm, int docid, int partition) {
      setRetrievalModel(rm);
      this.docid = docid;
      this.partition = partition;
   }

   public void setRetrievalModel(RetrievalModel rm) {
      this.retrievalmodel = rm;
      if (reportdata == null)
         reportdata = new Object[retrievalmodel.getFeatures().getReportableFeatures().size()];
   }

   public void decode() {
      for (ReportableFeature f : retrievalmodel.getFeatures().getReportableFeatures()) {
         f.decode(this);
      }
   }

   public void setReportedFeature(int f, Object data) {
      reportdata[f] = data;
   }

   public Object getReportedFeature(int f) {
      return reportdata[f];
   }

   public Object getReportedFeature(String name) {
      return reportdata[retrievalmodel.getFeatures().getReportedFeature(name).getReportID()];
   }

   public String getCollectionID() {
      if (collectionID == null) {
         DocLiteral f = (DocLiteral)retrievalmodel.getFeatures().getReportedFeature("DocLiteral:collectionid");
         collectionID = (String)reportdata[ f.getReportID() ];
      }
      return collectionID;
   }

   public String getLiteral(String name) {
      return retrievalmodel.getFeatures().getLiteral(name).valueReported(this);
   }

   public String getLiteral(DocLiteral feature) {
      return feature.valueReported(this);
   }

   public int[] getReportedForward() {
      return (int[])this.getReportedFeature("DocForward:all");
   }

   public int[] getForward() {
      if (docforward == null)
         docforward = (DocForward) retrievalmodel.repository.getFeature("DocForward:all");
      docforward.read(this);
      return docforward.getValue();
   }

   public int getTF() {
      if (tf < 0) {
         if (doctf == null)
            doctf = (DocTF)retrievalmodel.repository.getFeature("DocTF:all");
         doctf.read(this);
         tf = doctf.getValue();
      }
      return tf;
   }
   
   @Override
   public void write(StructureWriter writer) {
      writer.write(partition);
      writer.write(docid);
      writer.write(score);
      writer.write(reportdata.length);
      //log.info("WRITE %d %d %s", docid, partition, reportdata.length);
      for (ReportableFeature c : retrievalmodel.getFeatures().getReportableFeatures()) {
         //log.info("mapOutput doc %d literal %s", docid, (String)reportdata[c.getReportID()]);
         c.encode(this);
      }
      for (int i = 0; i < reportdata.length; i++) {
         writer.writeByteBlock((byte[]) reportdata[ i]);
      }
      writer.write(report);
   }

   @Override
   public void read(StructureReader reader) {
      try {
         partition = reader.readInt();
         docid = reader.readInt();
         score = reader.readDouble();
         int featurescount = reader.readInt();
         reportdata = new Object[featurescount];
         for (int i = 0; i < featurescount; i++) {
            reportdata[i] = reader.readByteBlock();
         }
         report = reader.readStringBuilder();
      } catch (EOFException ex) {
         log.exception(ex, "read( %s )", reader);
      }
   }

   /**
    * For debug purposes: formats using
    * {@link Lib.PrintTools#sprintf(java.lang.String, java.lang.Object[])} and
    * adds it to the document report
    * <p/>
    * @param s template
    * @param o parameters
    */
   public void addReport(String s, Object... o) {
      if (report == null) {
         report = new StringBuilder();
      }
      report.append(io.github.repir.tools.Lib.PrintTools.sprintf(s, o));
   }
}
