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

   protected CollectionID(Repository repository) {
      super(repository, "");
   }
   
   @Override
   public String getLabel() {
      return getClass().getSimpleName();
   }
   
   @Override
   public String entityAttribute() {
      return "collectionid";
   }
   
   public HashMap<String, Integer> getCollectionIDs(int partition) {
      HashMap<String, Integer> list = new HashMap<String, Integer>();
      this.setPartition(partition);
      file.openRead();
      while (file.next()) {
         list.put(file.literal.value, list.size());
      }
      file.closeRead();
      return list;
   }
}
