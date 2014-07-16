package io.github.repir.Repository;

import io.github.repir.tools.Content.Datafile;
import io.github.repir.tools.Structure.StructuredFile;
import io.github.repir.tools.Lib.Log;
import io.github.repir.tools.Lib.PrintTools;

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

   public abstract void setBufferSize(int size);
   
   public void writeCache() {}
   
   public abstract void reuse();
   
   public Datafile getStoredFeatureFile() {
      Datafile datafile;
      String name = getCanonicalName();
      name = name.replaceFirst(":", ".");
      String path = repository.configuredString(name.toLowerCase() + ".path");
      if (path != null && path.length() > 0)
         datafile = new Datafile( repository.fs, path);
      else
         datafile = repository.basedir.getFile(PrintTools.sprintf("repository/%s.%s", repository.getPrefix(), getFileNameSuffix()));
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
         datafile = repository.basedir.getFile(PrintTools.sprintf("repository/%s.%s.%04d", repository.getPrefix(), getFileNameSuffix(), segment));
      return datafile;
   }

}
