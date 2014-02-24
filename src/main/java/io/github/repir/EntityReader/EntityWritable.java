package io.github.repir.EntityReader;

import io.github.repir.tools.Content.BufferDelayedWriter;
import io.github.repir.tools.Content.BufferReaderWriter;
import io.github.repir.tools.Lib.Log;
import io.github.repir.Extractor.Entity;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import org.apache.hadoop.io.Writable;

public class EntityWritable implements Writable {

   public static Log log = new Log(EntityWritable.class);
   private BufferDelayedWriter bdw = new BufferDelayedWriter();
   public Entity entity;

   public void writeByte(int b) {
      bdw.writeUB(b);
   }

   public void writeBytes(byte b[], int pos, int length) {
      bdw.write(b, pos, length);
   }

   public void storeContent() {
      entity.content = bdw.getBytes();
   }

   @Override
   public void write(DataOutput out) throws IOException {
      entity.write(bdw);
      out.write(bdw.getAsByteBlock());
   }

   @Override
   public void readFields(DataInput in) throws IOException {
      BufferReaderWriter reader = new BufferReaderWriter(in);
      entity = new Entity();
      entity.read(reader);
   }
}
