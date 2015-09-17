package io.github.repir.Repository;

import io.github.htools.io.struct.StructuredFile;
import io.github.htools.lib.Log;

/**
 * This class can convert content that is described as an array of TermID's
 * to an array of string tokens.
 * @author jer
 * @param <F> 
 */
public abstract class VocabularyToString<F extends StructuredFile> extends StoredUnreportableFeature<F> implements DictionaryFeature {

   public static Log log = new Log(VocabularyToString.class);

   protected VocabularyToString(Repository repository) {
      super(repository);
   }

   public abstract String readValue(int id);

   public String[] getContent(int keys[]) {
      String r[] = new String[keys.length];
      for (int i = 0; i < keys.length; i++) {
         r[i] = readValue(keys[i]);
      }
      return r;
   }

   public String getContentStr(int keys[], int startpos, int length) {
      //log.info("content %s", ArrayTools.toString(keys));
      StringBuilder r = new StringBuilder();
      length = startpos + length;
      for (int i = startpos; i < length; i++) {
         r.append(" ").append(readValue(keys[i]));
      }
      return r.length() > 0 ? r.substring(1) : "";
   }

   public String getContentStr(int keys[]) {
      return getContentStr(keys, 0, keys.length);
   }
}
