package io.github.repir.Extractor;

import io.github.repir.tools.Content.StructureReader;
import io.github.repir.tools.Content.StructureWriter;
import io.github.repir.tools.Lib.Log;
import java.io.EOFException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.TreeSet;

/**
 * TODO: replace section positions with array of positions to make clean match of u.s.citizens 
 * Data class that contains the tokenized content per channel for one document.
 */
public class Entity extends HashMap<String, EntityAttribute> implements io.github.repir.tools.Content.BufferDelayedWriter.Serialize {

   public static Log log = new Log(Entity.class);
   public byte[] content;
   public TreeSet<SectionPos> positions = new TreeSet<SectionPos>();
   private HashMap<String, ArrayList<SectionPos>> sectionpositions = new HashMap<String, ArrayList<SectionPos>>();
   public long offset; //  currently not send over MR, could be used for debugging

   public Entity() {
   }
   
   public void setContent( byte[] content ) {
      this.content = content;
   }

   public EntityAttribute get(String channelname) {
      if (channelname == null) {
         return null;
      }
      EntityAttribute d = super.get(channelname);
      if (d == null) {
         d = new EntityAttribute(this, (String) channelname);
         put(channelname, d);
      }
      return d;
   }

   @Override
   public void write(StructureWriter writer) {
      writer.writeByteBlock(content);
      writer.writeC(this.size());
      for (Map.Entry<String, EntityAttribute> entry : this.entrySet()) {
         writer.write(entry.getKey());
         entry.getValue().write(writer);
      }
      writer.writeC(sectionpositions.size());
      for (Map.Entry<String, ArrayList<SectionPos>> entry : sectionpositions.entrySet()) {
         writer.write(entry.getKey());
         writer.writeC(entry.getValue().size());
         for (SectionPos p : entry.getValue()) {
            p.write(writer);
         }
      }
   }

   @Override
   public void read(StructureReader reader) throws EOFException {
      content = reader.readByteBlock();
      int attributes = reader.readCInt();
      for (int i = 0; i < attributes; i++) {
         String attributename = reader.readString();
         EntityAttribute attribute = new EntityAttribute(this, attributename);
         attribute.read(reader);
         put(attributename, attribute);
      }
      int sections = reader.readCInt();
      for (int i = 0; i < sections; i++) {
         String sectionname = reader.readString();
         int sectionsize = reader.readCInt();
         ArrayList<SectionPos> list = new ArrayList<SectionPos>();
         for (int j = 0; j < sectionsize; j++) {
            SectionPos sectionpos = new SectionPos();
            sectionpos.read(reader);
            list.add(sectionpos);
         }
         sectionpositions.put(sectionname, list);
      }
   }

   public void addSectionPos(String section, int openlead, int open, int close, int closetrail) {
      ArrayList<SectionPos> list = sectionpositions.get(section);
      if (list == null) {
         list = new ArrayList<SectionPos>();
         sectionpositions.put(section, list);
      }
      list.add(new SectionPos(openlead, open, close, closetrail));
   }

   public ArrayList<SectionPos> getSectionPos(String section) {
      ArrayList<SectionPos> list = sectionpositions.get(section);
      return (list != null) ? list : new ArrayList<SectionPos>();
   }

   public static class SectionPos implements Comparable<SectionPos>, io.github.repir.tools.Content.BufferDelayedWriter.Serialize {

      public int openlead;
      public int open;
      public int close;
      public int closetrail;

      public SectionPos() {
      }

      public SectionPos(int openlead, int open, int close, int closetrail) {
         this.openlead = openlead;
         this.open = open;
         this.close = close;
         this.closetrail = closetrail;
      }

      @Override
      public void write(StructureWriter writer) {
         writer.writeC(openlead);
         writer.writeC(open);
         writer.writeC(close);
         writer.writeC(closetrail);
      }

      @Override
      public void read(StructureReader reader) throws EOFException {
         openlead = reader.readCInt();
         open = reader.readCInt();
         close = reader.readCInt();
         closetrail = reader.readCInt();
      }

      public int compareTo(SectionPos o) {
         return (open < o.open)?-1:((open > o.open)?1:0);
      }
   }
}
