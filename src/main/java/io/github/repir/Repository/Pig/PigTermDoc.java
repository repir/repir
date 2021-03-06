package io.github.repir.Repository.Pig;

import io.github.repir.Repository.EntityStoredFeature;
import io.github.repir.Repository.Pig.PigTermDoc.File;
import io.github.repir.Repository.Pig.PigTermDoc.Tuple;
import io.github.repir.Repository.Repository;
import io.github.repir.Repository.StoredFeature;
import io.github.htools.io.Datafile;
import io.github.htools.lib.Log;
import io.github.htools.io.struct.StructuredTextFile;
import io.github.htools.io.struct.StructuredTextPig;
import io.github.htools.io.struct.StructuredTextPigTuple;

/**
 * Can store one literal String per Document, e.g. collection ID, title, url.
 * @see EntityStoredFeature
 * @author jer
 */
public class PigTermDoc extends PigFeatureField<File, Tuple>  {

   public static Log log = new Log(PigTermDoc.class);

   private PigTermDoc(Repository repository, String field) {
      super(repository, field);
   }

   public static PigTermDoc get(Repository repository, String field) {
       String label = canonicalName(PigTermDoc.class, field);
       PigTermDoc pigtermdoc = (PigTermDoc) StoredFeature.getStoredFeature(repository, label);
       if (pigtermdoc == null) {
          pigtermdoc = new PigTermDoc(repository, field);
          StoredFeature.storeFeature(repository, label, pigtermdoc);
       }
       return pigtermdoc;
   }
   
   @Override
   public File createFile(Datafile datafile) {
      return new File(datafile);
   }

   @Override
   public Tuple getValue() {
      if (getFile().nextRecord()) {
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
