package io.github.repir.Repository.Pig;

import io.github.repir.Repository.EntityStoredFeature;
import io.github.repir.Repository.Pig.PigTerm.File;
import io.github.repir.Repository.Pig.PigTerm.Tuple;
import io.github.repir.Repository.Repository;
import io.github.repir.tools.Content.Datafile;
import io.github.repir.tools.Content.StructuredTextPig;
import io.github.repir.tools.Content.StructuredTextPigTuple;
import io.github.repir.tools.Lib.Log;

/**
 * Can store one literal String per Document, e.g. collection ID, title, url.
 * @see EntityStoredFeature
 * @author jer
 */
public class PigTerm extends PigFeature<File, Tuple>  {

   public static Log log = new Log(PigTerm.class);

   protected PigTerm(Repository repository) {
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
      public IntField id = this.addInt("id");
      public StringField term = this.addString("term");
      public BoolField isstopword = this.addBoolean("isstopword");
      public LongField cf = this.addLong("cf");
      public LongField df = this.addLong("df");

      public File(Datafile df) {
         super(df);
      }
   }
   
   public static class Tuple extends StructuredTextPigTuple<File> {
      public int id;
      public String term;
      public boolean isstopword;
      public long cf;
      public long df;
      
      public Tuple() {}
      
      protected Tuple(File file) {
         id = file.id.get();
         term = file.term.get();
         isstopword = file.isstopword.get();
         cf = file.cf.get();
         df = file.df.get();
      }
      
      @Override
      public void write(File file) {
         file.id.set(id);
         file.term.set(term);
         file.isstopword.set(isstopword);
         file.cf.set(cf);
         file.df.set(df);
         file.write();
      }
   }
}
