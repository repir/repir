package io.github.repir.EntityReader;

import io.github.repir.tools.Content.Datafile;
import io.github.repir.tools.Content.StructuredFile;
import io.github.repir.tools.Lib.Log;

/**
 * File structure to store the key and pointer part of an inverted index. The
 * idea is that the key-part is small enough to be kept in memory, and the
 * offsets can be used to retrieve the posting lists from disk. The offsets
 * point directly to the posting list in the accompanying datafile.
 */
public class SubSetFile extends StructuredFile {

   public static Log log = new Log(SubSetFile.class);

   /**
    * This constructor does not open the file for read/write.
    * <p/>
    * @param filename
    */
   public SubSetFile(Datafile df) {
      super(df);
   }
   public StringField cluewebid = this.addString("cluewebid");

   public static idlist getIdList(Datafile df) {
      idlist sl = new idlist();
      SubSetFile sf = new SubSetFile(df);
      sf.setBufferSize(10000000);
      sf.openRead();
      while (sf.next()) {
         sl.set(sf.cluewebid.value);
      }
      sf.closeRead();
      return sl;
   }
}
