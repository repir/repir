package io.github.repir.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import io.github.repir.Repository.TermID.File;
import io.github.repir.tools.Content.Datafile;
import io.github.repir.tools.Content.Datafile.Status;
import io.github.repir.tools.Content.RecordSortHash;
import io.github.repir.tools.Content.RecordSortHashRecord;
import io.github.repir.tools.Content.RecordSortRecord;
import io.github.repir.tools.Lib.ArrayTools;
import io.github.repir.tools.Lib.Log;
import io.github.repir.tools.Lib.PrintTools;

/**
 * Fetches the internal term id for a term string. To improve lookup speed, the
 * most common terms are kept in memory, while less common terms remain on disk.
 * <p/>
 * Before requesting the internal term id, the text should be processed by the
 * same {@link Extractor} process as used for indexing.
 * {@link #get(java.lang.String)} is used to obtain the term id of a single
 * term, while {@link #getContent(Extractor.EntityAttribute)} is used to obtain
 * an array of term id's to represent a multi term text.
 * <p/>
 * @author jeroen
 */
public class TermID extends VocabularyToID<File> { 

   public static Log log = new Log(TermID.class);
   public HashMap<String, Integer> cache = new HashMap<String,Integer>();

   protected TermID(Repository repository) {
      super(repository);
      readCache();
   }
   
   public void readCache() {
      ArrayList<Integer> termids = repository.getConfigurationIntList("repository.cachedtermids");
      if (termids.size() > 0) {
         String termstrings[] = repository.getConfigurationSubStrings("repository.cachedtermstring");
         for (int i = 0; i < termids.size() && i < termstrings.length; i++) {
            if (termstrings[i].length() > 0) {
               int termid = termids.get(i);
               cache.put(termstrings[i], termid);
            }
         }
      }
   }

   @Override
   public void writeCache() {
      ArrayList<Integer> termids = repository.getConfigurationIntList("repository.cachedtermids");
      for (Integer s : cache.values())
         if (!termids.contains(s))
            termids.add(s);
      if (termids.size() > 0) {
         ArrayList<String> termstrings = new ArrayList<String>();
         NEXT:
         for (Integer i : termids) {
            for (Map.Entry<String, Integer> entry : cache.entrySet()) {
               if (entry.getValue().equals(i)) {
                  termstrings.add(entry.getKey());
                  continue NEXT;
               }
            }
            termstrings.add("");
         }
         repository.getConfiguration().setIntList("repository.cachedtermids", termids);
         repository.getConfiguration().setStringList("repository.cachedtermstring", termstrings);
      }
   }

   @Override
   public int get(String term) {
      Integer tid = cache.get(term);
      if (tid != null)
         return tid;
      if (getFile().getDatafile().status != Status.READ) {
         openRead();
      }
      int termid = io.github.repir.tools.Lib.Const.NULLINT;
      Record termrecord = new Record(file);
      termrecord.term = term;
      Record termfound = (Record) termrecord.find();
      if (termfound != null) {
         termid = termfound.id;
      } else {
         log.info("Term not found %s repo %s", term, repository.getTestsetName());
         log.info("TermID file %s", file.getDatafile().getFullPath());
         //log.crash();
      }
      cache.put(term, termid);
      return termid;
   }

   @Override
   public void openWrite() {
      getFile().setBufferSize(100000);
      file.setTableSize(repository.getVocabularySize());
      file.openWrite();
   }

   public void write(int id, String term) {
      Record termrecord = new Record(file);
      termrecord.id = id;
      termrecord.term = term;
      termrecord.write();
   }

   @Override
   public boolean exists(String term) {
      return get(term) >= 0;
   }

   @Override
   public File createFile(Datafile datafile) {
      return new File(datafile, repository.getVocabularySize());
   }

   @Override
   public void reduceInput(int id, String term, long tf, long df) {
       write(id, term);
   }

   @Override
   public void startReduce(long corpustermfreq, int corpusdocumentfrequency) {
      this.openWrite();
   }

   @Override
   public void finishReduce() {
      closeWrite();
   }

   @Override
   public void setBufferSize(int size) {
      getFile().setBufferSize(size);
   }

   public class File extends RecordSortHash {

      public String0Field term = this.addString0("term");
      public IntField id = this.addInt("id");

      public File(Datafile df, int tablesize) {
         super(df, tablesize);
      }
      
      public File clone() {
         return new File( new Datafile(datafile), getTableSize() );
      }

      @Override
      public RecordSortRecord createRecord() {
         Record r = new Record(this);
         r.offsetread = this.recordoffset;
         r.id = id.value;
         r.term = term.value;
         return r;
      }

   }
   
   public class Record extends RecordSortHashRecord {

         public String term;
         public int id;
         public long offsetread;
         
         public Record(File file) {
            super(file);
         }
         
         public int hashCode() {
            return term.hashCode();
         }
         
         public String toString() {
            return PrintTools.sprintf("hash %d bucket %d id %d term %s offsetread %d", hashCode(), getBucketIndex(), id, term, offsetread);
         }
         
         @Override
         protected void writeRecordData() {
            //log.printf("writeRecordData() cap %d bucket %d id %d term %s offr %d offw %d", file.getBucketCapacity(), this.getBucketIndex(), id, term, offsetread, file.getOffset());
            ((File) file).term.write(term);
            ((File) file).id.write(id);
         }

         @Override
         public boolean equals(Object r) {
            if (r instanceof Record)
               return term.equals(((Record) r).term);
            return false;
         }
      }

}
