package io.github.repir.Repository.Pig;

import io.github.repir.Repository.EntityStoredFeature;
import io.github.repir.Repository.Pig.PigRepo.File;
import io.github.repir.Repository.Pig.PigRepo.Tuple;
import io.github.repir.Repository.Repository;
import io.github.repir.tools.Content.Datafile;
import io.github.repir.tools.Structure.StructuredTextPig;
import io.github.repir.tools.Structure.StructuredTextPigTuple;
import io.github.repir.tools.Lib.Log;

/**
 * Can store one literal String per Document, e.g. collection ID, title, url.
 * @see EntityStoredFeature
 * @author jer
 */
public class PigRepo extends PigFeature<File, Tuple>  {

   public static Log log = new Log(PigRepo.class);

   protected PigRepo(Repository repository) {
      super(repository);
   }

   @Override
   public File createFile(Datafile datafile) {
      return new File(datafile);
   }

   @Override
   public Tuple getValue() {
      if (getFile().next()) {
         return new Tuple(file);
      } else {
         return null;  
      }
   }

   @Override
   public void write(Tuple value) {
      file.write(value);
   }
   
   public static class File extends StructuredTextPig {
      public StringField key = this.addString("key");
      public StringField value = this.addString("value");

      public File(Datafile df) {
         super(df);
      }
   }
   
   public static class Tuple extends StructuredTextPigTuple<File> {
      public String key;
      public String value;
      
      public Tuple() {}
      
      protected Tuple(File file) {
         key = file.key.get();
         value = file.value.get();
      }
      
      @Override
      public void write(File file) {
         file.key.set(key);
         file.value.set(value);
         file.write();
      }
   }
}
