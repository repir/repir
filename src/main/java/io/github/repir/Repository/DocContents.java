package io.github.repir.Repository;

import io.github.htools.extract.Content;
import io.github.htools.hadoop.io.archivereader.RecordKey;
import io.github.htools.hadoop.io.archivereader.RecordValue;
import io.github.htools.extract.ExtractChannel;
import io.github.repir.Repository.DocContents.File;
import io.github.htools.io.Datafile;
import io.github.htools.io.Datafile.STATUS;
import io.github.htools.io.EOCException;
import io.github.htools.io.struct.StructuredFileSortHash;
import io.github.htools.io.struct.StructuredFileSortHashRecord;
import io.github.htools.io.struct.StructuredFileSortRecord;
import io.github.htools.lib.Log;
import io.github.htools.lib.PrintTools;
import io.github.htools.lib.StrTools;

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
public class DocContents extends StringLookupFeature<File, String[]> { 

   public static Log log = new Log(DocContents.class);

   private DocContents(Repository repository, String field, String key) {
      super(repository, field, key);
   }

   public static DocContents get(Repository repository, String field, String key) {
       String label = canonicalName(DocContents.class, field);
       DocContents doccontents = (DocContents)repository.getStoredFeature(label);
       if (doccontents == null) {
          doccontents = new DocContents(repository, field, key);
          repository.storeFeature(label, doccontents);
       }
       return doccontents;
   }
   
   @Override
   public void setMapOutputValue(RecordValue value, Content doc) {
      ExtractChannel attr = doc.get(entityAttribute());
      //log.info("mapOutput %s %s", entityAttribute(), attr);
      value.writer.writeStringList(attr);
   }

   @Override
   public void writeReduce(RecordKey key, Iterable<RecordValue> values) {
      try {
         RecordValue value = values.iterator().next();
         String t[] = value.reader.readStringArray();
         //log.info("reduceInput %s %s", key.collectionid, StrTools.concat(t));
         write(key.collectionid, t);
      } catch (EOCException ex) {
         log.fatal(ex);
      }
   }

   @Override
   public String[] get(String entityname) {
      if (getFile().getDatafile().status != STATUS.READ) {
         openRead();
      }
      String contents[] = null;
      Record termrecord = new Record(file);
      termrecord.entityname = entityname;
      Record termfound = (Record) termrecord.find();
      if (termfound != null) {
         contents = termfound.contents;
      } else {
         log.info("DocContents not found %s", entityname);
         log.info("file %s", file.getDatafile().getCanonicalPath());
         //log.crash();
      }
      return contents;
   }

   @Override
   public void openWrite() {
      getFile().setBufferSize(1000000);
      //file.setTableSize((int)repository.getDocumentCount());
      file.openWrite();
   }

   public void write(String entityname, String contents[]) {
      Record termrecord = new Record(file);
      termrecord.entityname = entityname;
      termrecord.contents = contents;
      termrecord.write();
   }

   @Override
   public File createFile(Datafile datafile) {
       log.info("creatFile %s %d", datafile.getCanonicalPath(), repository.getDocumentCount());
      return new File(datafile, (int)repository.getDocumentCount());
   }

   public class File extends StructuredFileSortHash {

      public String0Field entityname = this.addString0("entityname");
      public StringArrayField contents = this.addStringArray("contents");

      public File(Datafile df, int tablesize) {
         super(df, tablesize);
      }
      
    @Override
    protected int spillThreshold() {
        return 10000;
    }

      public File clone() {
         return new File( new Datafile(getDatafile()), getTableSize() );
      }

      @Override
      public StructuredFileSortRecord createRecord() {
         Record r = new Record(this);
         r.offsetread = this.recordoffset;
         r.entityname = entityname.value;
         r.contents = contents.value;
         return r;
      }

   }
   
   public class Record extends StructuredFileSortHashRecord {

         public String entityname;
         public String contents[];
         public long offsetread;
         
         public Record(File file) {
            super(file);
         }
         
         public int hashCode() {
            return entityname.toLowerCase().hashCode();
         }
         
         public String toString() {
            return PrintTools.sprintf("hash %d bucket %d entity %s offsetread %d", hashCode(), getBucketIndex(), entityname, offsetread);
         }
         
         @Override
         protected void writeRecordData() {
            //log.printf("writeRecordData() cap %d bucket %d id %d term %s offr %d offw %d", file.getBucketCapacity(), this.getBucketIndex(), id, term, offsetread, file.getOffset());
            ((File) file).entityname.write(entityname);
            ((File) file).contents.write(contents);
         }

         @Override
         public boolean equals(Object r) {
            if (r instanceof Record)
               return entityname.equalsIgnoreCase(((Record) r).entityname);
            return false;
         }
      }

}
