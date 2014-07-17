package io.github.repir.EntityReader;

import io.github.repir.EntityReader.MapReduce.EntityWritable;
import io.github.repir.tools.ByteSearch.ByteSearch;
import io.github.repir.tools.Content.EOCException;
import io.github.repir.tools.Content.HDFSIn;
import io.github.repir.tools.Lib.Log;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;

/**
 * An implementation of EntityReader that scans the input for TREC style
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
public class EntityReaderTrec extends EntityReader {

   public static Log log = new Log(EntityReaderTrec.class);
   private ByteSearch zickel = ByteSearch.create("zickel");
   private byte[] startTag;
   private byte[] endTag;

   @Override
   public void initialize(FileSplit fileSplit) {
      startTag = conf.get("entityreader.entitystart", "<DOC>").getBytes();
      endTag = conf.get("entityreader.entityend", "</DOC>").getBytes();
      Path file = fileSplit.getPath();
      if (end < HDFSIn.getLengthNoExc(filesystem, file)) { // only works for uncompressed files
         fsin.setCeiling(end);
      }
   }

   @Override
   public boolean nextKeyValue() {
      if (fsin.hasMore()) {
         if (readUntilStart() && fsin.getOffset() - startTag.length < fsin.getCeiling()) {
            key.set(fsin.getOffset());
            if (readEntity()) {
               entitywritable.entity.addSectionPos("all", 0, 0, entitywritable.entity.content.length, entitywritable.entity.content.length);
               return true;
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
         } catch (EOCException ex) {
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
         } catch (EOCException ex) {
            return false;
         }
      }
   }
}