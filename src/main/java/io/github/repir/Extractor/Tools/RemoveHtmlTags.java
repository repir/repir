package io.github.repir.Extractor.Tools;

import io.github.repir.tools.ByteRegex.ByteRegex;
import io.github.repir.tools.ByteRegex.ByteRegex.Pos;
import io.github.repir.tools.DataTypes.Tuple2;
import io.github.repir.Extractor.Entity;
import io.github.repir.Extractor.Extractor;
import io.github.repir.tools.Lib.ByteTools;
import static io.github.repir.tools.Lib.ByteTools.*;
import io.github.repir.tools.Lib.Log;
import java.util.ArrayList;

/**
 * Removes HTML tags from the content
 * <p/>
 * @author jeroen
 */
public class RemoveHtmlTags extends ExtractorProcessor {

   public static Log log = new Log(RemoveHtmlTags.class);
   public boolean tagname[] = new boolean[128];
   public byte tagstart[] = "<".getBytes();
   public byte tagend[] = ">".getBytes();
   public ByteRegex start = new ByteRegex("<[/!]?[A-Za-z][A-Za-z0-9]*");
   public ByteRegex end = new ByteRegex("\\Q>"); // quote safe search for tag end

   private RemoveHtmlTags(Extractor extractor, String process) {
      super(extractor, process);
      for (int i = 0; i < 128; i++) {
         tagname[i] = (i >= 'a' && i <= 'z') || (i >= 'A' && i <= 'Z') || i == '/';
      }
   }

   public void process2(Entity entity, Entity.SectionPos section, String attribute) {
      int startpos = section.open;
      int endpos = section.close;
      //log.info("process %d %d", startpos, endpos);
      Tuple2<Integer, Integer> pos;
      while ((pos = find(entity.content, tagstart, tagend, startpos, endpos, false, true)) != null) {
         if (tagname[entity.content[pos.value1 + 1]]) {
            //log.info("remove %d %d %s", pos.value1, pos.value2 + 1, new String(entity.content, pos.value1, pos.value2 - pos.value1 + 1));
            for (int p = pos.value1; p <= pos.value2; p++) {
               entity.content[p] = 32;
            }
            startpos = pos.value2 + 1;
         } else {
            startpos = pos.value1 + 1;
         }
      }
   }

   @Override
   public void process(Entity entity, Entity.SectionPos section, String attribute) {
      int startpos = section.open;
      int endpos = section.close;
      ArrayList<Pos> pos = start.findAll(entity.content, startpos, endpos);
      for (int i = 0; i < pos.size(); i++) {
         Pos p = pos.get(i);
         Pos e = end.find(entity.content, p.end, endpos);
         while (e.found() && !ByteTools.checkQuotes(entity.content, p.end, e.start)) {
            e = end.find(entity.content, e.start + 1, endpos);
         }
         if (e.found() && (i == pos.size() - 1 || e.start < pos.get(i + 1).start)) {
            for (int a = p.start; a < e.end; a++) {
               entity.content[a] = 32;
            }
         } else {
         }
      }
   }
}
