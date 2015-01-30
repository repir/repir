package io.github.repir.Repository;

import io.github.repir.tools.io.buffer.BufferReaderWriter;
import io.github.repir.tools.io.Datafile;
import io.github.repir.tools.io.struct.StructuredFileSort;
import io.github.repir.tools.io.struct.StructuredFileCollision;
import io.github.repir.tools.lib.Log;
import io.github.repir.Repository.VocMem3.File;
import io.github.repir.tools.io.EOCException;
import io.github.repir.tools.lib.ByteTools;
import io.github.repir.tools.io.struct.StructuredFileCollisionRecord;
import io.github.repir.tools.io.struct.StructuredFileSortRecord;

/**
 * Fetches the internal term id for a term string. To improve lookup speed, the
 * most common terms are kept in memory, while less common terms remain on disk.
 * <p/>
 * Before requesting the internal term id, the text should be processed by the
 * same {@link Extractor} process as used for indexing.
 * {@link #get(java.lang.String)} is used to obtain the term id of a single
 * term, while {@link #getContent(Extractor.EntityChannel)} is used to obtain
 * an array of term id's to represent a multi term text.
 * <p/>
 * This implementation is limited to vocabularies of max 2^24.
 * @author jeroen
 */
public class VocMem3 extends VocabularyToIDRAM<File> {

   public static Log log = new Log(VocMem3.class);
   //public TermID termfile;

   public VocMem3(Repository repository) {
      super(repository);
   }
   
   public static VocMem3 get(Repository repository) {
       String label = canonicalName(VocMem3.class);
       VocMem3 vocmem = (VocMem3)repository.getStoredFeature(label);
       if (vocmem == null) {
          vocmem = new VocMem3(repository);
          repository.storeFeature(label, vocmem);
       }
       return vocmem;
   }
   
   @Override
   public void openRead() {
      super.openRead();
      //only need termid if the memvoc does not contain all terms
      //termfile = (TermID) repository.getStoredSystemValue("TermID");
   }

   @Override
   public void openWrite() {
      getFile().setTableSize(repository.getVocabularySize());
      file.setBufferSize(1000000);
      file.openWrite();
   }

   public int get(String term) {
      Record record = createRecord();
      record.term = term;
      Integer termid = io.github.repir.tools.lib.Const.NULLINT;
      Record found = (Record)file.find(record);
      if (found != null) {
         termid = found.id;
      }
      return termid;
   }

   @Override
   public File createFile(Datafile datafile) {
      return new File(datafile);
   }

   public Record createRecord() {
      Record record = new Record(file);
      return record;
   }

   @Override
   public void reduceInput(int id, String term, long cf, long df) {
      Record record = createRecord();
      record.id = id;
      record.term = term;
      record.cf = cf;
      record.write();
   }

   @Override
   public void startReduce(long corpustermfreq, int corpusdocumentfrequency) {
      openWrite();
   }

   @Override
   public void finishReduce() {
      closeWrite();
   }

   public static void build(Repository repository) throws EOCException {
      VocMem3 vocmem3 = VocMem3.get(repository);
      vocmem3.startReduce(0, 0);
      TermCF termtf = TermCF.get(repository);
      termtf.openRead();
      termtf.getFile().setBufferSize(10000000);
      TermString termstring = TermString.get(repository);
      termstring.getFile().openRead();
      termstring.getFile().setBufferSize(10000000);
      for (int id = 0; id < repository.getVocabularySize(); id++) {
         long cf = termtf.file.cf.read();
         String term = termstring.file.term.read();
         //log.info("%d %s %d", id, term, cf);
         vocmem3.reduceInput(id, term, cf, 0);
      }
      vocmem3.finishReduce();
   }

   public static void main(String[] args) throws EOCException {
      Repository repository = new Repository( args );
      build( repository );
   }
   
