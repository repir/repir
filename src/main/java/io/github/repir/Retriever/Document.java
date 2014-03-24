package io.github.repir.Retriever;

import io.github.repir.tools.Content.BufferDelayedWriter;
import io.github.repir.tools.Content.StructureReader;
import io.github.repir.tools.Content.StructureWriter;
import io.github.repir.tools.Lib.Log;
import io.github.repir.Repository.ReportableFeature;
import io.github.repir.Strategy.RetrievalModel;
import io.github.repir.tools.Content.BufferSerializable;
import io.github.repir.tools.Content.EOCException;

/**
 * Data class to contain values of retrieved documents. A Repository is assumed
 * to consist of entities that can be analyzed, we use Document to represent 
 * these entities, but the same generic mechanism can probably be used for other
 * types of entities, e.g. images, videos. That stored remains bound to these
 * Documents, during a retrieval task Documents can be retrieved with the 
 * feature values it is associated with, e.g. title, url, collectionid, size. 
 * <p/>
 * Documents are identified internally buy a unique (docid, partition), where document
 * is a sequence that is unique only within a partition. The Document itself
 * is more a data class without logic, its feature values are managed by the 
 * features.
 */
public class Document implements BufferSerializable {

   public static Log log = new Log(Document.class);
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
         reportdata = new Object[retrievalmodel.getReportedFeaturesMap().size()];
   }

   public void decode() {
      for (ReportedFeature<ReportableFeature> f : retrievalmodel.getReportableFeatures()) {
         f.feature.decode(this, f.reportID);
      }
   }

   public void setReportedFeature(int f, Object data) {
      reportdata[f] = data;
   }

   public Object getReportedFeature(int f) {
      return reportdata[f];
   }

   private Object getReportedFeature(ReportedFeature f) {
      return reportdata[f.reportID];
   }

   public String getString(ReportedFeature feature) {
      return (String)getReportedFeature(feature);
   }

   public String getString(ReportableFeature feature) {
      return (String)getReportedFeature(retrievalmodel.getReportID(feature));
   }

   public int getInt(ReportedFeature feature) {
      return (Integer)getReportedFeature(feature);
   }

   public int getInt(ReportableFeature feature) {
      return (Integer)getReportedFeature(retrievalmodel.getReportID(feature));
   }

   public double getDouble(ReportedFeature feature) {
      return (Double)getReportedFeature(feature);
   }

   public int[] getIntArray(ReportedFeature feature) {
      return (int[])getReportedFeature(feature);
   }

   public int[] getIntArray(ReportableFeature feature) {
      return (int[])getReportedFeature(retrievalmodel.getReportID(feature));
   }
   
   @Override
   public void write(StructureWriter writer) {
      writer.write(partition);
      writer.write(docid);
      writer.write(score);
      writer.write(reportdata.length);
      for (ReportedFeature c : retrievalmodel.getReportableFeatures()) {
         c.feature.encode(this, c.reportID);
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
      } catch (EOCException ex) {
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
