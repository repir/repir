package io.github.repir.Repository;

import io.github.repir.Retriever.Document;
import io.github.repir.tools.Content.Datafile;
import io.github.repir.tools.Content.StructuredFileSequential;
import io.github.repir.EntityReader.MapReduce.TermEntityKey;
import io.github.repir.EntityReader.MapReduce.TermEntityValue;
import io.github.repir.EntityReader.Entity;
import io.github.repir.Extractor.EntityChannel;
import io.github.repir.tools.Lib.Log;
import io.github.repir.Repository.DocForward.File;
import io.github.repir.tools.Content.EOCException;

/**
 * A forward index, that store all tokens contained in a document in the collection.
 * Per document, the tokens are stored as a simple array of ints, each int being 
 * a TermID, and the order of the tokens is the order in which they appear in the 
 * original document. 
 * @see EntityStoredFeature
 * @author jer
 */
public class DocForward extends EntityStoredFeature<File, int[]> implements ReducibleFeature  {

   public static Log log = new Log(DocForward.class);

   protected DocForward(Repository repository, String field) {
      super(repository, field);
   }

   @Override
   public void mapOutput(TermEntityValue value, Entity doc) {
      EntityChannel attr = doc.get(getField());
      if (attr.tokenized == null) {
         attr.tokenized = repository.tokenize(attr);
      }
      value.writer.writeC(attr.tokenized);
   }

   @Override
   public void reduceInput(TermEntityKey key, Iterable<TermEntityValue> values) {
      try {
         TermEntityValue value = values.iterator().next();
         int t[] = value.reader.readCIntArray();
         write(t);
      } catch (EOCException ex) {
         log.fatal(ex);
      }
   }

   @Override
   public void encode(Document d, int reportid) {
      int forward[] = (int[]) d.getReportedFeature(reportid);
      bdw.writeC(forward);
      d.setReportedFeature(reportid, bdw.getBytes());
   }

   @Override
   public void decode(Document d, int reportid) {
      reader.setBuffer((byte[]) d.getReportedFeature(reportid));
      try {
         d.setReportedFeature(reportid, reader.readCIntArray());
      } catch (EOCException ex) {
         log.fatalexception(ex, "decode( %s ) reader %s reportid %d", d, reader, reportid);
      }
   }

   @Override
   public void report(Document doc, int reportid) {
      //log.info("report %s doc %d reportid %d value %s", this.getCanonicalName(), doc.docid, reportid, getValue());
      doc.setReportedFeature(reportid, getValue());
   }

   @Override
   public int[] valueReported(Document doc, int reportid) {
      return (int[]) doc.getReportedFeature(reportid);
   }   
   
   @Override
   public void write(int[] value) {
      file.tokens.write(value);
   }

   @Override
   public int[] getValue() {
      return file.tokens.value;
   }

   @Override
   public File createFile(Datafile datafile) {
      return new File(datafile);
   }

   @Override
   public void setValue(int[] value) {
      getFile().tokens.value = value;
   }

   @Override
   public void readResident() {
      throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
   }

   @Override
   public boolean isReadResident() {
      return false;
   }

   public static class File extends StructuredFileSequential {

      public CIntArrayField tokens = this.addCIntArray("tokens");

      public File(Datafile df) {
         super(df);
      }
   }
}
