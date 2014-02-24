package io.github.repir.Repository;

import io.github.repir.tools.Content.BufferReaderWriter;
import io.github.repir.tools.Content.Datafile;
import io.github.repir.tools.Content.RecordBinary;
import io.github.repir.tools.Content.RecordSort;
import io.github.repir.tools.Content.RecordSortCollision;
import io.github.repir.tools.Lib.Log;
import io.github.repir.Extractor.EntityAttribute;
import io.github.repir.Repository.VocMem3.File;
import io.github.repir.tools.Lib.ByteTools;
import io.github.repir.tools.Lib.HDTools;
import java.io.EOFException;
import java.util.ArrayList;
import io.github.repir.tools.Content.RecordSortCollisionRecord;
import io.github.repir.tools.Content.RecordSortRecord;
import io.github.repir.tools.DataTypes.Configuration;

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
public class VocMem3 extends VocabularyToIDRAM<File> {

   public static Log log = new Log(VocMem3.class);
   //public TermID termfile;

   public VocMem3(Repository repository) {
      super(repository);
   }
   
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
      Integer termid = io.github.repir.tools.Lib.Const.NULLINT;
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
   public void reduceInput(int id, String term, long tf, long df) {
      Record record = createRecord();
      record.id = id;
      record.term = term;
      record.tf = tf;
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

   @Override
   public void setBufferSize(int size) {
      throw new UnsupportedOperationException("VocMem is intended to be read into memory");
   }

   public static void build(Repository repository) throws EOFException {
      VocMem3 vocmem3 = (VocMem3) repository.getFeature("VocMem3");
      vocmem3.startReduce(0, 0);
      TermTF termtf = (TermTF)repository.getFeature("TermTF");
      termtf.openRead();
      termtf.setBufferSize(10000000);
      TermString termstring = (TermString)repository.getFeature("TermString");
      termstring.getFile().openRead();
      termstring.setBufferSize(10000000);
      for (int id = 0; id < repository.getVocabularySize(); id++) {
         long tf = termtf.file.tf.read();
         String term = termstring.file.term.read();
         //log.info("%d %s %d", id, term, tf);
         vocmem3.reduceInput(id, term, tf, 0);
      }
      vocmem3.finishReduce();
   }

   public static void main(String[] args) throws EOFException {
      Configuration conf = HDTools.readConfig(args[0]);
      Repository repository = new Repository( conf );
      build( repository );
   }
   
   /**
    * The terms are sorted in a collision table, based on hashcode of the term,
    * and secondary on tf desc. The memory table can contain the first 2^24
    * terms from the file (sorted on tf desc), if a term is missing it should be
    * looked up in the disk-stored term file.
    * <p/>
    * This file used during indexing, to improve the speed of converting
    * tokenized content into termID's.
    * <p/>
    * @author jeroen
    */
   public class File extends RecordSortCollision {

      public String0Field term = this.addString0("term");
      public LongField tf = this.addLong("tf");
      public Int3Field id = this.addInt3("id");

      public File(Datafile df) {
         super(df);
      }

      public File clone() {
         File f = new File(new Datafile(datafile));
         f.setTableSize(this.getTableSize());
         return f;
      }
      
      @Override
      public void openRead() {
         this.remove(tf);
         super.openRead();
      }

      @Override
      public void openWriteFinal() {
         this.remove(tf);
         super.openWriteFinal();
      }

      @Override
      public RecordSortRecord createRecord() {
         Record record = new Record(this);
         record.id = id.value;
         record.tf = tf.value;
         record.term = term.value;
         return record;
      }

      /**
       * used to reversely sort colliding entries based on tf, to slightly
       * improve performance.
       */
      @Override
      public int secondaryCompare(RecordSort o1, RecordSort o2) {
         return ((File) o1).tf.value > ((File) o2).tf.value ? -1 : 1;
      }

      @Override
      public int secondaryCompare(RecordSortRecord o1, RecordSortRecord o2) {
         return ((Record) o1).tf > ((Record) o2).tf ? -1 : 1;
      }

      /**
       * this function is called by the internal {@link #find(Content.RecordSortCollision.SortableCollisionRecord)
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
      public RecordSortCollisionRecord find(BufferReaderWriter table, RecordSortCollisionRecord r) {
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
            } catch (EOFException ex) {
               log.exception(ex, "find( %s, %s )", table, r);
            }
         }
         return null;
      }

   }
   
   public class Record extends RecordSortCollisionRecord {

         public String term;
         public long tf;
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
            ((File) file).tf.write(tf);
            ((File) file).id.write(id);
         }

         @Override
         public boolean equals(RecordSortCollisionRecord r) {
            return term.equals(((Record) r).term);
         }
      }

}
