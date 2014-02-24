package io.github.repir.EntityReader;

import io.github.repir.Repository.Repository;
import io.github.repir.tools.Lib.Log;
import java.util.HashMap;
import io.github.repir.tools.DataTypes.Configuration;
import org.apache.hadoop.fs.Path;

public class FileFilter {

   public static Log log = new Log(FileFilter.class);
   String[] validFilenameStart;
   String[] validFilenameEnd;
   String[] invalidFilenameStart;
   String[] invalidFilenameEnd;

   public FileFilter(Configuration conf) {
      validFilenameStart = conf.getStrings("entityreader.validfilenamestart");
      validFilenameEnd = conf.getStrings("entityreader.validfilenameend");
      invalidFilenameStart = conf.getStrings("entityreader.invalidfilenamestart");
      invalidFilenameEnd = conf.getStrings("entityreader.invalidfilenameend");
   }

   public FileFilter(org.apache.hadoop.conf.Configuration conf) {
      this(Configuration.convert(conf));
   }
   
   public boolean acceptFile(Path path) {
      String file = path.getName();
      return startWith(file) && endWith(file) && omitStart(file) && omitEnd(file);
   }

   protected boolean startWith(String file) {
      if (validFilenameStart == null) {
         return true;
      }
      for (String s : validFilenameStart) {
         if (file.startsWith(s)) {
            return true;
         }
      }
      return false;
   }

   protected boolean endWith(String file) {
      if (validFilenameEnd == null) {
         return true;
      }
      for (String s : validFilenameEnd) {
         if (file.endsWith(s)) {
            return true;
         }
      }
      return false;
   }

   protected boolean omitStart(String file) {
      if (invalidFilenameStart == null) {
         return true;
      }
      for (String s : invalidFilenameStart) {
         if (file.startsWith(s)) {
            return false;
         }
      }
      return true;
   }

   protected boolean omitEnd(String file) {
      if (invalidFilenameEnd == null) {
         return true;
      }
      for (String s : invalidFilenameEnd) {
         if (file.endsWith(s)) {
            return false;
         }
      }
      return true;
   }
}
