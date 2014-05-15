package io.github.repir.Extractor.Tools;

import io.github.repir.tools.Lib.Log;
import io.github.repir.EntityReader.Entity;
import io.github.repir.Extractor.Extractor;

/**
 * This is used to remove chars from test set queries, that are configured using
 * "extractor.rrtestset.removechars=".
 * <p/>
 * @author jbpvuurens
 */
public class RemoveQueryChars extends ExtractorProcessor {

   public static Log log = new Log(RemoveQueryChars.class);
   public boolean removechars[];

   public RemoveQueryChars(Extractor extractor, String process) {
      super(extractor, process);
      removechars = io.github.repir.tools.Lib.ByteTools.getByteArray( extractor.getConfigurationString(process, "removechars", "") );
   }

   public void setChars(String chars) {
      removechars = io.github.repir.tools.Lib.ByteTools.getByteArray( chars );
   }
   
   @Override
   public void process(Entity entity, Entity.Section section, String attribute) {
      byte buffer[] = entity.content;
      for (int i = section.open; i < section.close; i++) {
         if ( removechars[ buffer[i] ] )
            buffer[i] = 32;
      }
   }
}
