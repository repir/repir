package io.github.repir.EntityReader;

import io.github.repir.tools.Content.Datafile;
import io.github.repir.Extractor.Entity;
import io.github.repir.tools.Lib.ByteTools;
import io.github.repir.tools.Lib.Log;
import java.io.EOFException;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;
import io.github.repir.tools.ByteRegex.ByteRegex;
import io.github.repir.tools.Lib.ConfTool;

/**
 * An implementation of IREFEntityReader that reads the ClueWeb09 collection. In
 * this collection, each document is preceded by a WARC header, which contains
 * document metadata and length. Each document has a WarcIDTag of 25 characters,
 * which s used to validate Warc headers of correct documents.
 * <p/>
 * Different from the TREC disks, the .gz format used for ClueWeb09 can be
 * decompressed by Java. So the disks can be read in its original format. A
 * technical problem with reading compressed files is that there is no way to
 * determine the distance to end-of-file. The file must therefore be read until
 * and EOF exception is encountered. The Datafile class will correctly throw an
 * EOF. However, after EOF has been reached, no further attempts should be made
 * reading the file, by checking {@link Datafile#hasMore()} first.
 * <p/>
 * Notorious for the amount of spam contained in the documents, the Fusion
 * spamlist provided by the University of Waterloo can be used to limit the
 * documents processed to a percentage of the spamlist. For this,
 * repository.spamlist should point to the directory that contains the spamlist
 * files, and repository.spamthreshold controls the percentage of spammiest
 * documents left out (i.e. 10 leaves out the spammiest 10%).
 * <p/>
 * @author jeroen
 */
public class EntityReaderCW12 extends IREFEntityReader {

   public static Log log = new Log(EntityReaderCW12.class);
   private byte[] warcTag = "WARC/1.0".getBytes();
   private byte[] contentlengthtag = "\nContent-Length: ".getBytes();
   private byte[] doctype = "<!DOCTYPE".getBytes();
   ByteRegex WarcIDTag = new ByteRegex("WARC\\-TREC\\-ID\\:\\s*");
   ByteRegex EOL = new ByteRegex("\\s");
   private byte[] warcIDTag = "WARC-TREC-ID: ".getBytes();
   private byte[] eol = "\n".getBytes();
   private idlist ids;

   @Override
   public void initialize(FileSplit fileSplit) {
      Path file = fileSplit.getPath();
      String directory = getDir(file);
      String idlist = conf.getSubString("repository.idlist", null);
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
         ByteRegex.Pos find = WarcIDTag.find(entitywritable.entity.content, 0, entitywritable.entity.content.length);
         if (find.found()) {
            ByteRegex.Pos find1 = EOL.find(entitywritable.entity.content, find.end, entitywritable.entity.content.length);
            if (find1.found()) {
               String id = new String(entitywritable.entity.content, find.end, find1.start - find.end);
               //log.info("entity %s", new String(entitywritable.entity.content));
               if (id.length() == 25 && (ids == null || ids.get(id))) {
                  log.info("id %s", id);
                  entitywritable.entity.get("collectionid").add(id);
                  int recordlength = getLength(pos);
                  if (recordlength > 0) {
                     int warcheaderend = pos.endpos;
                     int startdoctype = io.github.repir.tools.Lib.ByteTools.find(entitywritable.entity.content, doctype, pos.startpos, entitywritable.entity.content.length - pos.startpos, false, false);
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
         }
      }
      return false;
   }

   private int getLength(Position pos) {
      int lengthstart = io.github.repir.tools.Lib.ByteTools.find(entitywritable.entity.content, contentlengthtag, pos.startpos, entitywritable.entity.content.length - pos.startpos, false, false);
      if (lengthstart >= 0) {
         pos.startpos = lengthstart + contentlengthtag.length;
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
         } catch (EOFException ex) {
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
