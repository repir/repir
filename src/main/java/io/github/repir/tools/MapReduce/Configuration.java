package io.github.repir.tools.MapReduce;

import io.github.repir.tools.Content.Datafile;
import io.github.repir.tools.Content.FSFileInBuffer;
import io.github.repir.tools.Lib.ArrayTools;
import io.github.repir.tools.Lib.Log;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import org.apache.hadoop.util.GenericOptionsParser;

/**
 * Extension of Hadoop's Configuration, that is also used by {@link Repository}
 * to store its configuration. The extension can read/write configurations from
 * flat text files used to configure a {@link Repository} or a specific
 * {@link Strategy}.
 * <p/>
 * Valid configuration keys have at least one dot, e.g. retriever.strategy, are
 * in lowercase and can either be assigned a String, int, long (ends with l),
 * double (has a decimal point), boolean (true/false) or array (multiple lines
 * +key.name=...). A minus "-key.name=" can be used to only set keys that have
 * no value yet.
 * <p/>
 * From files, "import filename" can be used to read settings from a file in the
 * same folder, and "delete key.name" can be used to delete key.name.
 *
 * @author Jeroen Vuurens
 */
public class Configuration extends io.github.repir.tools.hadoop.Configuration {

   public static Log log = new Log(Configuration.class);

   public Configuration() {
       super();
   }
   
    protected Configuration(org.apache.hadoop.conf.Configuration other) {
        super(other);
    }

   public Configuration(Datafile df) {
      super();
      read(df);
      String repirdir = System.getenv("rrdir");
      String repirversion = System.getenv("rrversion");
      String user = System.getenv("rruser");
      set("rr.localdir", repirdir + "/");
      set("rr.libdir", repirdir + "/lib/");
      set("rr.configdir", repirdir + "/settings/");
      set("rr.conf", df.getFilename());
      set("rr.version", repirversion);
      set("rr.user", user);
      String libs = get("rr.lib");
      if (libs != null && libs.length() > 0) {
         StringBuilder sb = new StringBuilder();
         for (String lib : libs.split(",")) {
            sb.append(",").append(get("rr.libdir")).append(lib);
         }
         String args[] = new String[]{"-libjars", sb.deleteCharAt(0).toString()};
          try {
              GenericOptionsParser p = new GenericOptionsParser(this, args);
          } catch (IOException ex) {
              log.exception(ex, "Failed to include rr.lib jars: %s", libs);
          }
      }
   }

   public Configuration(String filename) {
      this(configfile(filename));
   }

   public static Datafile configfile(String filename) {
      if (filename.charAt(0) != '/') {
         String repirdir = System.getenv("rrdir");
         filename = repirdir + "/settings/" + filename;
      }
      log.info("configfile %s", filename);
      Datafile in = new Datafile(filename);
      return in;
   }

   // creates a Configuration based on a file with settings in a JAR
   public static Configuration createFromResource(String resource) {
      InputStream input = Thread.currentThread().getContextClassLoader().getResourceAsStream(resource);
      FSFileInBuffer fi = new FSFileInBuffer(input);
      byte[] readBytes = fi.readBytes();
      Configuration conf = new Configuration();
      conf.read(new String(readBytes, 0, readBytes.length));
      return conf;
   }

   public static Datafile configfile(Configuration conf) {
      Datafile in = new Datafile(conf.get("rr.configdir") + conf.get("rr.conf"));
      return in;
   }

   public Configuration(String args[], String template) {
      this(args[0]);
      this.parseArgs(ArrayTools.subArray(args, 1), template);
   }

   public void writeBoolean(Datafile df, String key) {
      if (!containsKey(key)) {
         df.printf("%s =\n", key);
      } else {
         df.printf("%s = %s\n", key, getBoolean(key, false) ? "true" : "false");
      }
   }

   public void writeInt(Datafile df, String key) {
      if (!containsKey(key)) {
         df.printf("%s =\n", key);
      } else {
         df.printf("%s = %d\n", key, getInt(key, -1));
      }
   }

   public void writeLong(Datafile df, String key) {
      if (!containsKey(key)) {
         df.printf("%s =\n", key);
      } else {
         df.printf("%s = %dl\n", key, getLong(key, -1));
      }
   }

   public void writeDouble(Datafile df, String key) {
      if (!containsKey(key)) {
         df.printf("%s =\n", key);
      } else {
         df.printf("%s = %fl\n", key, getDouble(key, -1));
      }
   }

   public void writeString(Datafile df, String key) {
      if (!containsKey(key)) {
         df.printf("%s =\n", key);
      } else {
         df.printf("%s = %s\n", key, getRaw(key));
      }
   }

   public void writeStrings(Datafile df, String key) {
      if (!containsKey(key)) {
         df.printf("%s =\n", key);
      } else {
         for (String value : getStrings(key)) {
            df.printf("+%s = %s\n", key, value);
         }
      }
   }

   public void writeParametersToFile(Map<String, String> parameters) {
      parameters = new java.util.HashMap<String, String>(parameters);
      Datafile df = configfile(this);
      String content = df.readAsString();
      String lines[] = content.split("\\n");
      df.openWrite();
      for (String line : lines) {
         if (line.contains("=")) {
            String key = line.substring(0, line.indexOf("=")).trim();
            String value = parameters.get(key);
            if (value == null) {
               df.printf("%s=%s\n", key, value);
               parameters.remove(key);
            } else {
               df.printf("%s\n", line);
            }
         }
      }
      for (Map.Entry<String, String> e : parameters.entrySet()) {
         df.printf("%s=%s\n", e.getKey(), e.getValue());
      }
      df.closeWrite();
   }
   
   public static Configuration convert(org.apache.hadoop.conf.Configuration conf) {
        if (conf instanceof Configuration) {
            return (Configuration) conf;
        }
        return new Configuration(conf);
    }


}
