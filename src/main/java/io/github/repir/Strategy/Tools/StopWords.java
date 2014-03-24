package io.github.repir.Strategy.Tools;

import io.github.repir.tools.Lib.Log;
import java.util.HashSet;
import io.github.repir.Repository.Repository;
import io.github.repir.tools.Stemmer.englishStemmer;

/**
 * List of stop words, which is not stored as a feature, but rather configured.
 * @author jeroen
 */
public class StopWords {
   public static Log log = new Log( StopWords.class );
   public static StopWords singleton;
   public HashSet<String> unstemmedfilterset = getUnstemmedFilterSet();
   public HashSet<String> stemmedfilterset = getStemmedFilterSet();
   private HashSet<Integer> intfilterset;
   private Repository repository;
   
   private StopWords( Repository r ) {
      repository = r;
      for (String s : r.getConfigurationSubStrings("retriever.stopword")) {
         unstemmedfilterset.add(s);
      }
   }
   
   public static StopWords get(Repository r) {
      if (singleton == null || singleton.repository != r)
         singleton = new StopWords(r);
      return singleton;
   }
   
   public void addNumbers() {
      for (int i = 0 ; i< 10; i++) {
         String n = "" + i;
         unstemmedfilterset.add(n);
         stemmedfilterset.add(n);
      }
   }
   
   public HashSet<String> getStemmedFilterSet() {
      englishStemmer stemmer = new englishStemmer();
      HashSet<String> set = new HashSet<String>();
      for (String s : this.unstemmedfilterset) {
         set.add(stemmer.stem(s));
      }
      return set;
   }
   
   public HashSet<String> getUnstemmedFilterSet() {
      HashSet<String> set = new HashSet<String>();
      //set.addAll(StopWordsInQuery.getUnstemmedFilterSet());
      set.addAll(StopWordsSmart.getUnstemmedFilterSet());
      set.addAll(StopWordsLetter.getUnstemmedFilterSet());
      //set.addAll(StopWordsContractions.getUnstemmedFilterSet());
      //set.addAll(StopWordsUrl.getUnstemmedFilterSet());   
      return set;
   }
   
   public boolean isUnstemmedStopWord(String s) {
      return unstemmedfilterset.contains(s);
   }
      
   public boolean isStemmedStopWord(String s) {
      return stemmedfilterset.contains(s);
   }
   
   public HashSet<Integer> getIntSet( ) {
      if (intfilterset == null) {
         intfilterset = new HashSet<Integer>();
         for (String t : stemmedfilterset) {
            int i = repository.termToID(t);
            if (i >= 0)
               intfilterset.add(i);
         }
      }
      return intfilterset;
   }
}
