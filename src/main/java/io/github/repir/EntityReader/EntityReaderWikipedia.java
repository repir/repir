package io.github.repir.EntityReader;

import io.github.repir.EntityReader.MapReduce.EntityWritable;
import io.github.repir.tools.Content.EOCException;
import io.github.repir.tools.Content.HDFSIn;
import io.github.repir.tools.Lib.Log;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;
import io.github.repir.tools.Lib.ByteTools;

/**
 * An implementation of EntityReader that scans the input for Wikipedia XML
 * dumps, that are enclosed in <page></page> tags.
 * <p/>
 * @author jeroen
 */
public class EntityReaderWikipedia extends EntityReader {

   public static Log log = new Log(EntityReaderWikipedia.class);
   private byte[] startTag;
   private byte[] endTag;
   private byte[] idStart = "<id>".getBytes();
   private byte[] idEnd = "</id>".getBytes();
   private byte[] titleStart = "<title>".getBytes();
   private byte[] titleEnd = "</title>".getBytes();
   private byte[] redirect = "<redirect ".getBytes();
   private byte[] nsStart = "<ns>".getBytes();
   private byte[] nsEnd = "</ns>".getBytes();
   private byte[] bodyStart = "<text".getBytes();
   private byte[] bodyStartEnd = ">".getBytes();
   private byte[] bodyEnd = "</text>".getBytes();

   @Override
   public void initialize(FileSplit fileSplit) {
      startTag = conf.get("entityreader.entitystart", "<page>").getBytes();
      endTag = conf.get("entityreader.entityend", "</page>").getBytes();
      Path file = fileSplit.getPath();
      if (end < HDFSIn.getLengthNoExc(filesystem, file)) { // only works for uncompressed files
         fsin.setCeiling(end);
      }
   }

   @Override
   public boolean nextKeyValue() {
      while (fsin.hasMore()) {
         if (readUntilStart() && fsin.getOffset() - startTag.length < fsin.getCeiling()) {
            key.set(fsin.getOffset());
            if (readEntity()) {
               int starttext = ByteTools.find(entitywritable.entity.content, bodyStart, 0, entitywritable.entity.content.length, false, false);
               if (starttext > 0) {
                  starttext = ByteTools.find(entitywritable.entity.content, bodyStartEnd, starttext + bodyStart.length, entitywritable.entity.content.length, false, false);
                  if (starttext > 0) {
                     // check for redirect page
                     int endtext = ByteTools.find(entitywritable.entity.content, bodyEnd, starttext + bodyStartEnd.length, entitywritable.entity.content.length, false, false);
                     if (endtext > starttext) {
                        int redirectpos = ByteTools.find(entitywritable.entity.content, redirect, 0, starttext, false, false);
                        if (redirectpos < 0) {
                           String ns = ByteTools.extract(entitywritable.entity.content, nsStart, nsEnd, 0, entitywritable.entity.content.length, false, false);
                           if (ns.trim().equals("0")) {
                              String id = ByteTools.extract(entitywritable.entity.content, idStart, idEnd, 0, starttext, false, false);
                              String title = ByteTools.extract(entitywritable.entity.content, titleStart, titleEnd, 0, starttext, false, false);
                              //log.info("id %s title %s ns %s", id, title, ns); 
                              entitywritable.entity.addSectionPos("all", starttext + 1, starttext + 1, endtext, endtext);
                              entitywritable.entity.get("literaltitle").add(title);
                              entitywritable.entity.get("collectionid").add(id);
                              return true;
                           }
                        }
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
