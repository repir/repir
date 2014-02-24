package io.github.repir.Extractor;

import io.github.repir.tools.Content.Dir;
import io.github.repir.tools.Content.FSDir;
import io.github.repir.tools.Content.FSFile;
import io.github.repir.tools.Lib.Log;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Implements Reader class for a file
 */
public class ReaderDir implements Reader {

   public static Log log = new Log(ReaderDir.class);
   protected boolean initialized = false;
   public int cycles = 0;       // the number of tiems the buffer has been read
   public int maxcycles = 0;    // a stopping criterion to read a number of cycles 
   public long bufferstart;
   FSDir basedir;
   ArrayList<FSFile> files = new ArrayList<FSFile>();
   InputStream fileinputstream = null;
   private int bufferend = 0;
   Iterator<FSFile> iter = null;
   FSFile currentfile = null;

   public ReaderDir(FSDir basedir) {
      this.basedir = basedir;
      initialized = true;
   }

   public ReaderDir(String filename) {
      this(new FSDir(filename));
   }

   public void addSubdir(String subdir) {
      FSDir dir = basedir.getSubdir(subdir);
      files.addAll(dir.getFiles());
   }

   /**
    * set the maximum number of times the buffer should be read
    * <p/>
    * @param max
    */
   public void setMaxCycles(int max) {
      maxcycles = max;
   }

   @Override
   public int read(byte[] buffer, int offset) {
      if (iter == null) {
         iter = files.iterator();
      }
      if (fileinputstream == null && iter.hasNext()) {
         currentfile = iter.next();
         fileinputstream = currentfile.getInputStream();
      }
      int read = -1;
      //log.info("cycle %d %d %d", cycles, offset, bufferstart);
      if (offset >= buffer.length) {
         log.crash();
      }
      if (maxcycles > 0 && cycles >= maxcycles) {
         return read;
      }
      cycles++;
      if (fileinputstream != null && offset >= 0) {
         try {
            read = fileinputstream.read(buffer, offset, buffer.length - offset);
            bufferstart += bufferend - offset;
            if (read > 0) {
               bufferend = read + offset;
            } else {
               bufferend = offset;
            }
            if (bufferend < buffer.length) {
               fileinputstream.close();
               fileinputstream = null;
            }
         } catch (IOException ex) {
            log.exception(ex, "read( %s, %d ) fileinputstream %s", buffer, offset, fileinputstream);
         }
      } else {
         return offset;
      }
      return bufferend;
   }

   /**
    * Advance to a file offset. Can only move forward, not back.
    * <p/>
    * @param offset
    */
   @Override
   public void setOffset(long offset) {
   }

   @Override
   public String printOffset() {
      return currentfile.getFilename();
   }

   @Override
   public long getBufferStart() {
      return bufferstart;
   }
}
