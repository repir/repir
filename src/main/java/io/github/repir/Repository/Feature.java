package io.github.repir.Repository;

import io.github.repir.Strategy.Strategy;
import io.github.repir.tools.Buffer.BufferDelayedWriter;
import io.github.repir.tools.Buffer.BufferReaderWriter;
import io.github.repir.tools.Content.Datafile;
import io.github.repir.tools.Lib.Log;
import io.github.repir.tools.Lib.PrintTools;
import java.util.HashMap;

/**
 * A Feature can be anything useful for retrieval or analysis of items stored in
 * a Repository. Typical branches are StoredFeatures, which are stored in the Repository,
 * and ReportedUnstoredFeatures which only exist during a retrieval task.
 * 
 * The Repository can manage Features, which are identified by their CanonicalName,
 * which is usually their classname, optionally followed by :parameter. The recommended
 * location for features is io.github.repir.Repository for StoredFeatures and
 * io.github.repir.Strategy for ReportedUnstoredFeatures.
 * @author jer
 */
public abstract class Feature {
   public static Log log = new Log( Feature.class );
   BufferDelayedWriter bdw = new BufferDelayedWriter();
   BufferReaderWriter reader = new BufferReaderWriter();
   private String field;
   public Repository repository;

   public Feature(Repository repository) {
      this.repository = repository;
      this.field = "";
   }
   
   public Feature(Repository repository, String field) {
      this.repository = repository;
      this.field = field;
   }
   
   public String getCanonicalName() {
      if (field.length() == 0)
         return canonicalName( getClass() );
      else
         return canonicalName( getClass(), field );
   }
   
   protected String getFileName() {
       if (field.length() > 0)
          return className(getClass()) + "." + field;
       else
          return className(getClass());
   }
   
   public String getField() {
      return field;
   }
   
   public String entityAttribute() {
      return field;
   }
   
   public static String canonicalName(Class c, String ... parameter) {
      StringBuilder sb = new StringBuilder();
      sb.append(className(c));
      for (String s : parameter) 
         sb.append(":").append(s);
      return sb.toString();
   }
   
   protected static String className(Class c) {
       if (!Feature.class.isAssignableFrom(c))
         log.fatal("No valid feature: %s", c.getCanonicalName());
      String clazz = c.getCanonicalName();
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
    * than appended with "." and the string configured, e.g. proximitystats.suffix=v2
    * will have the system use ProximityStats.v2.
    * @return 
    */
   public String getFileNameSuffix() {
      String name = getFileName();
      String suffix = repository.configuredString(name.toLowerCase() + ".suffix");
      if (suffix != null && suffix.length() > 0)
         name += "." + suffix;
      return name;
   }
}
