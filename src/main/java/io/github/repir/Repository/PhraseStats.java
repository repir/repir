package io.github.repir.Repository;

import io.github.repir.Repository.PhraseStats.File;
import io.github.repir.Repository.PhraseStats.Record;
import io.github.repir.tools.Content.Datafile;
import io.github.repir.tools.Content.RecordHeaderData;
import io.github.repir.tools.Content.RecordHeaderDataRecord;
import io.github.repir.tools.Content.RecordHeaderInterface;
import io.github.repir.tools.Lib.ArrayTools;
import io.github.repir.tools.Lib.Log;
import io.github.repir.tools.Lib.MathTools;
import io.github.repir.tools.Lib.PrintTools;

public class PhraseStats extends StoredDynamicFeature<File, Record> {

   public static Log log = new Log(PhraseStats.class);

   protected PhraseStats(Repository repository) {
      super(repository);
   }

   @Override
   public File createFile(Datafile df) {
      return new File(df);
   }

   public class File extends RecordHeaderData<Record> {

      public CIntArrayField terms = this.addCIntArray("terms");
      public CIntField span = this.addCInt("span");
      public BoolField ordered = this.addBoolean("ordered");
      public CLongField cf = this.addCLong("cf");
      public CIntField df = this.addCInt("df");

      public File(Datafile df) {
         super(df);
      }

      @Override
      public Record newRecord() {
         return new Record();
      }
   }

   public class Record implements RecordHeaderDataRecord<File> {

      public int terms[];
      public int span;
      public boolean ordered;
      public long cf;
      public int df;

      public String toString() {
         return PrintTools.sprintf("bucketindex=%d terms=%s ordered=%b span=%d cf=%d df=%d", this.hashCode(), ArrayTools.toString(terms), ordered, span, cf, df);
      }

      @Override
      public int hashCode() {
         return MathTools.finishHash(MathTools.combineHash(MathTools.combineHash(31, ordered ? 1 : 0, span), terms));
      }

      @Override
      public boolean equals(Object r) {
         if (r instanceof Record) {
            Record record = (Record) r;
            if (!ArrayTools.equals(terms, record.terms) || span != record.span || ordered != record.ordered) {
               return false;
            }
            return true;
         }
         return false;
      }

      @Override
      public void write(File file) {
         file.terms.write(terms);
         file.span.write(span);
         file.ordered.write(ordered);
         file.cf.write(cf);
         file.df.write(df);
      }

      @Override
      public void read(File file) {
         terms = file.terms.value;
         span = file.span.value;
         ordered = file.ordered.value;
         cf = file.cf.value;
         df = file.df.value;
      }

      public void convert(RecordHeaderDataRecord record) {
         Record r = (Record)record;
         r.terms = terms;
         r.span = span;
         r.ordered = ordered;
         r.cf = cf;
         r.df = df;
      }
   }
}
