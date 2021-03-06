package io.github.repir.Repository;

import io.github.htools.io.Datafile;
import io.github.htools.io.struct.StructuredFileSequential;
import io.github.htools.extract.Content;
import io.github.htools.hadoop.io.archivereader.RecordKey;
import io.github.htools.hadoop.io.archivereader.RecordValue;
import io.github.repir.Repository.TermInverted.File;
import io.github.repir.Retriever.Document;
import io.github.htools.io.EOCException;
import io.github.htools.lib.Log;
import java.util.ArrayList;

/**
 * A stored feature that uses a term-document structure similar to a textbook inverted index. This 
 * data structure is best used for sparse data that is to be accessed by term, which gives an ordered
 * list of the documents in which the term appears. The base class can be extended to define the
 * exact data that needs to be stored, such as the term frequency or the list of positions of the
 * term in the document. 
 * @author jeroen
 * @param <F>
 * @param <C> 
 */
public class TermInverted extends AutoTermDocumentFeature<File, int[]> {

   public static Log log = new Log(TermInverted.class);
   static final int ZEROPOS[] = new int[0];
   DocLiteral collectionid;

   private TermInverted(Repository repository, String field) {
      super(repository, field);
      collectionid = repository.getCollectionIDFeature();
   }

   private TermInverted(Repository repository, String field, Term term) {
      super(repository, (String)field);
      collectionid = repository.getCollectionIDFeature();
      this.term = term;
   }

   public static TermInverted get(Repository repository, String field) {
       String label = canonicalName(TermInverted.class, field);
       TermInverted termid = (TermInverted)repository.getStoredFeature(label);
       if (termid == null) {
          termid = new TermInverted(repository, field);
          repository.storeFeature(label, termid);
       }
       return termid;
   }
   
   public static TermInverted get(Repository repository, String field, Term term) {
       String label = canonicalName(TermInverted.class, field, term.getProcessedTerm());
       TermInverted termid = (TermInverted)repository.getStoredFeature(label);
       if (termid == null) {
          termid = new TermInverted(repository, field, term);
          repository.storeFeature(label, termid);
       }
       return termid;
   }
   
   @Override
   public void setMapOutputValue(RecordValue value, String docname, ArrayList<Integer> pos) {
      value.writer.write0(docname);
      value.writer.writeIncr(pos);
   }

   @Override
   public void reduceInput(RecordKey key, Iterable<RecordValue> values) {
      for (; reducetermid < key.termid; reducetermid++) {
         file.setOffsetTupleStart(file.getOffetTupleEnd());
         file.recordEnd();
      }
      try {
         long offset = file.getOffset();
         for (RecordValue v : values) {
            String doc = v.reader.readString0();
            int docid = docs.get(doc);
            file.docid.write(docid);
            int pos[] = v.reader.readCIntIncr();
            file.data.write(pos);
         }
         file.setOffsetTupleStart(offset);
         file.recordEnd();
         reducetermid++;
      } catch (EOCException ex) {
         log.exception(ex, "ReduceInput");
      }
   }

   @Override
   public int[] getValue(Document doc) {
      if (doc.docid == docid) {
         return file.data.value;
      } else {
         return ZEROPOS;
      }
   }

   @Override
   protected int readNextID() {
      //log.info("readNextID() reader %s offset %d ceiling %d", file.reader, file.getOffset(), file.getCeiling());
      if (file.nextRecord()) {
         return file.docid.value;
      } else {
         file.data.value = ZEROPOS;
      }
      return -1;
   }

   @Override
   public File createFile(Datafile datafile) {
      return new File(datafile);
   }

   @Override
   public void decode(Document d, int reportid) {
      reader.setBuffer((byte[]) d.getReportedFeature(reportid));
      try {
         d.setReportedFeature( reportid, reader.readCIntArray() );
      } catch (EOCException ex) {
         log.fatalexception(ex, "decode %d %d %d", d.docid, d.partition, d.getReportedFeature(reportid));
      }
   }

   @Override
   public void encode(Document d, int reportid) {
      bdw.writeC( (int[]) d.getReportedFeature( reportid ) );
      d.setReportedFeature( reportid, bdw.getBytes() );
   }

   @Override
   public void report(Document doc, int reportid) {
      doc.setReportedFeature(reportid, getValue(doc) );
   }

   @Override
   public int[] valueReported(Document doc, int reportid) {
      return (int[]) doc.getReportedFeature(reportid);
   }

   public static class File extends StructuredFileSequential {

      public IntField docid = this.addInt("docid");
      public CIntIncrField data = this.addCIntIncr("pos");

      public File(Datafile df) {
         super(df);
      }

      @Override
      public void hookRecordWritten() {
         // record doesn't end until we say so
      }

      public void recordEnd() {
         super.hookRecordWritten();
      }
   }
}
