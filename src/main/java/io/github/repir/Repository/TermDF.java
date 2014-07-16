package io.github.repir.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import io.github.repir.tools.Content.Datafile;
import io.github.repir.tools.Structure.StructuredFileByteJumpTable;
import io.github.repir.Repository.TermDF.File;
import io.github.repir.tools.Lib.Const;
import io.github.repir.tools.Lib.Log;

/**
 * Stores the document frequency of terms in the Vocabulary, which can be accessed
 * by {@link #readValue(int)} using the termID. 
 * @author jer
 */
public class TermDF extends StoredUnreportableFeature<File> implements DictionaryFeature {

   public static Log log = new Log(TermDF.class);
   int keyid = 0;
   public HashMap<Integer, Long> cache = new HashMap<Integer,Long>();

   protected TermDF(Repository repository) {
      super(repository);
      readCache();
   }
   
   public void readCache() {
      ArrayList<Integer> termids = repository.configuredIntList("repository.cachedtermids");
      if (termids.size() > 0) {
         ArrayList<Long> df = repository.configuredLongList("repository.cachedtermdfs");
         for (int i = 0; i < termids.size() && i < df.size(); i++) {
            if (df.get(i) != Const.NULLLONG) {
               cache.put(termids.get(i), df.get(i));
            }
         }
      }
   }

   public void writeCache() {
      ArrayList<Integer> termids = repository.configuredIntList("repository.cachedtermids");
      for (Integer s : cache.keySet())
         if (!termids.contains(s))
            termids.add(s);
      if (termids.size() > 0) {
         ArrayList<Long> df = new ArrayList<Long>();
         NEXT:
         for (Integer i : termids) {
            Long v = cache.get(i);
            if (v == null)
               v = Const.NULLLONG;
            df.add(v);
         }
         repository.getConfiguration().setIntList("repository.cachedtermids", termids);
         repository.getConfiguration().setLongList("repository.cachedtermdfs", df);
      }
   }
   
   public void dontCache() {
      cache = null;
   }

   public void write(Long df) {
      file.df.write(df);
   }

   public long readValue(int id) {
      Long value;
      if (cache != null) {
         value = cache.get(id);
         if (value != null)
            return value;
      }
         
      if (file == null)
         openRead();
      file.read(id);
      value = getValue();
      if (cache != null)
         cache.put(id, value);
      return value;
   }
   
   public Long getValue() {
      return file.df.value;
   }

   public void loadMem() {
      openRead();
      file.loadMem();
   }

   public void unloadMem() {
      file.unloadMem();
   }

   @Override
   public File createFile(Datafile datafile) {
      return new File(datafile);
   }

   @Override
   public void reduceInput(int id, String term, long cf, long df) {
       write( df );
   }

   @Override
   public void startReduce(long corpustermfreq, int corpusdocumentfrequency) {
      openWrite();
   }

   @Override
   public void finishReduce() {
       closeWrite();
   }

   public static class File extends StructuredFileByteJumpTable {

      public CLongField df = this.addCLong("df");

      public File(Datafile df) {
         super(df);
      }
   }
}
