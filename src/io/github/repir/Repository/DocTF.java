package io.github.repir.Repository;

import io.github.repir.Retriever.Document;
import io.github.repir.tools.Content.Datafile;
import io.github.repir.tools.Content.StructuredFile;
import io.github.repir.tools.Content.StructuredFileIntID;
import io.github.repir.EntityReader.MapReduce.TermEntityKey;
import io.github.repir.EntityReader.MapReduce.TermEntityValue;
import io.github.repir.EntityReader.Entity;
import io.github.repir.tools.Lib.Log;
import io.github.repir.Repository.DocTF.File;
import io.github.repir.tools.Content.BufferReaderWriter;
import io.github.repir.tools.Content.EOCException;

/**
 * Stores the number of tokens in a Document as an Integer.
 * @see EntityStoredFeature
 * @author jer
 */
public class DocTF 
   extends EntityStoredFeature<File, Integer> 
   implements ReducibleFeature, ReportableFeature<Integer>, ResidentFeature  {

   public static Log log = new Log(DocTF.class);

   protected DocTF(Repository repository, String field) {
      super(repository, field);
   }

   @Override
   public void mapOutput(TermEntityValue value, Entity doc) {
      value.writer.write(doc.get(entityAttribute()).size());
   }

   @Override
   public void reduceInput(TermEntityKey key, Iterable<TermEntityValue> values) {
      try {
         file.dtf.write(values.iterator().next().reader.readInt());
      } catch (EOCException ex) {
         log.fatal(ex);
      }
   }

   @Override
   public void encode(Document d, int reportid) {
      bdw.write((Integer) d.getReportedFeature(reportid));
      d.setReportedFeature(reportid, bdw.getBytes());
   }

   @Override
   public void decode(Document d, int reportid) {
      reader.setBuffer((byte[]) d.getReportedFeature(reportid));
      try {
         d.setReportedFeature(reportid, reader.readInt());
      } catch (EOCException ex) {
         log.fatalexception(ex, "decode( %s ) reader %s", d, reader);
      }
   }

   @Override
   public void report(Document doc, int reportid) {
      //log.info("report %s doc %d reportid %d value %s", this.getCanonicalName(), doc.docid, reportid, getValue());
      doc.setReportedFeature(reportid, getValue());
   }

   @Override
   public Integer valueReported(Document doc, int docid) {
      return (Integer) doc.getReportedFeature(docid);
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
      } catch (EOCException ex) {
         log.fatalexception(ex, "readResident()");
      }
   }
   
   public boolean isReadResident() {
      return getFile().isresident;
   }

   public static class File extends StructuredFile implements StructuredFileIntID {

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
         } catch (EOCException ex) {
            log.exception(ex, "read( %d ) dtf %s", id, dtf);
         }
      }

      @Override
      public void find(int id) {
         this.setOffset(id * 3);
      }

      @Override
      public void readResident(int id) throws EOCException {
         readResident();
      }
      
      @Override
      public void readResident() throws EOCException {
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
