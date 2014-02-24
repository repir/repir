package io.github.repir.Repository;

import io.github.repir.Repository.SynStats.File;
import io.github.repir.Repository.SynStats.Record;
import io.github.repir.tools.Content.Datafile;
import io.github.repir.tools.Content.RecordHeaderData;
import io.github.repir.tools.Content.RecordHeaderDataRecord;
import io.github.repir.tools.Lib.ArrayTools;
import io.github.repir.tools.Lib.Log;
import io.github.repir.tools.Lib.MathTools;
import io.github.repir.tools.Lib.PrintTools;

public class SynStats extends StoredDynamicFeature<File, Record> {

   public static Log log = new Log(SynStats.class);

   protected SynStats(Repository repository) {
      super(repository);
   }

   @Override
   public File createFile(Datafile df) {
      return new File(df);
   }

   public class File extends RecordHeaderData<Record> {
      public StringField syn = this.addString("syn");
      public CLongField cf = this.addCLong("cf");
      public CLongField df = this.addCLong("df");

      public File(Datafile df) {
         super(df);
      }

      @Override
      public Record newRecord() {
         return new Record();
      }
   }

   public class Record implements RecordHeaderDataRecord<File> {
      public String syn;
      public long cf;
      public long df;

      public String toString() {
         return PrintTools.sprintf("bucketindex=%d terms=%s cf=%d df=%d", this.hashCode(), syn, cf, df);
      }
      
      @Override
      public int hashCode() {
         return MathTools.finishHash(MathTools.combineHash(31, syn.hashCode()));
      }

      @Override
      public boolean equals(Object r) {
         if (r instanceof Record) {
            Record record = (Record)r;
            return syn.equals( record.syn );  
         }
         return false;
      }

      @Override
      public void write(File file) {
         file.syn.write(syn);
         file.cf.write(cf);
         file.df.write(df);
      }

      @Override
      public void read(File file) {
         syn = file.syn.value;
         cf = file.cf.value;
         df = file.df.value;
      }

      public void convert(RecordHeaderDataRecord record) {
         Record r = (Record)record;
         r.syn = syn;
         r.cf = cf;
         r.df = df;
      }
   }
}
