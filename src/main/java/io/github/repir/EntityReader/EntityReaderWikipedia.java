package io.github.repir.EntityReader;

import java.io.EOFException;
import io.github.repir.tools.Content.HDFSIn;
import io.github.repir.Extractor.Entity;
import io.github.repir.tools.Lib.Log;
import java.io.IOException;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;
import io.github.repir.tools.Lib.ByteTools;

/**
 * An implementation of IREFEntityReader that scans the input for TREC style
 * documents, that are enclosed in <DOC></DOC> tags. The used tags may be
 * overridden by setting different tags in entityreader.entitystart and
 * entityreader.entityend.
 * <p/>
 * NOTE that the original TREC disks contain .z files, which cannot be
 * decompressed by Java. The files must therefore be decompressed outside this
 * framework.
 * <p/>
 * @author jeroen
 */
public class EntityReaderWikipedia extends IREFEntityReader {

   public static Log log = new Log(EntityReaderWikipedia.class);
   private byte[] startTag;
   private byte[] endTag;
   private byte[] idStart = "<id>".getBytes();
   private byte[] idEnd = "</id>".getBytes();
   private byte[] titleStart = "<title>".getBytes();
   private byte[] titleEnd = "</title>".getBytes();
   private byte[] bodyStart = "<text".getBytes();
   private byte[] bodyStartEnd = ">".getBytes();
   private byte[] bodyEnd = "</text>".getBytes();

   @Override
   public void initialize(FileSplit fileSplit) {
      startTag = conf.getSubString("entityreader.entitystart", "<page>").getBytes();
      endTag = conf.getSubString("entityreader.entityend", "</page>").getBytes();
      Path file = fileSplit.getPath();
      if (end < HDFSIn.getLength(filesystem, file)) { // only works for uncompressed files
         fsin.setCeiling(end);
      }
   }

   @Override
   public boolean nextKeyValue() {
      if (fsin.hasMore()) {
         if (readUntilStart() && fsin.getOffset() - startTag.length < fsin.getCeiling()) {
            key.set(fsin.getOffset());
            if (readEntity()) {
               int p = ByteTools.find(entitywritable.entity.content, bodyStart, 0, entitywritable.entity.content.length, false, false);
               if (p > 0) {
                  p = ByteTools.find(entitywritable.entity.content, bodyStartEnd, p + bodyStart.length, entitywritable.entity.content.length, false, false);
                  if (p > 0) {
                     // check for redirect page
                     int end = ByteTools.find(entitywritable.entity.content, bodyEnd, p + bodyStartEnd.length, entitywritable.entity.content.length, false, false);
                     if (end > p) {
                        String id = ByteTools.extract(entitywritable.entity.content, idStart, idEnd, 0, entitywritable.entity.content.length, false, false);
                        String title = ByteTools.extract(entitywritable.entity.content, titleStart, titleEnd, 0, entitywritable.entity.content.length, false, false);
                        entitywritable.entity.addSectionPos("all", p + 1, p + 1, end, end);
                        entitywritable.entity.get("literaltitle").add(title);
                        entitywritable.entity.get("collectionid").add(id);
                        return true;
                     }
                  }
               }
            }
         }
      }
      return false;
   }

   private boolean readEntity() {
      entitywritable = new EntityWritable();
      entitywritable.entity = new Entity();
      int needleposition = 0;
      while (true) {
         try {
            int b = fsin.readByte();
            if (b != endTag[needleposition]) { // check if we match needle
               if (needleposition > 0) {
                  entitywritable.writeBytes(endTag, 0, needleposition);
                  needleposition = 0;
               }
            }
            if (b == endTag[needleposition]) {
               needleposition++;
               if (needleposition >= endTag.length) {
                  entitywritable.storeContent();
                  return true;
               }
            } else {
               entitywritable.writeByte(b);

//               if (needleposition == 0 && !fsin.hasMore()) {  // see if we've passed the stop point:
//                  return false;
//               }
            }
         } catch (EOFException ex) {
            return false;
         }
      }
   }

   private boolean readUntilStart() {
      int needleposition = 0;
      while (true) {
         try {
            int b = fsin.readByte();
            if (b != startTag[needleposition]) { // check if we match needle
               needleposition = 0;
            }
            if (b == startTag[needleposition]) {
               needleposition++;
               if (needleposition >= startTag.length) {
                  return true;
               }
            } else {
               if (needleposition == 0 && !fsin.hasMore()) {  // see if we've passed the stop point:
                  return false;
               }
            }
         } catch (EOFException ex) {
            return false;
         }
      }
   }
}
