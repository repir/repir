package io.github.repir.Repository;

import io.github.repir.Repository.ProximityStats.File;
import io.github.repir.Repository.ProximityStats.Record;
import io.github.repir.tools.Content.Datafile;
import io.github.repir.tools.Content.StructuredFileKeyValue;
import io.github.repir.tools.Content.StructuredFileKeyValueRecord;
import io.github.repir.tools.Lib.ArrayTools;
import io.github.repir.tools.Lib.Log;
import io.github.repir.tools.Lib.MathTools;
import io.github.repir.tools.Lib.PrintTools;

/**
 * This feature caches co-occurrence statistics (collection frequency and document 
 * frequency) that were retrieved, to speedup retrieval when the same co-occurrence
 * statistics are needed again. This is used by the default FeatureProximity operators. 
 * They first look if the co-occurrence statistics for a proximity operator are
 * already available, if not a CollectorProximity is used to fetch these in a pre-pass
 * which are automatically stored for reuse. 
 * <p/>
 * Note that this only applies to simple
 * Proximity operators on query, RR can also handle complex proximity operators 
 * that operate for instance on other proximity operators or synonyms, but these
 * are not cached but retrieved in a pre-pass and passed, and the statistics are
 * non-persistently passed on to the next (final) retrieval pass. 
 * @author jer
 */
public class ProximityStats extends StoredDynamicFeature<File, Record> {

   public static Log log = new Log(ProximityStats.class);

   protected ProximityStats(Repository repository) {
      super(repository);
   }

   @Override
   public File createFile(Datafile df) {
      return new File(df);
   }

   public class File extends StructuredFileKeyValue<Record> {

      public StringField query = this.addString("query");
      public CLongField cf = this.addCLong("cf");
      public CIntField df = this.addCInt("df");

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
      public int df;

      public String toString() {
         return PrintTools.sprintf("bucketindex=%d query=%s cf=%d df=%d", this.hashCode(), query, cf, df);
      }

      @Override
      public int hashCode() {
         return MathTools.finishHash(MathTools.hash(query.hashCode()));
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
