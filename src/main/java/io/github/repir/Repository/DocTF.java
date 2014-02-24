package io.github.repir.Repository;

import io.github.repir.Retriever.Document;
import io.github.repir.tools.Content.Datafile;
import io.github.repir.tools.Content.RecordBinary;
import io.github.repir.tools.Content.RecordIdentity;
import io.github.repir.EntityReader.TermEntityKey;
import io.github.repir.EntityReader.TermEntityValue;
import io.github.repir.Extractor.Entity;
import io.github.repir.tools.Lib.Log;
import io.github.repir.Repository.DocTF.File;
import java.io.EOFException;
import java.util.logging.Level;
import java.util.logging.Logger;
import static io.github.repir.Repository.StoredReportableFeature.log;
import io.github.repir.tools.Content.BufferReaderWriter;

public class DocTF extends EntityStoredFeature<File, Integer> implements ReducableFeature, ReportableFeature<Integer>  {

   public static Log log = new Log(DocTF.class);

   protected DocTF(Repository repository, String field) {
      super(repository, field);
   }

   @Override
   public void mapOutput(TermEntityValue value, Entity doc) {
      value.writer.write(doc.get(getField()).size());
   }

   @Override
   public void reduceInput(TermEntityKey key, Iterable<TermEntityValue> values) {
      try {
         file.dtf.write(values.iterator().next().reader.readInt());
      } catch (EOFException ex) {
         log.fatal(ex);
      }
   }

   @Override
   public void encode(Document d) {
      bdw.write((Long) d.getReportedFeature(reportid));
      d.setReportedFeature(reportid, bdw.getBytes());
   }

   @Override
   public void decode(Document d) {
      reader.setBuffer((byte[]) d.getReportedFeature(reportid));
      try {
         d.setReportedFeature(reportid, reader.readLong());
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
   public Integer valueReported(Document doc) {
      return (Integer) doc.getReportedFeature(reportid);
   }   
   
   @Override
   public File createFile(Datafile datafile) {
      return new File(datafile);
   }

   @Override
   public Integer getValue() {
      return file.dtf.value;
   }

   @Override
   public void write(Integer value) {
      file.dtf.write(value);
   }

   @Override
   public void setValue(Integer value) {
      getFile().dtf.value = value;
   }

   public void readResident() {
      try {
         getFile().readResident(0);
      } catch (EOFException ex) {
         log.fatalexception(ex, "readResident()");
      }
   }
   
   public boolean isReadResident() {
      return getFile().isresident;
   }

   public static class File extends RecordBinary implements RecordIdentity {

      public Int3Field dtf = this.addInt3("dtf");
      public boolean isresident = false;

      public File(Datafile df) {
         super(df);
      }

      @Override
      public void read(int id) {
         reader.setOffset(id * 3);
         try {
            dtf.readNoReturn();
         } catch (EOFException ex) {
            log.exception(ex, "read( %d ) dtf %s", id, dtf);
         }
      }

      @Override
      public void find(int id) {
         this.setOffset(id * 3);
      }

      public void readResident(int id) throws EOFException {
         openRead();
         BufferReaderWriter w = new BufferReaderWriter(datafile.readFully());
         reader = w;
         isresident = true;
      }
      
      public boolean isReadResident() {
         return isresident;  
      }

      public void reset() {
         reader.reset();
      }
   }
}
