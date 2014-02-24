package io.github.repir.Strategy.Tools;

import io.github.repir.tools.Lib.Log;
import io.github.repir.tools.Stemmer.englishStemmer;
import java.util.HashSet;

/**
 * Stop word list of 429 terms from http://www.lextek.com/manuals/onix/stopwords1.html
 * which is the original list of stop words Salton & Buckley orginally used for
 * the SMART system at Cornell University, which was slightly trimmed down.
 */
public class StopWordsLetter {
   public static Log log = new Log( StopWordsLetter.class );
   
   public static String filterarray[] = {
      // removed "us", because we tokenize U.S. as us.
      "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", 
      "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z"
   };

   public static HashSet<String> getUnstemmedFilterSet() {
      HashSet<String> set = new HashSet<String>();
      for (String s : filterarray) {
         set.add(s);
      }
      return set;
   }
}
