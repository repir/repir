package io.github.repir.Extractor;

import io.github.repir.tools.Content.Datafile;
import io.github.repir.tools.Content.FSFile;
import io.github.repir.tools.Lib.Log;

/**
 * Implements Reader class for a file
 */
public class ReaderFile implements Reader {

   public static Log log = new Log(ReaderFile.class);
   public int cycles = 0;       // the number of tiems the buffer has been read
   public int maxcycles = 0;    // a stopping criterion to read a number of cycles 
   public long bufferstart = 0; // indicates the current offset to the start of the buffer
   FSFile inputfile;
   Datafile filein;
   private long fileoffset;
   private int readlength;
   private int bufferend = 0;

   public ReaderFile(Datafile file) {
      filein = file;
      filein.openRead();
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
      //log.info("cycle %d %d %d", cycles, offset, bufferstart);
      //if (offset >= buffer.length)
      //    log.crash();
      if (maxcycles > 0 && cycles >= maxcycles) {
         return -1;
      }
      cycles++;
      if (offset >= 0) {
         bufferstart += readlength;
         readlength = filein.readBytes(fileoffset, buffer, offset, buffer.length - offset);
         if (readlength > 0) {
            bufferend = readlength + offset;
            fileoffset += readlength;
         } else {
            bufferend = offset;
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
      filein.setOffset(offset);
      bufferstart = offset;
   }

   @Override
   public String printOffset() {
      return inputfile.getFilename() + " " + bufferstart;
   }

   @Override
   public long getBufferStart() {
      return bufferstart;
   }
}
