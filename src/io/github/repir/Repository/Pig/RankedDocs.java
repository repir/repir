package io.github.repir.Repository.Pig;

import io.github.repir.Repository.EntityStoredFeature;
import io.github.repir.Repository.Pig.RankedDocs.File;
import io.github.repir.Repository.Pig.RankedDocs.Tuple;
import io.github.repir.Repository.Repository;
import io.github.repir.tools.Content.Datafile;
import io.github.repir.tools.Content.StructuredTextFile;
import io.github.repir.tools.Content.StructuredTextPig;
import io.github.repir.tools.Content.StructuredTextPigTuple;
import io.github.repir.tools.Lib.Log;

/**
 * Can store one literal String per Document, e.g. collection ID, title, url.
 * @see EntityStoredFeature
 * @author jer
 */
public class RankedDocs extends PigFeature<File, Tuple>  {

   public static Log log = new Log(RankedDocs.class);

   protected RankedDocs(Repository repository) {
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
      public StructuredTextFile.IntField topic = this.addInt("topic");
      public StructuredTextFile.IntField docid = this.addInt("docid");
      public StructuredTextFile.IntField docpartition = this.addInt("docpartition");
      public StructuredTextFile.StringField collectionid = this.addString("collectionid");
      public StructuredTextFile.DoubleField score = this.addDouble("score");

      public File(Datafile df) {
         super(df);
      }
   }
   
   public static class Tuple extends StructuredTextPigTuple<File> {
      public int topic;
      public int docid;
      public int docpartition;
      public String collectionid;
      public double score;
      
      public Tuple() {}
      
      protected Tuple(File file) {
         topic = file.topic.get();
         docid = file.docid.get();
         docpartition = file.docpartition.get();
         collectionid = file.collectionid.get();
         score = file.score.get();
      }
      
      @Override
      public void write(File file) {
         file.topic.set(topic);
         file.docid.set(docid);
         file.docpartition.set(docpartition);
         file.collectionid.set(collectionid);
         file.score.set(score);
         file.write();
      }
      
      
   }
}
