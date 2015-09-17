package io.github.repir.Repository.Stopwords;

import io.github.htools.words.StopWordsSmart;
import io.github.htools.words.StopWordsLetter;
import io.github.htools.lib.Log;
import java.util.HashSet;
import io.github.repir.Repository.Repository;
import io.github.htools.extract.DefaultTokenizer;
import java.util.ArrayList;

/**
 * List of stop words, which is not stored as a feature, but rather configured.
 *
 * @author jeroen
 */
public class StopWords extends io.github.htools.words.StopWords {

    public static Log log = new Log(StopWords.class);
    private static HashSet<Integer> intfilterset;
    private Repository repository;

    private StopWords(Repository r) {
        super(r.getConf());
        repository = r;
    }

    public static StopWords get(Repository r) {
        if (singleton == null || 
                !(singleton instanceof StopWords) || 
                ((StopWords)singleton).repository != r) {
            singleton = new StopWords(r);
        }
        return (StopWords)singleton;
    }

    public HashSet<Integer> getIntSet() {
        if (intfilterset == null) {
            intfilterset = new HashSet<Integer>();
            for (String t : this.getStemmedFilterSet()) {
                int i = repository.termToID(t);
                if (i >= 0) {
                    intfilterset.add(i);
                }
            }
        }
        return intfilterset;
    }
}
