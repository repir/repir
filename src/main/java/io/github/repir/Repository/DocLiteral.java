package io.github.repir.Repository;

import io.github.repir.Retriever.Document;
import io.github.repir.tools.Content.Datafile;
import io.github.repir.tools.Structure.StructuredFileShortJumpTable;
import io.github.repir.tools.Structure.StructuredDataStream;
import io.github.repir.EntityReader.MapReduce.TermEntityKey;
import io.github.repir.EntityReader.MapReduce.TermEntityValue;
import io.github.repir.tools.Lib.Log;
import io.github.repir.Extractor.Entity;
import io.github.repir.Repository.DocLiteral.File;
import io.github.repir.tools.Content.EOCException;

/**
 * Can store one literal String per Document, e.g. collection ID, title, url.
 * @see EntityStoredFeature
 * @author jer
 */
public class DocLiteral
   extends EntityStoredFeature<File, String> 
   implements ReduciblePartitionedFeature, ReportableFeature<String>, ResidentFeature  {

   public static Log log = new Log(DocLiteral.class);

   protected DocLiteral(Repository repository, String field) {
      super(repository, field);
   }

   public static DocLiteral get(Repository repository, String field) {
       String label = canonicalName(DocLiteral.class, field);
       DocLiteral termid = (DocLiteral)repository.getStoredFeature(label);
       if (termid == null) {
          termid = new DocLiteral(repository, field);
          repository.storeFeature(label, termid);
       }
       return termid;
   }
   
   @Override
   public void setMapOutputValue(TermEntityValue value, Entity entity) {
      value.writer.write(extract(entity));
   }

   @Override
   public void writeReduce(TermEntityKey key, Iterable<TermEntityValue> values) {
      try {
         TermEntityValue value = values.iterator().next();
         String literal = value.reader.readString();
         file.literal.write(literal);
      } catch (EOCException ex) {
         log.exception(ex, "reduceInput( %s, %s ) file %s", key, values, file);
      }
   }

   @Override
   public void encode(Document d, int reportid) {
      //log.info("encode %s doc %d reportid %d value %s", this.getCanonicalName(), d.docid, reportid, d.getReportedFeature(reportid));
      String literal = (String) d.getReportedFeature(reportid);
      bdw.write(literal);
      d.setReportedFeature(reportid, bdw.getBytes());
   }

   @Override
   public void decode(Document d, int reportid) {
      reader.setBuffer((byte[]) d.getReportedFeature(reportid));
      d.setReportedFeature(reportid, reader.readString());
   }

   @Override
   public void report(Document doc, int reportid) {
      //log.info("report %s doc %d reportid %d value %s", this.getCanonicalName(), doc.docid, reportid, getValue());
      doc.setReportedFeature(reportid, getValue());
   }

   @Override
   public String valueReported(Document doc, int reportid) {
      return (String) doc.getReportedFeature(reportid);
   }
   
   @Override
   public String getValue() {
      return file.literal.value;
   }

   @Override
   public void write(String value) {
      file.literal.write(value);
   }

   @Override
   public File createFile(Datafile datafile) {
      return new File(datafile);
   }

   /**
    * Finds the internal document id for a literal. This search reads the whole
    * feature sequentially, as there is no direct access to finding literal
    * values.
    * <p/>
    * @param literal
    * @return
    */
   public int findLiteral(String literal) {
      getFile().openRead();
      file.setBufferSize(4096 * 25000);
      file.setOffset(0);
      int id = 0;
         while (file.nextRecord()) {
            if (literal.equals(file.literal.value)) {
               return id;
            }
            id++;
         }
      file.setBufferSize(4096);
      return -1;
   }

   @Override
   public void setValue(String value) {
      getFile().literal.value = value;
   }

   @Override
   public void readResident() {
      getFile().loadMem();
   }

   @Override
   public boolean isReadResident() {
      return getFile().isLoadedInMem();
   }
   
   public static class File extends StructuredFileShortJumpTable {

      public StringField literal = this.addString("directterm");

      public File(Datafile df) {
         super(df);
      }
   }
}
