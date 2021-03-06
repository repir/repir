package io.github.repir.Repository.Pig;

import io.github.htools.extract.Content;
import io.github.htools.hadoop.io.archivereader.RecordKey;
import io.github.htools.hadoop.io.archivereader.RecordValue;
import io.github.repir.Repository.*;
import io.github.repir.Retriever.Document;
import io.github.htools.search.ByteSearch;
import io.github.htools.search.ByteSearchPosition;
import io.github.htools.io.Datafile;
import io.github.htools.io.struct.StructuredFileIntID;
import io.github.htools.io.struct.StructuredTextFile.DataNode;
import io.github.htools.io.struct.StructuredTextFile.FolderNode;
import io.github.htools.io.struct.StructuredTextFile.Node;
import io.github.htools.io.struct.StructuredTextPig;
import io.github.htools.io.struct.StructuredTextPigTuple;
import io.github.htools.lib.ClassTools;
import io.github.htools.lib.PrintTools;
import io.github.htools.lib.StrTools;
import java.util.HashMap;

/**
 * An abstract feature that can store a value per Document in the Repository.
 * This value can be accessed with an internal DocumentID passed through
 * {@link EntityStoredFeature#read(io.github.repir.Retriever.Document) }
 * @author jer
 * @param <F> a StructuredFileIntID file to store it's values, allowing 
 * the stored Record to be accessed through an internal integer ID
 * @param <C> The datatype stored
 */
public abstract class PigFeature<F extends StructuredTextPig, C extends StructuredTextPigTuple<? extends StructuredTextPig>> extends StoredFeature {

   public F file;
   
   public PigFeature(Repository repository) {
      super(repository);
   }

   public PigFeature(Repository repository, String field) {
      super(repository, field);
   }

   @Override
   public F getFile() {
      if (file == null) {
         file = createFile(getStoredFeatureFile());
      }
      return file;
   }
   
   @Override
   public Datafile getStoredFeatureFile() {
      Datafile datafile;
      String name = getCanonicalName();
      name = name.replaceFirst(":", ".");
      String path = repository.configuredString(name.toLowerCase() + ".path");
      if (path != null && path.length() > 0)
         datafile = new Datafile( repository.getFS(), path);
      else
         datafile = repository.getBaseDir().getFile(PrintTools.sprintf("pig/%s.%s", repository.getPrefix(), getFileNameSuffix()));
      return datafile;
   }
   
   private static final ByteSearch dot = ByteSearch.create("\\."); 
   
   public String loadScript() {
      StringBuilder sb = new StringBuilder();
      String filename = getFile().getDatafile().getName();
      ByteSearchPosition pos = dot.findLastPos(filename);
      sb.append("LOAD '");
      sb.append( getFile().getDatafile().getCanonicalPath() ).append("' AS ");
      sb.append(loadFolder( file.getRoot()));
      sb.append(";\n");
      return sb.toString();
   }
   
   public String loadLocalScript() {
      StringBuilder sb = new StringBuilder();
      String filename = getFile().getDatafile().getName();
      ByteSearchPosition pos = dot.findLastPos(filename);
      sb.append("LOAD '");
      sb.append("data/").append( getFile().getDatafile().getName() ).append("' AS ");
      sb.append(loadFolder( file.getRoot()));
      sb.append(";\n");
      return sb.toString();
   }
   
   public String loadFolder(FolderNode folder) {
      StringBuilder sb = new StringBuilder();
      sb.append("(");
      boolean first = true;
      for (Node n : folder.orderedfields) {
         if (first)
            first = false;
         else
            sb.append(", ");
         if (n instanceof DataNode) {
            sb.append(n.label);
            Class c = ClassTools.getGenericType(n);
            if (c.equals(Integer.class)) {
               sb.append(":int");
            } else if (c.equals(Long.class)) {
               sb.append(":long");
            } else if (c.equals(Double.class)) {
               sb.append(":double");
            } else if (c.equals(String.class)) {
               sb.append(":chararray");
            } else if (c.equals(Boolean.class)) {
               sb.append(":chararray");
            }
         } else {
            sb.append(n.label).append(":{");
            sb.append(loadFolder((FolderNode)n)).append("}");
         }
      }
      sb.append(")");
      return sb.toString();
   }
   
   public abstract F createFile(Datafile datafile);

   @Override
   public void openRead() {
      getFile().openRead();
   }

   @Override
   public void closeRead() {
      getFile().closeRead();
      file = null;
   }

   public void openAppend() {
      if (getFile().lock())
         getFile().openAppend();
      else 
         log.fatal("Could not lock file %s", getFile().getDatafile().getCanonicalPath());
   }
   
   public void openWrite() {
      if (getFile().lock())
         getFile().openWrite();
      else 
         log.fatal("Could not lock file %s", getFile().getDatafile().getCanonicalPath());
   }
   
   public void closeWrite() {
      getFile().closeWrite();
      getFile().unlock();
   }
   
   public abstract C getValue();

   public abstract void write(C value);
   
   
   public void setBufferSize(int size) {
      getFile().getDatafile().setBufferSize(size);
   }
   
   public void reuse() {}
}
