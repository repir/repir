package io.github.repir.Repository;

import io.github.repir.Repository.PartitionLocation.File;
import io.github.htools.io.Datafile;
import io.github.htools.io.EOCException;
import io.github.htools.io.struct.StructuredFile;
import io.github.htools.lib.Log;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.apache.hadoop.fs.BlockLocation;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

/**
 * This feature stores the primary node that stored the data files for each
 * partition. Once created, this feature is used automatically to lookup the
 * preferred node, thus avoiding having to do lookups for every task, which
 * saves a lot of time.
 *
 * @author jer
 */
public class PartitionLocation extends StoredUnreportableFeature<File> {

   public static Log log = new Log(PartitionLocation.class);
   String location[][];

   private PartitionLocation(Repository repository) {
      super(repository);
   }

   public static PartitionLocation get(Repository repository) {
       String label = canonicalName(PartitionLocation.class);
       PartitionLocation partitionlocation = (PartitionLocation)repository.getStoredFeature(label);
       if (partitionlocation == null) {
          partitionlocation = new PartitionLocation(repository);
          repository.storeFeature(label, partitionlocation);
       }
       return partitionlocation;
   }
   
   public void write(String[] l) {
      file.location.write(l);
   }

   public void loadMem() throws EOCException {
      if (location == null && getFile().getDatafile().exists()) {
         if (!getFile().getDatafile().isWriteOpen()) {
            location = new String[repository.partitions][];
            getFile().openRead();
            for (int i = 0; i < repository.partitions; i++) {
               location[i] = file.location.read();
            }
            file.closeRead();
         }
      } else {
         log.info("consider creating PartitionLocation to speed up retrieval");
      }
   }

   @Override
   public File createFile(Datafile datafile) {
      return new File(datafile);
   }

   public String[] read(int partition) {
      try {
         if (location == null) {
            loadMem();
         }
         return (location != null && location[partition] != null) ? location[partition] : estimateLocations(partition);
      } catch (EOCException ex) {
         log.exception(ex, "get( %d )", partition);
      }
      return null;
   }

   public static class File extends StructuredFile {

      public StringArrayField location = this.addStringArray("location");

      public File(Datafile df) {
         super(df);
      }
   }

   public String[] estimateLocations(int partition) {
      HashMap<String, Integer> hosts = new HashMap<String, Integer>();
      String partitionstring = io.github.htools.lib.PrintTools.sprintf("%04d", partition);
      FileSystem fs = repository.getFS();
      try {
         if (fs != null) {
            for (String filename : repository.getIndexDir().getFilepathnames()) {
               Path p = new Path(filename);
               if (p.getName().contains(partitionstring)) {
                  FileStatus file = fs.getFileStatus(p);
                  BlockLocation[] blkLocations = fs.getFileBlockLocations(file, 0, 0);
                  for (BlockLocation b : blkLocations) {
                     String h[] = b.getHosts();
                     for (String host : h) {
                        Integer count = hosts.get(host);
                        if (count == null) {
                           hosts.put(host, 1);
                        } else {
                           hosts.put(host, count + 1);
                        }
                     }
                  }
               }
            }
         }
      } catch (IOException ex) {
         log.exception(ex, "getLocations( %s, %d )", repository.basedir.getCanonicalPath(), partition);
      }
      int max = 0;
      String maxlocation = "";
      for (Map.Entry<String, Integer> entry : hosts.entrySet()) {
         if (entry.getValue() > max) {
            max = entry.getValue();
            maxlocation = entry.getKey();
         }
      }
      //log.info("getLocations() %d %s", hosts.length, hosts.toString());
      return new String[]{maxlocation};
   }

}
