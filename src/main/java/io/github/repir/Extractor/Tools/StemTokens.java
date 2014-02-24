package io.github.repir.Extractor.Tools;

import java.util.ArrayList;
import io.github.repir.tools.Lib.Log;
import io.github.repir.tools.Stemmer.englishStemmer;
import io.github.repir.Extractor.Entity;
import io.github.repir.Extractor.EntityAttribute;
import io.github.repir.Extractor.Extractor;
import java.util.HashMap;
import io.github.repir.tools.DataTypes.ByteArrayPos;

/**
 * Processes all tokens in the supplied EntityAttribute though the snowball
 * (Porter 2) stemmer. This only works if the content has been processed so that
 * each word is a separate token.
 */
public class StemTokens extends ExtractorProcessor {

   private static Log log = new Log(StemTokens.class);
   englishStemmer stemmer = englishStemmer.get();
   static HashMap<String, String> translateStemmed = new HashMap<String, String>();

   public StemTokens(Extractor extractor, String process) {
      super(extractor, process);
   }

   @Override
   public void process(Entity entity, Entity.SectionPos pos, String attributename) {
      //log.fatal("process channel %s %d", channel.channel, channel.size());
      EntityAttribute attribute = entity.get(attributename);
      for (int c = 0; c < attribute.size(); c++) {
         String chunk = attribute.get(c);
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