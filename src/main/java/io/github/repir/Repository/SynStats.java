package io.github.repir.Repository;

import io.github.repir.Repository.SynStats.File;
import io.github.repir.Repository.SynStats.Record;
import io.github.repir.tools.io.Datafile;
import io.github.repir.tools.io.struct.StructuredFileKeyValue;
import io.github.repir.tools.io.struct.StructuredFileKeyValueRecord;
import io.github.repir.tools.lib.Log;
import io.github.repir.tools.lib.MathTools;
import io.github.repir.tools.lib.PrintTools;

/**
 * This feature caches statistics for synonym sets (collection frequency and document 
 * frequency) that were retrieved, to speedup retrieval when the same synonym
 * statistics are needed again. This is used by the default FeatureSynonym operator. 
 * They first look if the synonym statistics are already available, if not a 
 * Collector is used to fetch these in a pre-pass
 * which are automatically stored for reuse. 
 * <p/>
 * Note that in contrast to ProximityStats, this feature will also store complex 
 * synonyms that operate for instance on other proximity operators. For this,
 * the query-set is sorted and stored as a query string.
 * @author jer
 */
public class SynStats extends StoredDynamicFeature<File, Record> {

   public static Log log = new Log(SynStats.class);

   private SynStats(Repository repository) {
      super(repository);
   }

   public static SynStats get(Repository repository) {
       String label = canonicalName(SynStats.class);
       SynStats termid = (SynStats)repository.getStoredFeature(label);
       if (termid == null) {
          termid = new SynStats(repository);
          repository.storeFeature(label, termid);
       }
       return termid;
   }
   
   @Override
   public File createFile(Datafile df) {
      return new File(df);
   }

   public class File extends StructuredFileKeyValue<Record> {
      public StringField query = this.addString("query");
      public CLongField cf = this.addCLong("cf");
      public CLongField df = this.addCLong("df");

      public File(Datafile df) {
         super(df);
      }

      @Override
      public Record newRecord() {
         return new Record();
      }

      @Override
      public Record closingRecord() {
         Record r = new Record();
         r.query = "";
         r.cf=-1;
         r.df=-1;
         return r;
      }
   }

   public class Record implements StructuredFileKeyValueRecord<File> {
      public String query;
      public long cf;
      public long df;

      public String toString() {
         return PrintTools.sprintf("bucketindex=%d query=%s cf=%d df=%d", this.hashCode(), query, cf, df);
      }
      
      @Override
      public int hashCode() {
         return MathTools.finishHash(MathTools.hashCode(query.hashCode()));
      }

      @Override
      public boolean equals(Object r) {
         if (r instanceof Record) {
            Record record = (Record)r;
            return query.equals( record.query );  
         }
         return false;
      }

      @Override
      public void write(File file) {
         file.query.write(query);
         file.cf.write(cf);
         file.df.write(df);
      }

      @Override
      public void read(File file) {
         query = file.query.value;
         cf = file.cf.value;
         df = file.df.value;
      }

      public void convert(StructuredFileKeyValueRecord record) {
         Record r = (Record)record;
         r.query = query;
         r.cf = cf;
         r.df = df;
      }
   }
}
