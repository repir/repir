package io.github.repir.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import io.github.repir.tools.Content.Datafile;
import io.github.repir.tools.Content.RecordJumpArray;
import io.github.repir.tools.Content.StructuredDataStream;
import io.github.repir.Repository.TermTF.File;
import io.github.repir.tools.Lib.Const;
import io.github.repir.tools.Lib.Log;

public class TermTF extends StoredUnreportableFeature<File> implements DictionaryFeature {

   public static Log log = new Log(TermTF.class);
   public HashMap<Integer, Long> cache = new HashMap<Integer, Long>();

   protected TermTF(Repository repository) {
      super(repository);
      readCache();
   }

   public void readCache() {
      ArrayList<Integer> termids = repository.getConfigurationIntList("repository.cachedtermids");
      if (termids.size() > 0) {
         ArrayList<Long> tf = repository.getConfigurationLongList("repository.cachedtermtfs");
         for (int i = 0; i < termids.size() && i < tf.size(); i++) {
            if (tf.get(i) != Const.NULLLONG) {
               cache.put(termids.get(i), tf.get(i));
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
         ArrayList<Long> tf = new ArrayList<Long>();
         NEXT:
         for (Integer i : termids) {
            Long v = cache.get(i);
            if (v == null)
               v = Const.NULLLONG;
            tf.add(v);
         }
         repository.getConfiguration().setIntList("repository.cachedtermids", termids);
         repository.getConfiguration().setLongList("repository.cachedtermtfs", tf);
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
      return file.tf.value;
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
   public void reduceInput(int id, String term, long tf, long df) {
      file.tf.write(tf);
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

   public static class File extends RecordJumpArray {

      public StructuredDataStream.CLongField tf = this.addCLong("tf");

      public File(Datafile df) {
         super(df);
      }
   }
}
