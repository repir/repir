package io.github.repir.TestSet;

import io.github.repir.tools.Content.Datafile;
import io.github.repir.tools.Content.RecordCSV;
import io.github.repir.tools.Lib.Log;
import java.util.HashMap;

public class TrecTopicWT9 extends TrecTopicWT implements TrecTopicReader {

   public static Log log = new Log(TrecTopicWT9.class);

   public String createStartRecord(Field f) {
      return "wt09\\-";
   }

   public TrecTopicWT9(Datafile df) {
      super(df);
   }
}
