package io.github.repir.Repository.Stopwords;
import io.github.repir.Repository.Repository;
import java.util.ArrayList;
import java.util.HashSet;
import io.github.repir.Repository.Stopwords.StopwordsCache.File;
import io.github.repir.Repository.StoredFeature;
import io.github.htools.io.Datafile;
import io.github.htools.io.EOCException;
import io.github.htools.io.struct.StructuredFile;
import io.github.htools.lib.Log; 

/**
 * This feature caches the configured list of stop words as a list of TermID
 * integers, for fast access.
 * @author Jeroen Vuurens
 */
public class StopwordsCache extends StoredFeature<File> {
  public static Log log = new Log( StopwordsCache.class );
  File file;
  HashSet<Integer> stopwords;

  public StopwordsCache( Repository repository ) {
     super( repository );
  }

   public static StopwordsCache get(Repository repository) {
       String label = canonicalName(StopwordsCache.class);
       StopwordsCache stopwordscache = (StopwordsCache)StoredFeature.getStoredFeature(repository, label);
       if (stopwordscache == null) {
          stopwordscache = new StopwordsCache(repository);
          StoredFeature.storeFeature(repository, label, stopwordscache);
       }
       return stopwordscache;
   }
   
   @Override
   public File getFile() {
      if (file == null)
         file = new File( getStoredFeatureFile() );
      return file;
   }

   @Override
   public void openRead() {
      getFile().openRead();
      stopwords = new HashSet<Integer>();
      if (file.getDatafile().exists() && file.hasNext()) {
         try {
            stopwords = new HashSet<Integer>( file.stopwords.readArrayList() );
         } catch (EOCException ex) {
         }
      }
      file.closeRead();
   }

   public void write() {
      getFile().openWrite();
      file.stopwords.write( new ArrayList<Integer>( stopwords ));
      file.closeWrite();
   }
   
   public HashSet<Integer> getStopwords() {
      if (stopwords == null)
         openRead();
      return stopwords;
   }
   
   @Override
   public void closeRead() {  }

   @Override
   public void reuse() {
      throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
   }
   
   public static class File extends StructuredFile {
      CIntArrayField stopwords = this.addCIntArray("stopwords");
      public File( Datafile df ) {
         super( df );
      }
   }

}
