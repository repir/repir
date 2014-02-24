package io.github.repir.Repository;

import io.github.repir.tools.Content.Datafile;
import io.github.repir.tools.Content.RecordBinary;
import io.github.repir.tools.Lib.Log;
import io.github.repir.Repository.PartitionLocation.File;
import java.io.EOFException;

public class PartitionLocation extends StoredUnreportableFeature<File> {

   public static Log log = new Log(PartitionLocation.class);
   String location[][];

   protected PartitionLocation(Repository repository) {
      super(repository);
   }

   public void write(String[] l) {
      file.location.write(l);
   }

   public void loadMem() throws EOFException {
      location = new String[repository.partitions][];
      if (getFile().datafile.exists()) {
         file.openRead();
         for (int i = 0; i < repository.partitions; i++) {
            location[i] = file.location.read();
         }
         file.closeRead();
      } else {
         log.info("consider creating PartitionLocation to speed up retrieval");
      }
   }

   @Override
   public File createFile(Datafile datafile) {
      return new File(datafile);
   }

   public String[] read(int id) {
      try {
         if (location == null) {
            loadMem();
         }
         return (location[id] != null)?location[id]:new String[0];
      } catch (EOFException ex) {
         log.exception(ex, "get( %d )", id);
      }
      return null;
   }

   @Override
   public void setBufferSize(int size) {
      throw new UnsupportedOperationException("Doesnt make sense to use setBufferSize on PartitionLocation");
   }

   public static class File extends RecordBinary {

      public StringArrayField location = this.addStringArray("location");

      public File(Datafile df) {
         super(df);
      }
   }
}
