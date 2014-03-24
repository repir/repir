package io.github.repir.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import io.github.repir.tools.Content.Datafile;
import io.github.repir.tools.Content.StructuredFileByteJumpTable;
import io.github.repir.tools.Content.StructuredDataStream;
import io.github.repir.Repository.TermCF.File;
import io.github.repir.tools.Lib.Const;
import io.github.repir.tools.Lib.Log;

/**
 * Stores the collection frequency of terms, which can be accessed
 * by {@link #readValue(int)} using the termID. 
 * @author jer
 */
public class TermCF extends StoredUnreportableFeature<File> implements DictionaryFeature {

   public static Log log = new Log(TermCF.class);
   public HashMap<Integer, Long> cache = new HashMap<Integer, Long>();

   protected TermCF(Repository repository) {
      super(repository);
      readCache();
   }

   public void readCache() {
      ArrayList<Integer> termids = repository.getConfigurationIntList("repository.cachedtermids");
      if (termids.size() > 0) {
         ArrayList<Long> cf = repository.getConfigurationLongList("repository.cachedtermtfs");
         for (int i = 0; i < termids.size() && i < cf.size(); i++) {
            if (cf.get(i) != Const.NULLLONG) {
               cache.put(termids.get(i), cf.get(i));
            }
         }
      }
   }

   public void writeCache() {
      ArrayList<Integer> termids = repository.getConfigurationIntList("repository.cachedtermids");
      for (Integer s : cache.keySet())
         if (!termids.contains(s))
            termids.add(s);
      if (termids.size() > 0) {
         ArrayList<Long> cf = new ArrayList<Long>();
         NEXT:
         for (Integer i : termids) {
            Long v = cache.get(i);
            if (v == null)
               v = Const.NULLLONG;
            cf.add(v);
         }
         repository.getConfiguration().setIntList("repository.cachedtermids", termids);
         repository.getConfiguration().setLongList("repository.cachedtermtfs", cf);
      }
   }
   
   public void dontCache() {
      cache = null;
   }

   public void loadMem() {
      openRead();
      file.loadMem();
   }

   public void unloadMem() {
      getFile().unloadMem();
   }

   @Override
   public File createFile(Datafile datafile) {
      return new File(datafile);
   }

   public Long getValue() {
      return file.cf.value;
   }

   public long readValue(int id) {
      Long value;
      if (cache != null) {
         value = cache.get(id);
         if (value != null)
            return value;
      }
      getFile().read(id);
      value = getValue();
      if (cache != null)
         cache.put(id, value);
      return value;
   }
   
   @Override
   public void reduceInput(int id, String term, long cf, long df) {
      file.cf.write(cf);
   }

   @Override
   public void startReduce(long corpustermfreq, int corpusdocumentfrequency) {
      openWrite();
   }

   @Override
   public void finishReduce() {
      closeWrite();
   }

   @Override
   public void setBufferSize(int size) {
      getFile().setBufferSize(size);
   }

   public static class File extends StructuredFileByteJumpTable {

      public StructuredDataStream.CLongField cf = this.addCLong("cf");

      public File(Datafile df) {
         super(df);
      }
   }
}
