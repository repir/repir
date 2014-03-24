package io.github.repir.Extractor.Tools;

import java.util.ArrayList;
import io.github.repir.tools.Lib.Log;
import io.github.repir.tools.Stemmer.englishStemmer;
import io.github.repir.EntityReader.Entity;
import io.github.repir.Extractor.EntityChannel;
import io.github.repir.Extractor.Extractor;
import java.util.HashMap;
import io.github.repir.tools.DataTypes.ByteArrayPos;

/**
 * Query specific stemmer, that ignores words that precede a colon (:),
 * because this is a Java Class name in RR syntax.
 */
public class StemTokensQuery extends ExtractorProcessor {

   private static Log log = new Log(StemTokensQuery.class);
   englishStemmer stemmer = englishStemmer.get();
   static HashMap<String, String> translateStemmed = new HashMap<String, String>();

   public StemTokensQuery(Extractor extractor, String process) {
      super(extractor, process);
   }

   @Override
   public void process(Entity entity, Entity.Section pos, String attributename) {
      //log.fatal("process channel %s %d", channel.channel, channel.size());
      EntityChannel attribute = entity.get(attributename);
      for (int c = 0; c < attribute.size(); c++) {
         String chunk = attribute.get(c);
         if (!chunk.contains(":")) {
            String stem = translateStemmed.get(chunk);
            if (stem == null) {
               stem = stemmer.stem(chunk);
               translateStemmed.put(chunk, stem);
               //log.info("stem %s %s", chunk, stem);
            }
            attribute.set(c, stem);
         }
      }
   }
}