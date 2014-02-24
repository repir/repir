package io.github.repir.TestSet;

import io.github.repir.tools.Lib.Log;

public class DocFilterDOE implements DocFilter {

   @Override
   public boolean keepDocument(String collectionID) {
      return collectionID.startsWith("DOE");
   }
}