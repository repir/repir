package io.github.repir.EntityReader;

import io.github.repir.tools.Content.Datafile;
import io.github.repir.tools.Structure.StructuredFile;
import io.github.repir.tools.Lib.Log;

/**
 * File structure to store the key and pointer part of an inverted index. The
 * idea is that the key-part is small enough to be kept in memory, and the
 * offsets can be used to retrieve the posting lists from disk. The offsets
 * point directly to the posting list in the accompanying datafile.
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
      while (sf.next()) {
         if (sf.spamindex.value >= spamthreshold) {
            sl.set(sf.cluewebid.value);
         }
      }
      sf.closeRead();
      return sl;
   }
}
