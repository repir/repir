package io.github.repir.Repository;

import io.github.repir.EntityReader.MapReduce.TermEntityKey;
import io.github.repir.tools.io.Datafile;
import io.github.repir.tools.io.struct.StructuredFile;
import io.github.repir.tools.lib.Log;
import io.github.repir.tools.lib.PrintTools;

/**
 * Generic class for Features that are stored in the repository. Implementations 
 * must declare a RecordIdentity file, (usually an extension of StructuredFile that 
 * ensures records have a unique ID (int)). For performance, the features that are merged with other
 * features should be stored physically sorted on ID. The second declaration is a data 
 * type, which can be complex.
 * @author jeroen
 * @param <F> FileType that extends RecordIdentity
 * @param <C> Data type of the feature
 */
public abstract class StoredFeature<F> extends Feature {

   public static Log log = new Log(StoredFeature.class);
   
   public StoredFeature(Repository repository) {
      super( repository  );
   }
   
   public StoredFeature(Repository repository, String field) {
      super( repository, field  );
   }
   
   public abstract F getFile();

   public abstract void openRead();

   public abstract void closeRead();
   
   public void writeCache() {}
   
   public abstract void reuse();
   
   protected String getFeatureFolder() {
       return "repository";
   }
   
   public Datafile getStoredFeatureFile() {
      Datafile datafile;
      String name = getCanonicalName();
      name = name.replaceFirst(":", ".");
      String path = repository.configuredString(name.toLowerCase() + ".path");
      if (path != null && path.length() > 0)
         datafile = new Datafile( repository.fs, path);
      else
         datafile = repository.basedir.getFile(PrintTools.sprintf("%s/%s.%s", getFeatureFolder(), repository.getPrefix(), getFileNameSuffix()));
      return datafile;
   }

   public Datafile getStoredFeatureFile(int segment) {
      if (segment == -1)
         log.crash();
      Datafile datafile;
      String name = getCanonicalName();
      name = name.replaceFirst(":", ".");
      String path = repository.configuredString(name.toLowerCase() + ".path");
      if (path != null && path.length() > 0)
         datafile = new Datafile( repository.fs, PrintTools.sprintf("%s.%04d", path, segment));
      else
         datafile = repository.basedir.getFile(PrintTools.sprintf("%s/%s.%s.%04d", getFeatureFolder(), repository.getPrefix(), getFileNameSuffix(), segment));
      return datafile;
   }
   
   public static void storeFeature(Repository repository, String label, StoredFeature sf) {
       repository.storeFeature(label, sf);
   }

   public static StoredFeature getStoredFeature(Repository repository, String label) {
       return repository.getStoredFeature(label);
   }
}
