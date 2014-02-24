package io.github.repir.Repository;

import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import io.github.repir.Repository.ModelParameters.File;
import io.github.repir.Repository.ModelParameters.Record;
import io.github.repir.Repository.Repository;
import io.github.repir.Repository.StoredDynamicFeature;
import io.github.repir.tools.Content.Datafile;
import io.github.repir.tools.Content.RecordHeaderData;
import io.github.repir.tools.Content.RecordHeaderDataRecord;
import io.github.repir.tools.Lib.Log;
import io.github.repir.tools.Lib.MathTools;

/**
 * 
 * @author jer
 */
public class ModelParameters extends StoredDynamicFeature<File, Record> {

   public static Log log = new Log(ModelParameters.class);
   
   protected ModelParameters(Repository repository) {
      super(repository);
   }

   @Override
   public File createFile(Datafile df) {
      return new File(df);
   }
   
   public Record newRecord( String settings[] ) {
      return getFile().newRecord(settings);
   }

   public class File extends RecordHeaderData<Record> {

      public StringArrayField parameter = this.addStringArray("parameter");
      public StringArrayField value = this.addStringArray("value");
      public DoubleField map = this.addDouble("map");

      public File(Datafile df) {
         super(df);
      }

      @Override
      public Record newRecord() {
         return new Record();
      }
      
      public Record newRecord( String settings[] ) {
         return new Record( settings );
      }
   }

   public class Record implements RecordHeaderDataRecord<File> {
      public TreeMap<String, String> parameters = new TreeMap<String,String>();
      public double map = -1;
      
      public Record() {}
      
      public Record( String settings[] ) {
         for (String s : settings) {
            parameters.put(s, repository.getConfigurationString(s));
         }
      }
      
      @Override
      public int hashCode() {
         int hash = 31;
         for (Map.Entry<String, String> e : parameters.entrySet()) {
            hash = MathTools.combineHash(hash, e.getKey().hashCode());
            hash = MathTools.combineHash(hash, e.getValue().hashCode());
         }
         return MathTools.finishHash(hash);
      }

      @Override
      public boolean equals(Object r) {
         if (r instanceof Record) {
            Record record = (Record)r;
            return parameters.equals(record.parameters);
         }
         return false;
      }

      public void write(File file) {
         file.parameter.write( parameters.keySet().toArray(new String[ parameters.size() ]));
         file.value.write( parameters.values().toArray(new String[ parameters.size() ]));
         file.map.write(map);
      }

      public void read(File file) {
         for (int i = 0; i < file.parameter.value.length; i++) {
            parameters.put( file.parameter.value[i], file.value.value[i]);
         }
         map = file.map.value;
      }

      public void convert(RecordHeaderDataRecord record) {
         Record r = (Record)record;
         r.parameters = (TreeMap<String, String>)parameters.clone();
         r.map = map;
      }
   }
   
   public Record read( String settings[] ) {
      this.openRead();
      Record s = (Record)newRecord( settings );
      Record r = (Record) find(s);
      return r;
   }
   
   public Record read( Record record ) {
      this.openRead();
      Record found = (Record) find(record);
      return (found == null)?record:found;
   }
}