   /**
    * The terms are sorted in a collision table, based on hashcode of the term,
    * and secondary on cf desc. The memory table can contain the first 2^24
    * terms from the file (sorted on cf desc), if a term is missing it should be
    * looked up in the disk-stored term file.
    * <p/>
    * This file used during indexing, to improve the speed of converting
    * tokenized content into termID's.
    * <p/>
    * @author jeroen
    */
   public class File extends StructuredFileCollision {

      public String0Field term = this.addString0("term");
      public LongField cf = this.addLong("cf");
      public Int3Field id = this.addInt3("id");

      public File(Datafile df) {
         super(df);
      }

    @Override
    protected int spillThreshold() {
        return 1000000;
    }

      public File clone() {
         File f = new File(new Datafile(getDatafile()));
         f.setTableSize(this.getTableSize());
         return f;
      }
      
      @Override
      public void openRead() {
         this.remove(cf);
         super.openRead();
      }

      @Override
      public void openWriteFinal() {
         this.remove(cf);
         super.openWriteFinal();
      }

      @Override
      public StructuredFileSortRecord createRecord() {
         Record record = new Record(this);
         record.id = id.value;
         record.cf = cf.value;
         record.term = term.value;
         return record;
      }

      /**
       * used to reversely sort colliding entries based on cf, to slightly
       * improve performance.
       */
      @Override
      public int secondaryCompare(StructuredFileSort o1, StructuredFileSort o2) {
         return ((File) o1).cf.value > ((File) o2).cf.value ? -1 : 1;
      }

      @Override
      public int secondaryCompare(StructuredFileSortRecord o1, StructuredFileSortRecord o2) {
         return ((Record) o1).cf > ((Record) o2).cf ? -1 : 1;
      }

      /**
       * this function is called by the internal {@link #find(Content.StructuredFileCollision.SortableCollisionRecord)
       * }
       * method, which seeks the offset at which the bucketIndex is position.
       * Because this is a collision table, if a matching entry exists, it is
       * always placed after this offset, as close to the offset as allowed, but
       * there can be other entries in between. An implementing function should
       * therefore readValue until the entry is found or a hashcode is
       * encountered that is greater than the search key's hashcode. In the
       * latter case, the entry does not exist and null should be returned.
       * <p/>
       * @param table the resident table to search, with the offset pointing to
       * the
       * @param r the entry containing the term string to search for
       * @return a matching entry, with its id, or null if it doesn't exist.
       */
      @Override
      public StructuredFileCollisionRecord find(BufferReaderWriter table, StructuredFileCollisionRecord r) {
         Record rr = (Record) r;
         byte needle[] = rr.term.getBytes();
         int match;
         int offset = table.bufferpos;
         while (table.bufferpos < table.end) {
            try {
               for (match = 0; match < needle.length && table.buffer[table.bufferpos + match] == needle[match]; match++);
               if (match == needle.length && table.buffer[table.bufferpos + match] == 0) {
                  table.skipString0();
                  rr.id = table.readInt3();
                  return rr;
               }
               int bucketindex = ByteTools.string0HashCode(table.buffer, table.bufferpos, table.end) & (this.getBucketCapacity() - 1);
               if (bucketindex > rr.getBucketIndex()) {
                  break;
               }
               table.skipString0();
               table.skip(3);
            } catch (EOCException ex) {
               log.exception(ex, "find( %s, %s )", table, r);
            }
         }
         return null;
      }

   }
   
   public class Record extends StructuredFileCollisionRecord {

         public String term;
         public long cf;
         public int id;

         public Record(File file) {
            super(file);
         }

         @Override
         protected void writeRecordData() {
            ((File) file).term.write(term);
            ((File) file).id.write(id);
         }

         public int hashCode() {
            return term.hashCode();
         }
         
         @Override
         protected void writeTempRecordData() {
            ((File) file).term.write(term);
            ((File) file).cf.write(cf);
            ((File) file).id.write(id);
         }

         @Override
         public boolean equals(StructuredFileCollisionRecord r) {
            return term.equals(((Record) r).term);
         }
      }

}
