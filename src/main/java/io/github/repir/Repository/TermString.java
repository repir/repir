package io.github.repir.Repository;

import io.github.repir.Repository.TermString.File;
import io.github.repir.Retriever.Document;
import io.github.htools.io.Datafile;
import io.github.htools.io.Datafile.STATUS;
import io.github.htools.io.EOCException;
import io.github.htools.io.struct.StructuredFileByteJumpTable;
import io.github.htools.io.struct.StructuredDataStream;
import io.github.htools.lib.Log;

/**
 * Stores the stemmed string of terms in the Vocabulary, which can be accessed
 * by {@link #readValue(int)} using the termID. 
 * @author jer
 */

public class TermString extends VocabularyToString<File> {

   public static Log log = new Log(TermString.class);
   private boolean loaded = false;
   private String[] cache = new String[0];

   protected TermString(Repository repository) {
      super(repository);
   }

   public static TermString get(Repository repository) {
       String label = canonicalName(TermString.class);
       TermString termid = (TermString)repository.getStoredFeature(label);
       if (termid == null) {
          termid = new TermString(repository);
          repository.storeFeature(label, termid);
       }
       return termid;
   }
   
   public String readValue(int id) {
      if (id < cache.length) {
         return cache[id];
      }
      if (file == null || file.getDatafile().status != STATUS.READ) {
         openRead();
      }
      file.read(id);
      return file.term.value;
   }

   public void loadMem(int size) {
      size = Math.min(size, repository.getVocabularySize());
      if (size != cache.length) {
         super.openRead();
         file.setBufferSize(4096 * 10000);
         file.setOffset(0);
         cache = new String[size];
         int entry = 0;
         try {
            for (entry = 0; entry < size; entry++) {
               cache[entry] = file.term.read();
            }
         } catch (EOCException ex) {
            log.info("EOF reached at term %d", entry);
            log.exception(ex, "loadMem( %d ) index %s vocfile %s", size, repository, file);
         }
         file.setBufferSize(1000);
      }
   }
   
   public void openRead() {
      loadMem(100000);
   }

   public void closeRead() {
      super.closeRead();
      cache = new String[0];
   }

   public void write(String term) {
      file.term.write(term);
   }

   @Override
   public File createFile(Datafile datafile) {
      return new File(datafile);
   }

   @Override
   public void reduceInput(int id, String term, long cf, long df) {
        write(term);
   }

   @Override
   public void startReduce(long corpustermfreq, int corpusdocumentfrequency) {
      openWrite();
   }

   @Override
   public void finishReduce() {
      closeWrite();
   }

   public class File extends StructuredFileByteJumpTable {

      public StructuredDataStream.String0Field term = this.addString0("term");

      public File(Datafile df) {
         super(df);
      }
   }
}
