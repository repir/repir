package io.github.repir.Repository;

import io.github.repir.Retriever.Document;
import io.github.repir.tools.Content.Datafile;
import io.github.repir.tools.Structure.StructuredFileSequential;
import io.github.repir.EntityReader.MapReduce.TermEntityKey;
import io.github.repir.EntityReader.MapReduce.TermEntityValue;
import io.github.repir.tools.Extractor.Entity;
import io.github.repir.tools.Extractor.EntityChannel;
import io.github.repir.tools.Lib.Log;
import io.github.repir.Repository.DocForward.File;
import io.github.repir.tools.Content.EOCException;
import java.io.IOException;
import org.apache.hadoop.mapreduce.Mapper;

/**
 * A forward index, that store all tokens contained in a document in the collection.
 * Per document, the tokens are stored as a simple array of ints, each int being 
 * a TermID, and the order of the tokens is the order in which they appear in the 
 * original document. 
 * @see EntityStoredFeature
 * @author jer
 */
public class DocForward extends EntityStoredFeature<File, int[]> implements ReduciblePartitionedFeature  {

   public static Log log = new Log(DocForward.class);

   private DocForward(Repository repository, String field) {
      super(repository, field);
   }

   public static DocForward get(Repository repository, String field) {
       String label = canonicalName(DocForward.class, field);
       DocForward docforward = (DocForward)repository.getStoredFeature(label);
       if (docforward == null) {
          docforward = new DocForward(repository, field);
          repository.storeFeature(label, docforward);
       }
       return docforward;
   }
   
   @Override
   public void setMapOutputValue(TermEntityValue value, Entity doc) {
      EntityChannel attr = doc.get(entityAttribute());
      if (attr.tokenized == null) {
         attr.tokenized = repository.tokenize(attr);
      }
      value.writer.writeC(attr.tokenized);
   }

   @Override
   public void writeReduce(TermEntityKey key, Iterable<TermEntityValue> values) {
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

   public void readResident() {
      cacheResults();
      getFile().setBufferSize((int)this.getLength());
      openRead();
      while (this.next()) {
         cache.put(cache.size(), getValue());
      }
      closeRead();
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
   
   public long findOffset(int docid) {
       return getFile().findOffset(docid);
   }

   public void setOffset(int docid) {
       getFile().setOffset(getFile().findOffset(docid));
       getFile().openRead();
   }

   public static class File extends StructuredFileSequential {

      public CIntArrayField tokens = this.addCIntArray("tokens");

      public File(Datafile df) {
         super(df);
      }
   }
}
