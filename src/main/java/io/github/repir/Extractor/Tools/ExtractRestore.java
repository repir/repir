package io.github.repir.Extractor.Tools;

import io.github.repir.Extractor.Entity;
import io.github.repir.Extractor.Entity.SectionPos;
import io.github.repir.Extractor.Extractor;
import io.github.repir.tools.Lib.Log;
import java.util.ArrayList;

/**
 * Extract HTML Metadata for the keywords and description field. This data is
 * also added to the 'all' field.
 * <p/>
 * @author jbpvuurens
 */
public class ExtractRestore extends ExtractorProcessor {

   public static Log log = new Log(ExtractRestore.class);
   public static ArrayList<StoredContent> content = new ArrayList<StoredContent>();

   public ExtractRestore(Extractor extractor, String process) {
      super(extractor, process);
   }

   @Override
   public void process(Entity entity, SectionPos section, String attribute) {
      restore(entity);
      content = new ArrayList<StoredContent>();
   }

   public static void add(Entity entity, int start, int end) {
      content.add(new StoredContent(entity.content, start, end));
   }

   public void restore(Entity entity) {
      for (StoredContent sc : content) {
         sc.restore(entity.content);
      }
   }

   static class StoredContent {

      int pos;
      byte content[];

      public StoredContent(byte buffer[], int start, int end) {
         content = new byte[end - start];
         System.arraycopy(buffer, start, content, 0, end - start);
         pos = start;
      }

      public void restore(byte buffer[]) {
         System.arraycopy(content, 0, buffer, pos, content.length);
         if (buffer[pos - 1] == 0) {
            buffer[pos - 1] = 32;
         }
         if (buffer[ pos + content.length] == 0) {
            buffer[pos + content.length] = 32;
         }
      }
   }
}
