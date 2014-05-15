package io.github.repir.Repository;

import io.github.repir.tools.ByteSearch.ByteRegex;
import io.github.repir.tools.ByteSearch.ByteSearchPosition;
import io.github.repir.tools.Content.Datafile;
import io.github.repir.tools.Lib.ArgsParser;
import io.github.repir.tools.Lib.Log;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.mapred.JobPriority;

/**
 * Extension of Hadoop's Configuration, that is also used by {@link Repository} to
 * store its configuration. The extension can read/write configurations from
 * flat text files used to configure a {@link Repository} or a specific {@link Strategy}.
 * <p/>
 * Valid configuration keys have at least one dot, e.g. retriever.strategy, are 
 * in lowercase and can either be assigned a String, int, long (ends with l), double
 * (has a decimal point), boolean (true/false) or array (multiple lines +key.name=...).
 * A minus "-key.name=" can be used to only set keys that have no value yet.
 * <p/>
 * From files, "import filename" can be used to read settings from a file in the same
 * folder, and "delete key.name" can be used to delete key.name. 
 * 
 * @author Jeroen Vuurens
 */
public class Configuration extends org.apache.hadoop.conf.Configuration {

   public static Log log = new Log(Configuration.class);
   static ByteRegex configurationkey = new ByteRegex("\\+?\\c\\w*(\\.\\c\\w*)+=\\S*$");
   static ByteRegex commentregex = new ByteRegex("#[^\\n]*\\n");
   static ByteRegex importregex = new ByteRegex("[ \\t]*import[ \\t]+");
   static ByteRegex deleteregex = new ByteRegex("[ \\t]*delete[ \\t]+");
   static ByteRegex arraykeyregex = new ByteRegex("\\+[ \\t]*\\c\\w*(\\.\\w+)*[ \\t]*=");
   static ByteRegex optionalkeyregex = new ByteRegex("\\-[ \\t]*\\c\\w*(\\.\\w+)*[ \\t]*=");
   static ByteRegex keyregex = new ByteRegex("[ \\t]*\\c\\w*(\\.\\w+)*[ \\t]*=");
   static ByteRegex emptylineregex = new ByteRegex("[ \\t]*\\n");
   static ByteRegex junklineregex = new ByteRegex(".*?\\n");
   static ByteRegex lineregex = ByteRegex.combine(commentregex, emptylineregex, importregex, deleteregex, arraykeyregex, optionalkeyregex, keyregex, junklineregex);
   static ByteRegex doubleregex = new ByteRegex("[ \\t]*\\d+\\.\\d+\\s*\\n");
   static ByteRegex longregex = new ByteRegex("[ \\t]*\\d+l\\s*\\n");
   static ByteRegex intregex = new ByteRegex("[ \\t]*\\d+\\s*\\n");
   static ByteRegex boolregex = new ByteRegex("[ \\t]*(true|false)\\s*\\n");
   static ByteRegex stringregex = new ByteRegex("[^\\n]+\\n");
   static ByteRegex valueregex = ByteRegex.combine(emptylineregex, longregex, doubleregex, intregex, boolregex, stringregex);

   public Configuration() {
      super();
   }

   private Configuration(org.apache.hadoop.conf.Configuration other) {
      super(other);
   }

   public Configuration(Datafile df) {
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
   }

   public Configuration(String filename) {
      this(configfile(filename));
   }
   
   public static Datafile configfile(String filename) {
      if (filename.charAt(0) != '/') {
         String irefdir = System.getenv("rrdir");
         filename = irefdir + "/settings/" + filename;
      }
      Datafile in = new Datafile(filename);
      return in;
   }

   public static Datafile configfile( Configuration conf ) {
      Datafile in = new Datafile(conf.get("rr.configdir") + conf.get("rr.conf"));
      return in;
   }
   
   public Configuration(String args[], String template) {
      this(args[0]);
      setStrings("rr.args", args);
      args = argsToConf(args);
      ArgsParser parsedargs = new ArgsParser(args, "configfile " + template);
      for (Map.Entry<String, String> entry : parsedargs.parsedargs.entrySet()) {
         set(entry.getKey(), entry.getValue());
      }
      if (parsedargs.getRepeatedGroup() != null) {
         setStrings(parsedargs.getRepeatedGroupName(), parsedargs.getRepeatedGroup());
      }
   }
   
   private String[] argsToConf(String args[]) {
      ArrayList<String> ar = new ArrayList<String>();
      for (int i = 0; i < args.length; i++) {
         if (configurationkey.startsWith(args[i])) {
            read(args[i]);
         } else {
            ar.add(args[i]);
         }
      }
      args = ar.toArray(new String[ar.size()]);
      return args;
   }
   
   
   public void writeBoolean(Datafile df, String key) {
      if (!containsKey(key))
         df.printf("%s =\n", key);
      else
         df.printf("%s = %s\n", key, getBoolean(key, false) ? "true" : "false");
   }
   
