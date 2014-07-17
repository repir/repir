package io.github.repir.Repository.Pig;

import io.github.repir.Repository.EntityStoredFeature;
import io.github.repir.Repository.Pig.PigTermDocPos.File;
import io.github.repir.Repository.Pig.PigTermDocPos.Tuple;
import io.github.repir.Repository.Repository;
import io.github.repir.tools.Content.Datafile;
import io.github.repir.tools.Structure.StructuredTextFile;
import io.github.repir.tools.Structure.StructuredTextFile.NodeValue;
import io.github.repir.tools.Structure.StructuredTextPig;
import io.github.repir.tools.Structure.StructuredTextPigTuple;
import io.github.repir.tools.Lib.ArrayTools;
import io.github.repir.tools.Lib.Log;
import java.util.ArrayList;

/**
 * Can store one literal String per Document, e.g. collection ID, title, url.
 * @see EntityStoredFeature
 * @author jer
 */
public class PigTermDocPos extends PigFeatureField<File, Tuple>  {

   public static Log log = new Log(PigTermDocPos.class);

   protected PigTermDocPos(Repository repository, String field) {
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
      public StringField term = this.addString("term");
      public Bag pos = this.addBag("pos");
      public IntField position = this.addInt(pos, "position");

      public File(Datafile df) {
         super(df);
      }
   }
   
   public static class Tuple extends StructuredTextPigTuple<File> {
      public int docid;
      public int partition;
      public String term;
      public int position[];
      
      public Tuple() {}
      
      protected Tuple(File file) {
         docid = file.docid.get();
         partition = file.docpartition.get();
         ArrayList<NodeValue> posarray = file.pos.get();
         position = new int[posarray.size()];
         term = file.term.get();
         int p = 0;
         for (NodeValue n : posarray) {
            position[p++] = (Integer)n.get(file.position);      
         }
      }
      
      @Override
      public void write(File file) {
         file.docid.set(docid);
         file.docpartition.set(partition);
         file.term.set(term);
         for (int p : position) {
            file.pos.addAnother();
            file.position.set(p);
         }
         file.write();
      }
   }
}