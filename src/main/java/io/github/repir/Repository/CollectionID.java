package io.github.repir.Repository;

import io.github.repir.tools.Lib.Log;
import java.util.HashMap;

/**
 * The collection ID is a mandatory feature that contains a literal string extracted from
 * the source, which is used to externally identify the entities in the collection. This
 * feature maps the integer internal entity/document ID to the original collection ID.
 * The configuration does not need to contain the CollectionID feature explicitly, but does
 * need to extract the collection id into a literal "collectionid" field.
 * @see EntityStoredFeature
 * @author jer
 */ 
public class CollectionID extends DocLiteral  {

   public static Log log = new Log(CollectionID.class);
   private String entityAttribute;

   protected CollectionID(Repository repository) {
      super(repository, "");
      entityAttribute = "collectionid";
   }
   
   protected CollectionID(Repository repository, String field) {
      super(repository, "");
      entityAttribute = field;
   }
   
   public static CollectionID get(Repository repository) {
       String label = canonicalName(CollectionID.class);
       CollectionID collectionid = (CollectionID)repository.getStoredFeature(label);
       if (collectionid == null) {
          collectionid = new CollectionID(repository);
          repository.storeFeature(label, collectionid);
       }
       return collectionid;
   }
   
   public static CollectionID get(Repository repository, String field) {
       String label = canonicalName(CollectionID.class, field);
       CollectionID collectionid = (CollectionID)repository.getStoredFeature(label); 
       if (collectionid == null) {
          collectionid = new CollectionID(repository, field);
          repository.storeFeature(label, collectionid);
       }
       return collectionid;
   }
   
   @Override
   public String getLabel() {
      return getClass().getSimpleName();
   }
   
   @Override
   protected String getFileName() {
       return className(getClass());
   }
   
   public String entityAttribute() {
      return entityAttribute;
   }
   
   public HashMap<String, Integer> getCollectionIDs(int partition) {
      HashMap<String, Integer> list = new HashMap<String, Integer>();
      this.setPartition(partition);
      file.openRead();
      while (file.nextRecord()) {
         list.put(file.literal.value, list.size());
      }
      file.closeRead();
      return list;
   }
}
