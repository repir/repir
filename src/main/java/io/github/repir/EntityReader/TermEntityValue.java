package io.github.repir.EntityReader;

import io.github.repir.tools.Content.BufferDelayedWriter;
import io.github.repir.tools.Content.BufferReaderWriter;
import io.github.repir.tools.Lib.Log;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.EOFException;
import java.io.IOException;
import org.apache.hadoop.io.BytesWritable;

public class TermEntityValue extends BytesWritable {

   public static Log log = new Log(TermEntityValue.class);
   public BufferReaderWriter reader = new BufferReaderWriter();
   public BufferDelayedWriter writer = new BufferDelayedWriter();

   public TermEntityValue() {
   }

   @Override
   public void readFields(DataInput in) throws IOException {
      try {
         int length = in.readInt();
         byte b[] = new byte[length];
         in.readFully(b);
         reader.setBuffer(b);
      } catch (EOFException ex) {
         throw new IOException(ex);
      }
   }

   @Override
   public void write(DataOutput out) throws IOException {
      byte b[] = writer.getAsByteBlock();
      out.write(b);
   }
}
