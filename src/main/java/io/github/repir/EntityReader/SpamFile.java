package io.github.repir.EntityReader;

import io.github.repir.tools.Content.Datafile;
import io.github.repir.tools.Structure.StructuredFile;
import io.github.repir.tools.Lib.Log;

/**
 * A file containing the document ID's and Waterloo Fusion Spam index, to allow
 * selective indexing of documents above a certain threshold.
 */
public class SpamFile extends StructuredFile {

   public static Log log = new Log(SpamFile.class);

   /**
    * This constructor does not open the file for read/write.
    * <p/>
    * @param filename
    */
   public SpamFile(Datafile df) {
      super(df);
   }
   public StringField cluewebid = this.addString("cluewebid");
   public IntField spamindex = this.addInt("spamindex");

   public static idlist getIdList(Datafile df, int spamthreshold) {
      idlist sl = new idlist();
      SpamFile sf = new SpamFile(df);
      sf.setBufferSize(10000000);
      sf.openRead();
      while (sf.nextRecord()) {
         if (sf.spamindex.value >= spamthreshold) {
            sl.set(sf.cluewebid.value);
         }
      }
      sf.closeRead();
      return sl;
   }
}
