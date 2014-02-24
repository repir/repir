package io.github.repir.Repository;

import io.github.repir.Strategy.Strategy;
import io.github.repir.tools.Content.BufferDelayedWriter;
import io.github.repir.tools.Content.BufferReaderWriter;

public abstract class Feature {
   BufferDelayedWriter bdw = new BufferDelayedWriter();
   BufferReaderWriter reader = new BufferReaderWriter();
   public Repository repository;

   public Feature(Repository repository) {
      this.repository = repository;
   }
   
   public String getCanonicalName() {
      String clazz = getClass().getCanonicalName();
      clazz = io.github.repir.tools.Lib.StrTools.removeOptionalStart(clazz, Repository.class.getPackage().getName() + ".");
      clazz = io.github.repir.tools.Lib.StrTools.removeOptionalStart(clazz, Strategy.class.getPackage().getName() + ".");
      return clazz;
   }

   public String getLabel() {
      return getClass().getSimpleName();
   }
   
   /**
    * The suffix of a stored feature's filename is <classsimplename>, which is 
    * obtained from it's CanonicalName, by stripping any parameters after a ":".
    * The main user of this function is the repository, which will modify this 
    * name by optionally adding a path, partition number and repository name. Using
    * a different version of a file is facilitated through the configuration settings
    * by setting <classsimplename>.suffix (all lowercase!). The filenamesuffix is
    * than appended with "." and the string configured, e.g. phrasestats.suffix=v2
    * will have the system use PhraseStats.v2.
    * @return 
    */
   public String getFileNameSuffix() {
      String name = getCanonicalName();
      name = name.replaceFirst(":", ".");
      String suffix = repository.getConfigurationString(name.toLowerCase() + ".suffix");
      if (suffix != null && suffix.length() > 0)
         name += "." + suffix;
      return io.github.repir.tools.Lib.StrTools.getToString(name + ":", ":");
   }

}
