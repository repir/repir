package io.github.repir.EntityReader;

import io.github.repir.tools.Content.Datafile;
import io.github.repir.tools.Structure.StructuredFile;
import io.github.repir.tools.Lib.Log;

/**
 * Contains a list of document ID's for selective indexing. 
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
   public StringField documentid = this.addString("documentid");

   public static idlist getIdList(Datafile df) {
      idlist sl = new idlist();
      SubSetFile sf = new SubSetFile(df);
      sf.setBufferSize(10000000);
      sf.openRead();
      while (sf.nextRecord()) {
         sl.set(sf.documentid.value);
      }
      sf.closeRead();
      return sl;
   }
}
