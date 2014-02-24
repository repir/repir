package io.github.repir.Repository;

public abstract class SystemValues<C>  {

   public Repository repository;

   public SystemValues(Repository repository) {
      this.repository = repository;
   }
   
   public String getCanonicalName() {
      String clazz = getClass().getCanonicalName();
      clazz = io.github.repir.tools.Lib.StrTools.removeOptionalStart(clazz, Repository.class.getPackage().getName() + ".");
      return clazz;
   }
}
