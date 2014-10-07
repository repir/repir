package io.github.repir.Repository.Pig;

import io.github.repir.Repository.EntityStoredFeature;
import static io.github.repir.Repository.Feature.canonicalName;
import io.github.repir.Repository.Pig.PigDoc.File;
import io.github.repir.Repository.Pig.PigDoc.Tuple;
import io.github.repir.Repository.Repository;
import io.github.repir.Repository.StoredFeature;
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
public class PigDoc extends PigFeature<File, Tuple>  {

   public static Log log = new Log(PigDoc.class);

   protected PigDoc(Repository repository) {
      super(repository);
   }

   public static PigDoc get(Repository repository) {
       String label = canonicalName(PigTermDoc.class);
       PigDoc pigtermdoc = (PigDoc) StoredFeature.getStoredFeature(repository, label);
       if (pigtermdoc == null) {
          pigtermdoc = new PigDoc(repository);
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
      file.write(value);
   }
   
   public static class File extends StructuredTextPig {
      public IntField id = this.addInt("id");
      public IntField docpartition = this.addInt("docpartition");
      public StringField collectionid = this.addString("collectionid");
      public StringField title = this.addString("title");
      public IntField tf = this.addInt("tf");

      public File(Datafile df) {
         super(df);
      }
   }
   
   public static class Tuple extends StructuredTextPigTuple<File> {
      public int id;
      public int partition;
      public String collectionid;
      public String title;
      public int tf;
      
      public Tuple() {}
      
      protected Tuple(File file) {
         id = file.id.get();
         partition = file.docpartition.get();
         collectionid = file.collectionid.get();
         title = file.title.get();
         tf = file.tf.get();
      }
      
      @Override
      public void write(File file) {
         file.id.set(id);
         file.docpartition.set(partition);
         file.collectionid.set(collectionid);
         file.title.set(title);
         file.tf.set(tf);
         file.write();
      }
   }
}
