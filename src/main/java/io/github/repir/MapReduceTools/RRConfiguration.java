package io.github.repir.MapReduceTools;

import io.github.repir.tools.Content.Datafile;
import io.github.repir.tools.Content.FSFileInBuffer;
import io.github.repir.tools.Lib.ArgsParser;
import io.github.repir.tools.Lib.ArrayTools;
import io.github.repir.tools.Lib.Log;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import org.apache.hadoop.util.GenericOptionsParser;

/**
 * Extension of RepIRTools.Configuration, which is an extension to
 * Hadoop's Configuration, that is also used by {@link Repository}
 * to store its configuration. This extension reads the location
 * of RepIR resources from the environment, and sets these in the
 * Configuration for further use. If instantiated 
 * with an array of arguments, the first argument should be the name
 * of a file with configuration settings that is added to the 
 * Configuration. 
 * <p/>
 * For internal use, this extension contains methods to read and write
 * the Configuration set for a repository to a file.
 * @author Jeroen Vuurens
 */
public class RRConfiguration extends io.github.repir.tools.hadoop.Configuration {

   public static Log log = new Log(RRConfiguration.class);

   public RRConfiguration() {
       super();
   }
   
    protected RRConfiguration(org.apache.hadoop.conf.Configuration other) {
        super(other);
    }
   
   public RRConfiguration(String args[], String template) {
       super();
       this.setEnv();
       this.parseArgsConfFile(args, template);
   }

    protected void setEnv() {
      String repirdir = System.getenv("rrdir");
      String repirversion = System.getenv("rrversion");
      String user = System.getenv("rruser");
      set("rr.localdir", repirdir + "/");
      set("rr.libdir", repirdir + "/lib/");
      set("rr.configdir", repirdir + "/settings/");
      set("rr.version", repirversion);
      set("rr.user", user);
    }
    
   @Override
   public void processConfigFile(Datafile df) {
      super.processConfigFile(df);
      set("rr.conf", df.getFilename());
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

   public RRConfiguration(String filename) {
      super();
      setEnv();
      processConfigFile(configfile(filename));
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

   public Datafile configDatafile(String filename) {
      return configfile(filename);
   }

   // creates a Configuration based on a file with settings in a JAR
   public static RRConfiguration createFromResource(String resource) {
      InputStream input = Thread.currentThread().getContextClassLoader().getResourceAsStream(resource);
      FSFileInBuffer fi = new FSFileInBuffer(input);
      byte[] readBytes = fi.readBytes();
      RRConfiguration conf = new RRConfiguration();
      conf.setEnv();
      conf.processScript(new String(readBytes, 0, readBytes.length));
      return conf;
   }

   public static Datafile configfile(RRConfiguration conf) {
      Datafile in = new Datafile(conf.get("rr.configdir") + conf.get("rr.conf"));
      return in;
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
   
   public static RRConfiguration convert(org.apache.hadoop.conf.Configuration conf) {
        if (conf instanceof RRConfiguration) {
            return (RRConfiguration) conf;
        }
        return new RRConfiguration(conf);
    }


}
