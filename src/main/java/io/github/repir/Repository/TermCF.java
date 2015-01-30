package io.github.repir.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import io.github.repir.tools.io.Datafile;
import io.github.repir.tools.io.struct.StructuredFileByteJumpTable;
import io.github.repir.tools.io.struct.StructuredDataStream;
import io.github.repir.Repository.TermCF.File;
import io.github.repir.tools.lib.Const;
import io.github.repir.tools.lib.Log;

/**
 * Stores the collection frequency of terms, which can be accessed
 * by {@link #readValue(int)} using the termID. 
 * @author jer
 */
public class TermCF extends StoredUnreportableFeature<File> implements DictionaryFeature {

   public static Log log = new Log(TermCF.class);
   public HashMap<Integer, Long> cache = new HashMap<Integer, Long>();

   private TermCF(Repository repository) {
      super(repository);
      readCache();
   }

   public static TermCF get(Repository repository) {
       String label = canonicalName(TermCF.class);
       TermCF termcf = (TermCF)repository.getStoredFeature(label);
       if (termcf == null) {
          termcf = new TermCF(repository);
          repository.storeFeature(label, termcf);
       }
       return termcf;
   }
   
   public void readCache() {
      ArrayList<Integer> termids = repository.configuredIntList("repository.cachedtermids");
      if (termids.size() > 0) {
         ArrayList<Long> cf = repository.configuredLongList("repository.cachedtermtfs");
         for (int i = 0; i < termids.size() && i < cf.size(); i++) {
            if (cf.get(i) != Const.NULLLONG) {
               cache.put(termids.get(i), cf.get(i));
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
         ArrayList<Long> cf = new ArrayList<Long>();
         NEXT:
         for (Integer i : termids) {
            Long v = cache.get(i);
            if (v == null)
               v = Const.NULLLONG;
            cf.add(v);
         }
         repository.getConf().setIntList("repository.cachedtermids", termids);
         repository.getConf().setLongList("repository.cachedtermtfs", cf);
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

   public static class File extends StructuredFileByteJumpTable {

      public StructuredDataStream.CLongField cf = this.addCLong("cf");

      public File(Datafile df) {
         super(df);
      }
   }
}
