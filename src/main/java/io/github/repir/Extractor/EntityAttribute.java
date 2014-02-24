package io.github.repir.Extractor;

import io.github.repir.tools.Content.StructureReader;
import io.github.repir.tools.Content.StructureWriter;
import io.github.repir.tools.Lib.Log;
import java.io.EOFException;
import java.util.ArrayList;
import io.github.repir.tools.DataTypes.ByteArrayPos;

/**
 * Data class to hold the chunks that belong to one tokenized channel of a
 * document.
 */
public class EntityAttribute extends ArrayList<String> implements io.github.repir.tools.Content.BufferDelayedWriter.Serialize {
   public static Log log = new Log( EntityAttribute.class );
   public String channel;
   public String contentstring;
   public int tokenized[];
   public Entity entity;

   public EntityAttribute(Entity entity, String name) {
      this.entity = entity;
      channel = name;
   }

   @Override
   public EntityAttribute clone() {
      EntityAttribute c = new EntityAttribute( entity, channel );
      c.addAll(this);
      return c;
   }
   
   /**
    * Add the separated chunks of content to a StringBuilder
    * <p/>
    * @param r
    * @param seperator
    * @return
    */
   public StringBuilder getContent(StringBuilder r, String seperator) {
      boolean first = true;
      for (String chunk : this) {
         if (first) {
            first = false;
         } else {
            r.append(" ");
         }
         r.append(chunk.toString());
      }
      //log.info("getCOntent %s", r.toString());
      return r;
   }

   /**
    * Returns a space seperated String of all content Tokenized for the channel.
    * <p/>
    * @return
    */
   public String getContentStr() {
      if (contentstring == null) {
         StringBuilder r = new StringBuilder();
         getContent(r, " ");
         contentstring = r.toString();
      }
      return contentstring;
   }

   @Override
   public void write(StructureWriter writer) {
      writer.write(size());
      for (String token : this) {
         writer.write(token);
      }
   }

   @Override
   public void read(StructureReader reader) throws EOFException {
      int size = reader.readInt();
      for (int i = 0; i < size; i++) {
         add(reader.readString());
      }
   }
}
