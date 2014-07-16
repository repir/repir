package io.github.repir.EntityReader.MapReduce;

import io.github.repir.tools.Buffer.BufferDelayedWriter;
import io.github.repir.tools.Buffer.BufferReaderWriter;
import io.github.repir.tools.Lib.Log;
import io.github.repir.EntityReader.Entity;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import org.apache.hadoop.io.Writable;

/**
 * Wraps an {@link Entity} as read by an {@link EntityReader} as a {@link Writable}
 * to allow Hadoop to submit it to a Mapper.
 * @author jer
 */
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
