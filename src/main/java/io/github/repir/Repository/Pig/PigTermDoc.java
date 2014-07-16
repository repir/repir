package io.github.repir.Repository.Pig;

import io.github.repir.Repository.EntityStoredFeature;
import io.github.repir.Repository.Pig.PigTermDoc.File;
import io.github.repir.Repository.Pig.PigTermDoc.Tuple;
import io.github.repir.Repository.Repository;
import io.github.repir.tools.Content.Datafile;
import io.github.repir.tools.Structure.StructuredTextFile;
import io.github.repir.tools.Structure.StructuredTextPig;
import io.github.repir.tools.Structure.StructuredTextPigTuple;
import io.github.repir.tools.Lib.Log;

/**
 * Can store one literal String per Document, e.g. collection ID, title, url.
 * @see EntityStoredFeature
 * @author jer
 */
public class PigTermDoc extends PigFeatureField<File, Tuple>  {

   public static Log log = new Log(PigTermDoc.class);

   protected PigTermDoc(Repository repository, String field) {
      super(repository, field);
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
      getFile().write(value);
   }
   
   public static class File extends StructuredTextPig {
      public IntField docid = this.addInt("docid");
      public IntField docpartition = this.addInt("docpartition");
      public IntField tf = this.addInt("tf");

      public File(Datafile df) {
         super(df);
      }
   }
   
   public static class Tuple extends StructuredTextPigTuple<File> {
      public int docid;
      public int partition;
      public int tf;
      
      public Tuple() {}
      
      protected Tuple(File file) {
         docid = file.docid.get();
         partition = file.docpartition.get();
         tf = file.tf.get();
      }
      
      @Override
      public void write(File file) {
         file.docid.set(docid);
         file.docpartition.set(partition);
         file.tf.set(tf);
         file.write();
      }
   }
}
