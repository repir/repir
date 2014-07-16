package io.github.repir.Extractor.Tools;

import io.github.repir.tools.ByteSearch.ByteRegex;
import io.github.repir.tools.ByteSearch.ByteSearchPosition;
import io.github.repir.EntityReader.Entity;
import io.github.repir.EntityReader.EntityRemovedException;
import io.github.repir.Extractor.Extractor;
import io.github.repir.tools.Lib.Log;

/**
 * Removes redirect pages from Wikipedia XML source.
 * <p/>
 * @author jeroen
 */
public class RemoveRedirect extends ExtractorProcessor {

   public static Log log = new Log(RemoveRedirect.class);
   public ByteRegex start = new ByteRegex("<redirect\\s+title\\s*=");

   private RemoveRedirect(Extractor extractor, String process) {
      super(extractor, process);
   }

   @Override
   public void process(Entity entity, Entity.Section section, String attribute) throws EntityRemovedException {
      if (start.match(entity.content, section.open, section.close))
         throw new EntityRemovedException();
   }
}
