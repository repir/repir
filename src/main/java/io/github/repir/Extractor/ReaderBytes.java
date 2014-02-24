package io.github.repir.Extractor;

import io.github.repir.tools.Lib.Log;
import io.github.repir.Extractor.Reader;
import org.apache.hadoop.io.BytesWritable;

public class ReaderBytes implements Reader {

   public static Log log = new Log(ReaderBytes.class);
   public long bytesoffset;
   public int bytesend;
   public byte bytes[];

   public ReaderBytes(String text) {
      bytes = text.getBytes();
      bytesoffset = 0;
      bytesend = bytes.length;
   }

   public ReaderBytes() {
   }

   public void setBuffer(BytesWritable b) {
      bytes = b.getBytes();
      bytesoffset = 0;
      bytesend = b.getLength();
   }

   @Override
   public int read(byte[] buffer, int bufferpos) {
      int todo = Math.min(buffer.length - bufferpos, (int) (bytesend - bytesoffset));
      int end = bufferpos + todo;
      int diff = (int) (bytesoffset - bufferpos);
      for (int i = bufferpos; i < end; i++) {
         buffer[i] = bytes[i + diff];
      }
      bytesoffset += todo;
      return end;
   }

   @Override
   public void setOffset(long offset) {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   @Override
   public String printOffset() {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   @Override
   public long getBufferStart() {
      return bytesoffset;
   }
}
