package io.github.repir.EntityReader;

import io.github.repir.tools.Extractor.Entity;
import io.github.repir.EntityReader.MapReduce.EntityWritable;
import io.github.repir.tools.Content.Datafile;
import io.github.repir.tools.Lib.ByteTools;
import io.github.repir.tools.Lib.Log;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;
import io.github.repir.tools.ByteSearch.ByteRegex;
import io.github.repir.tools.ByteSearch.ByteSearch;
import io.github.repir.tools.Content.EOCException;
import io.github.repir.tools.ByteSearch.ByteSearchString;
import io.github.repir.tools.ByteSearch.ByteSection;

/**
 * An implementation of EntityReader that reads the ClueWeb12 collection, 
 * similar to {@link EntityReaderCW}, just some differences in Record structure.
 * <p/>
 * @author jeroen
 */
public class EntityReaderCW122 extends EntityReader {

   public static Log log = new Log(EntityReaderCW122.class);
   private byte[] warcTag = "WARC/1.0".getBytes();
   private ByteSearch doctype = ByteSearch.create("<!DOCTYPE");
   private ByteSearch endtag = ByteSearch.create(">");
   private ByteSearch contentlength = ByteSearch.create("\nContent-Length: ");
   private ByteSearch warcIDTag = ByteSearch.create("WARC-TREC-ID: ");
   private ByteSearch eol = ByteSearch.create("\n");
   private ByteSection warcID = new ByteSection(warcIDTag, eol); 
   private idlist ids;

   @Override
   public void initialize(FileSplit fileSplit) {
      Path file = fileSplit.getPath();
      String directory = getDir(file);
      String idlist = conf.get("repository.idlist", null);
      if (idlist != null) {
         ids = SubSetFile.getIdList(new Datafile(filesystem, idlist + "/" + directory + ".idlist"));
      }
      readEntity(); // skip the first warc tag, isn't a document
   }

   @Override
   public boolean nextKeyValue() {
      while (fsin.hasMore()) {
         readEntity();
         Position pos = new Position();
         String id = warcID.getFirstString(entitywritable.entity.content, 0, entitywritable.entity.content.length);

               if (id.length() == 25 && (ids == null || ids.get(id))) {
                  //log.info("id %s", id);
                  entitywritable.entity.get("collectionid").add(id);
                  int recordlength = getLength(pos);
                  if (recordlength > 0) {
                     int warcheaderend = pos.endpos;
                     int startdoctype = doctype.find(entitywritable.entity.content, pos.startpos, entitywritable.entity.content.length);
                     if (startdoctype > 0) {
                        int enddoctype = 1 + ByteTools.find(entitywritable.entity.content, (byte) '>', startdoctype, entitywritable.entity.content.length);
                        entitywritable.entity.addSectionPos("warcheader", 0, 0, warcheaderend, warcheaderend);
                        entitywritable.entity.addSectionPos("all", enddoctype, enddoctype, entitywritable.entity.content.length, entitywritable.entity.content.length);
                     }
                  }
                  key.set(fsin.getOffset());
                  return true;
               }
  
      }
      return false;
   }

   private int getLength(Position pos) {
      int lengthend = contentlength.findEnd(entitywritable.entity.content, pos.startpos, entitywritable.entity.content.length - pos.startpos);
      if (lengthend >= 0) {
         pos.startpos = lengthend;
         pos.endpos = ByteTools.find(entitywritable.entity.content, (byte) '\n', pos.startpos, entitywritable.entity.content.length);
         if (pos.endpos > pos.startpos) {
            String length = new String(entitywritable.entity.content, pos.startpos, pos.endpos - pos.startpos).trim();
            if (Character.isDigit(length.charAt(0))) {
               return Integer.parseInt(length);
            }
         }
      }
      return -1;
   }

   private void readEntity() {
      int p = 0;
      entitywritable = new EntityWritable();
      entitywritable.entity = new Entity();
      int match = 0;
      while (true) {
         try {
            int b = fsin.readByte();
            if (match > 0 && b != warcTag[match]) { // output falsely cached chars
               entitywritable.writeBytes(warcTag, 0, match);
               match = 0;
            }
            if (b == warcTag[match]) { // check if we're matching needle
               match++;
               if (match >= warcTag.length) {
                  break;
               }
            } else {
               entitywritable.writeByte(b);
            }
         } catch (EOCException ex) {
            entitywritable.writeBytes(warcTag, 0, match);
            break;
         }
      }
      entitywritable.storeContent();
   }

   public String getDir(Path p) {
      String file = p.toString();
      int pos = file.lastIndexOf('/');
      int pos2 = file.lastIndexOf('/', pos - 1);
      if (pos < 0 || pos2 < 0) {
         log.fatal("illegal path %s", file);
      }
      return file.substring(pos2 + 1, pos);
   }

   class Position {

      int startpos;
      int endpos;
   }
}