   public void writeInt(Datafile df, String key) {
      if (!containsKey(key))
         df.printf("%s =\n", key);
      else
      df.printf("%s = %d\n", key, getInt(key, -1));
   }
   
   public void writeLong(Datafile df, String key) {
      if (!containsKey(key))
         df.printf("%s =\n", key);
      else
         df.printf("%s = %dl\n", key, getLong(key, -1));
   }
   
   public void writeDouble(Datafile df, String key) {
      if (!containsKey(key))
         df.printf("%s =\n", key);
      else
      df.printf("%s = %fl\n", key, getDouble(key, -1));
   }
   
   public void writeString(Datafile df, String key) {
      if (!containsKey(key))
         df.printf("%s =\n", key);
      else
         df.printf("%s = %s\n", key, getRaw(key));
   }
   
   public void writeStrings(Datafile df, String key) {
      if (!containsKey(key))
         df.printf("%s =\n", key);
      else
         for (String value : getStrings(key))
            df.printf("+%s = %s\n", key, value);
   }
   
   public void writeParametersToFile( Map<String, String> parameters ) {
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
   
   public void read(Datafile df) {
      read(df, df.readAsString());
   }
   
   public void read(String content) {
      if (content != null)
         read(null, content);
   }
   
   private void read(Datafile df, String cont) {
      byte content[];
      if (cont.endsWith("\n")) {
         content = cont.getBytes();
      } else {
         content = new StringBuilder(cont).append('\n').toString().getBytes();
      }
      int pos = 0;
      while (pos < content.length) {
         boolean array = false;
         boolean optional = false;
         ByteSearchPosition p = lineregex.matchPos(content, pos, content.length);
         if (!p.found()) {
            break;
         }
         pos = p.end;
         switch (p.pattern) {
            case 0: // line is comment
            case 1: // line is empty
               pos = p.end;
               continue;
            case 2: // line is import
               if (df != null) {
                  p = stringregex.matchPos(content, p.end, content.length);
                  pos = p.end;
                  String file = new String(content, p.start, p.end - p.start).trim();
                  Datafile subfile = new Datafile(df.getDir().getFilename(file));
                  subfile.setFileSystem(df.getFileSystem());
                  String c = subfile.readAsString();
                  read(df, c);
               } else {
                  log.fatal("Cannot read import from string");
               }
               continue;
            case 3: // delete entry
               p = stringregex.matchPos(content, p.end, content.length);
               pos = p.end;
               String key = new String(content, p.start, p.end - p.start - 1).trim();
               delete(key);
               continue;
            case 4: // line is array
               array = true;
               p.start++;
               break;
            case 5: // optionalkey
               optional = true;
               p.start++;
            case 6: // line is no array
               break;
            default:
               log.info("unreadable line in configuration : %s", new String(content, p.start, p.end - p.start));
               continue;
         }
         String key = new String(content, p.start, p.end - p.start - 1).trim();
         if (array) {
            p = stringregex.matchPos(content, p.end, content.length);
            pos = p.end;
            String value = new String(content, p.start, p.end - p.start - 1).trim();
            addArray(key, value);
         } else {
            p = valueregex.matchPos(content, p.end, content.length);
            pos = p.end;

            String value = new String(content, p.start, p.end - p.start).trim();
            //log.info("conf key %s pattern %d content %s", key, p.pattern, value);
            switch (p.pattern) {
               case 0: // empty line means delete key
                  delete(key);
                  break;
               case 1: // long
                  if (!optional || !containsKey(key))
                      setLong(key, Long.parseLong(value.substring(0, value.indexOf('l'))));
                  break;
               case 2: // double is stored as String to avoid precision loss fro converting to float
                  if (!optional || !containsKey(key))
                     set(key, value);
                  break;
               case 3: // int
                  if (!optional || !containsKey(key))
                     setInt(key, Integer.parseInt(value));
                  break;
               case 4: // boolean
                  if (!optional || !containsKey(key))
                     setBoolean(key, value.equalsIgnoreCase("true"));
                  break;
               case 5: // string
                  if (!optional || !containsKey(key))
                     set(key, value);
            }
         }
      }
   }
   
   public boolean containsKey(String key) {
      String value = super.get(key);
      return (value != null && value.length() > 0);
   }

   public void delete(String label) {
      set(label, "");
   }

   public void addArray(String label, String value) {
      if (value.length() == 0) {
         delete(label);
      } else {
         ArrayList<String> values;
         if (containsKey(label)) {
           values = getStringList(label);
         } else {
            values = new ArrayList<String>();
         }
         if (!values.contains(value))
            values.add(value);
         setStrings(label, values.toArray(new String[values.size()]));
      }
   }
   
   public static Configuration convert(org.apache.hadoop.conf.Configuration conf) {
      if (conf instanceof Configuration) {
         return (Configuration) conf;
      }
      return new Configuration(conf);
   }

   /**
    * If a String value contains ${correct.name} then that part will be replaced
    * by the value of correct.name. This way general configuration settings can be
    * reused such as location of the repository. Therefore the use of 
    * {@link #getSubString(java.lang.String) } is recommended over {@link #get(java.lang.String)}
    * @param key
    * @return value of key, in which other key references have been substituted.
    */
   public String getSubString(String key) {
      return get(key, "");
   }

   /**
    * 
    * @param key
    * @return Array of strings attached configured for the given key. Different
    * from Hadoop's default getStrings(), this method returns an array length 0 when
    * the key does not exist, and substitutes nested variables (e.g. ${name}) in the
    * values.
    */
   @Override
   public String[] getStrings(String key) {
      String values[] = super.getStrings(key);
      if (values == null) {
         return new String[0];
      }
      //for (int i = 0; i < values.length; i++) {
      //   values[i] = substituteString(values[i]);
      //}
      return values;
   }

   @Override
   public String get(String key, String defaultvalue) {
      String value = super.get(key, defaultvalue);
      return value;
      //return substituteString(value);
   }

   public ArrayList<String> getStringList(String key) {
      ArrayList<String> values = new ArrayList<String>();
      String value[] = getStrings(key);
      if (value != null) {
         values.addAll(Arrays.asList(value));
      }
      return values;
   }

   public ArrayList<Integer> getIntList(String key) {
      ArrayList<Integer> values = new ArrayList<Integer>();
      String value[] = getStrings(key, new String[0]);
      if (value != null) {
         for (int i = 0; i < value.length; i++) {
            values.add(Integer.parseInt(value[i]));
         }
      }
      return values;
   }

   public ArrayList<Long> getLongList(String key) {
      ArrayList<Long> values = new ArrayList<Long>();
      String value[] = super.getStrings(key, new String[0]);
      if (value != null) {
         for (int i = 0; i < value.length; i++) {
            values.add(Long.parseLong(value[i]));
         }
      }
      return values;
   }

   public void setIntList(String key, ArrayList<Integer> list) {
      ArrayList<String> s = new ArrayList<String>();
      for (Integer i : list) {
         s.add(i.toString());
      }
      setStringList(key, s);
   }

   public void setLongList(String key, ArrayList<Long> list) {
      ArrayList<String> s = new ArrayList<String>();
      for (Long i : list) {
         s.add(i.toString());
      }
      setStringList(key, s);
   }

   public void setStringList(String key, ArrayList<String> list) {
      setStrings(key, list.toArray(new String[list.size()]));
   }

   /**
    * Note: Hadoop 0.20 does not support double, so these are stored as strings,
    * if the value is not empty or a valid double a fatal exception is the
    * result
    * <p/>
    * @return the double value of the key.
    */
   public double getDouble(String key, double defaultvalue) {
      double d = defaultvalue;
      String value = get(key);
      try {
         if (value != null && value.length() > 0) {
            d = Double.parseDouble(value);
         }
      } catch (NumberFormatException ex) {
         log.fatalexception(ex, "Configuration setting '%s' does not contain a valid double '%s'", key, value);
      }
      return d;
   }

   /**
    * Substitutes ${key} occurrences with their value in the configuration.
    * Note: there is no check for cyclic references, which are not allowed.
    * <p/>
    * @param conf
    * @param value
    * @return
    */
   private String substituteString(String value) {
      if (value == null) {
         return null;
      }
      for (int p1 = value.indexOf("${"); p1 > -1; p1 = value.indexOf("${")) {
         int p2 = value.indexOf("}", p1);
         String subkey = value.substring(p1 + 2, p2 - p1 - 2);
         value = value.substring(0, p1) + getSubString(subkey) + value.substring(p2 + 1);
      }
      return value;
   }
   
   /**
    * Outputs all keys that begin with <prefix> that are configured.
    * For debug purposes.
    * @param prefix 
    */
   public void print(String prefix) {
      for (Entry<String, String> e : this) {
         if (prefix == null || prefix.length() == 0 || e.getKey().startsWith(prefix)) {
            log.printf("%s=%s", e.getKey(), e.getValue());
         }
      }
   }
   
   public FileSystem FS() {
      try {
         return FileSystem.get(this);
      } catch (IOException ex) {
         log.exception(ex, "getFS()");
         return null;
      }
   }
   
   public static FileSystem getFS() {
      return new Configuration().FS();
   }
   
   public void softSetConfiguration(String key, String value) {
      if (!containsKey(key)) {
         set(key, value);
      }
   }

   public void setPriorityHigh() {
      softSetConfiguration("mapred.job.priority", JobPriority.HIGH.toString());
   }

   public void setPriorityVeryHigh() {
      softSetConfiguration("mapred.job.priority", JobPriority.VERY_HIGH.toString());
   }

   public void setPriorityLow() {
      softSetConfiguration("mapred.job.priority", JobPriority.LOW.toString());
   }

}
