package io.github.repir.Repository;

import io.github.repir.tools.Content.Datafile;
import io.github.repir.tools.Content.RecordSequentialArray;
import io.github.repir.Extractor.Entity;
import io.github.repir.EntityReader.TermEntityKey;
import io.github.repir.EntityReader.TermEntityValue;
import io.github.repir.Repository.TermInverted.File;
import io.github.repir.Retriever.Document;
import io.github.repir.tools.Lib.Log;
import java.io.EOFException;
import java.util.ArrayList;

public class TermInverted extends AutoTermDocumentFeature<File, int[]> implements ReducableFeature, ReportableFeature<int[]> {

   public static Log log = new Log(TermInverted.class);
   static final int ZEROPOS[] = new int[0];
   DocLiteral collectionid;

   protected TermInverted(Repository repository, String field) {
      super(repository, field);
      collectionid = repository.getCollectionIDFeature();
   }

   @Override
   public void writeMapOutput(TermEntityValue value, Entity doc, ArrayList<Integer> pos) {
      value.writer.write0Str(collectionid.extract(doc));
      value.writer.writeIncr(pos);
   }

   @Override
   public void reduceInput(TermEntityKey key, Iterable<TermEntityValue> values) {
      for (; termid < key.termid; termid++) {
         file.setOffsetTupleStart(file.getOffetTupleEnd());
         file.recordEnd();
      }
      try {
         long offset = file.getOffset();
         for (TermEntityValue v : values) {
            String doc = v.reader.readString0();
            int docid = docs.get(doc);
            file.docid.write(docid);
            int pos[] = v.reader.readCIntIncr();
            file.data.write(pos);
         }
         file.setOffsetTupleStart(offset);
         file.recordEnd();
         termid++;
      } catch (EOFException ex) {
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
      if (file.next()) {
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
   public void decode(Document d) {
      reader.setBuffer( (byte[]) d.getReportedFeature(reportid) );
      try {
         d.setReportedFeature( reportid, reader.readCIntArray() );
      } catch (EOFException ex) {
         log.fatalexception(ex, "decode %d %d %d", d.docid, d.partition, d.getReportedFeature(reportid));
      }
   }

   @Override
   public void encode(Document d) {
      bdw.writeC( (int[]) d.getReportedFeature( reportid ) );
      d.setReportedFeature( reportid, bdw.getBytes() );
   }

   @Override
   public void report(Document doc) {
      doc.setReportedFeature(reportid, getValue(doc) );
   }

   @Override
   public int[] valueReported(Document doc) {
      return (int[]) doc.getReportedFeature(reportid);
   }

   public static class File extends RecordSequentialArray {

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
