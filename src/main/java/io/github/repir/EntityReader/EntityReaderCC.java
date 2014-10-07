package io.github.repir.EntityReader;

import io.github.repir.Extractor.Entity;
import io.github.repir.EntityReader.MapReduce.EntityWritable;
import io.github.repir.tools.ByteSearch.ByteSearch;
import io.github.repir.tools.ByteSearch.ByteSearchPosition;
import io.github.repir.tools.ByteSearch.ByteSearchSection;
import io.github.repir.tools.ByteSearch.ByteSection;
import io.github.repir.tools.Content.EOCException;
import io.github.repir.tools.Lib.Log;
import java.util.ArrayList;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;

/**
 * An implementation of EntityReader that reads the CommonCrawl WARC files,
 * similar to {@link EntityReaderCW}.
 * <p/>
 * @author jeroen
 */
public class EntityReaderCC extends EntityReader {

   public static Log log = new Log(EntityReaderCC.class);
   private byte[] warcTag = "WARC/1.0".getBytes();
   ByteSection domainregex = new ByteSection("WARC\\-Target\\-URI\\:\\s*\\c+://+([^/@:]*(:[^/@]*)?@)?", "(/|\\s)");
   ByteSearch newSection = ByteSearch.create("\\n\\n");
   ByteSection warcType = new ByteSection("Content-Type:\\s*", "\\s");

   @Override
   public void initialize(FileSplit fileSplit) {
      Path file = fileSplit.getPath();
   }

   @Override
   public boolean nextKeyValue() {
      while (fsin.hasMore()) {
         readEntity();
         ArrayList<ByteSearchPosition> sectionbreaks = newSection.findPos(entitywritable.entity.content, 0, entitywritable.entity.content.length, 2);
         if (sectionbreaks.size() == 2) {
            ByteSearchSection typesection = warcType.findPos(entitywritable.entity.content, sectionbreaks.get(0).end, entitywritable.entity.content.length);
            if (sectionbreaks.get(1).end < entitywritable.entity.content.length) {
               if (typesection.found() && typesection.toString().startsWith("text")) {
                  ByteSearchSection domainsection = domainregex.findPos(entitywritable.entity.content, 0, entitywritable.entity.content.length);
                  if (domainsection.found()) {
                     String domain = domainsection.toString();
                     if (domain.contains(".")) {
                        log.info("domain %s", domain);
                        entitywritable.entity.get("domain").add(domain);
                        entitywritable.entity.addSectionPos("all", 0, sectionbreaks.get(1).end, entitywritable.entity.content.length, entitywritable.entity.content.length);
                        return true;
                     }
                  }
               }
            }
         }
      }
      return false;
   }

   private void readEntity() {
      entitywritable = new EntityWritable();
      entitywritable.entity = new Entity();
      key.set(fsin.getOffset());
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
}
