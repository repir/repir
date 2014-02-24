package io.github.repir.Repository;

import io.github.repir.Retriever.Document;
import io.github.repir.tools.Content.Datafile;
import io.github.repir.tools.Content.RecordJump2Array;
import io.github.repir.tools.Content.StructuredDataStream;
import io.github.repir.EntityReader.TermEntityKey;
import io.github.repir.EntityReader.TermEntityValue;
import io.github.repir.tools.Lib.Log;
import io.github.repir.Extractor.Entity;
import io.github.repir.Repository.DocLiteral.File;
import java.io.EOFException;

public class DocLiteral extends EntityStoredFeature<File, String> implements ReducableFeature, ReportableFeature<String>  {

   public static Log log = new Log(DocLiteral.class);

   protected DocLiteral(Repository repository, String field) {
      super(repository, field);
   }

   @Override
   public void mapOutput(TermEntityValue value, Entity entity) {
      value.writer.write(extract(entity));
   }

   public String extract(Entity entity) {
      return entity.get(getField()).getContentStr();
   }

   @Override
   public void reduceInput(TermEntityKey key, Iterable<TermEntityValue> values) {
      try {
         TermEntityValue value = values.iterator().next();
         String literal = value.reader.readString();
         file.literal.write(literal);
      } catch (EOFException ex) {
         log.exception(ex, "reduceInput( %s, %s ) file %s", key, values, file);
      }
   }

   @Override
   public void encode(Document d) {
      //log.info("encode %s doc %d reportid %d value %s", this.getCanonicalName(), d.docid, reportid, d.getReportedFeature(reportid));
      String literal = (String) d.getReportedFeature(reportid);
      bdw.write(literal);
      d.setReportedFeature(reportid, bdw.getBytes());
   }

   @Override
   public void decode(Document d) {
      reader.setBuffer((byte[]) d.getReportedFeature(reportid));
      try {
         d.setReportedFeature(reportid, reader.readString());
      } catch (EOFException ex) {
         log.fatalexception(ex, "decode( %s ) reader %s", d, reader);
      }
   }

   @Override
   public void report(Document doc) {
      //log.info("report %s doc %d reportid %d value %s", this.getCanonicalName(), doc.docid, reportid, getValue());
      doc.setReportedFeature(reportid, getValue());
   }

   @Override
   public String valueReported(Document doc) {
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
         while (file.next()) {
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
   
   public static class File extends RecordJump2Array {

      public StringField literal = this.addString("directterm");

      public File(Datafile df) {
         super(df);
      }
   }
}
