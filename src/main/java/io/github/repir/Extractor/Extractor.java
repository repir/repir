package io.github.repir.Extractor;

import io.github.repir.EntityReader.Entity;
import io.github.repir.tools.ByteSearch.ByteRegex;
import io.github.repir.EntityReader.Entity.Section;
import io.github.repir.EntityReader.EntityRemovedException;
import io.github.repir.Extractor.Tools.ExtractorProcessor;
import io.github.repir.Extractor.Tools.SectionMarker;
import io.github.repir.Repository.Repository;
import io.github.repir.tools.Lib.ClassTools;
import io.github.repir.tools.Lib.Log;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import io.github.repir.tools.DataTypes.Configuration;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The Extractor is a generic processor that converts {@link Entity}s submitted
 * by an {@link EntityReader} into extracted values to store as features.
 * <p/>
 * Extraction proceeds in 3 phases. (1) the raw byte content of the
 * {@link Entity} is pre-processed by the modules configured as
 * "extractor.preprocess". Typical operations are converting tagnames to
 * lowercase, converting unicodes to ASCII, and removing irrelevant parts, to
 * simplify further processing. (2) mark sections in the content using the
 * modules configured with "extractor.sectionmarker". One default section is
 * "all" to indicate all content. Other {@link SectionMarker}s can process an
 * existing section, to mark subsections. (3) each section can have its own (set
 * of) processing pipeline(s), configured with "extractor.sectionprocess". For
 * the marked sections the modules configured with "extractor.<processname>" are
 * performed sequentially.
 *
 * @author jeroen
 */
public class Extractor {

   public static Log log = new Log(Extractor.class);
   public Repository repository;
   public Configuration conf;
   protected ArrayList<ExtractorProcessor> preprocess = new ArrayList<ExtractorProcessor>();
   protected HashMap<String, ArrayList<ExtractorProcessor>> processor = new HashMap<String, ArrayList<ExtractorProcessor>>();
   protected HashSet<String> processes = new HashSet<String>();
   protected ArrayList<String> sections = new ArrayList<String>();
   protected HashMap<String, ArrayList<SectionMarker>> sectionmarkers = new HashMap<String, ArrayList<SectionMarker>>();
   protected ArrayList<ExtractorPatternMatcher> patternmatchers = new ArrayList<ExtractorPatternMatcher>();
   protected ArrayList<SectionMarker> markers = new ArrayList<SectionMarker>();
   protected ArrayList<SectionProcess> processors = new ArrayList<SectionProcess>();
   protected ByteRegex sectionstart;

   public Extractor(Repository repository) {
      this.repository = repository;
      conf = repository.getConfiguration();
      for (String p : conf.getStrings("extractor.preprocess", new String[0])) {
         Class clazz = ClassTools.toClass(p, getClass().getPackage().getName() + ".Tools");
         Constructor c = ClassTools.getAssignableConstructor(clazz, ExtractorProcessor.class, Extractor.class, String.class);
         this.preprocess.add((ExtractorProcessor) ClassTools.construct(c, this, "preprocess"));
      }
      init();
      createPatternMatchers();
   }

   public void init() {
      for (String p : conf.getStrings("extractor.sectionprocess", new String[0])) {
         String part[] = p.split(" +");
         linkSectionToProcess(part[0], part[1], (part.length > 2) ? part[2] : null);
      }
      for (String sectionmarker : conf.getStrings("extractor.sectionmarker", new String[0])) {
         String part[] = sectionmarker.split(" +");
         createSectionMarker(part[2], part[0], part[1]);
      }
      for (String process : processes) {
         createProcess(process);
      }
   }

   public void createPatternMatchers() {
      for (String section : sections) {
         patternmatchers.add(new ExtractorPatternMatcher(this, section, sectionmarkers.get(section)));
      }
   }

   public void linkSectionToProcess(String section, String processname, String attributename) {
      processors.add(new SectionProcess(section, processname, attributename));
      processes.add(processname);
   }

   public void createSectionMarker(String sectionmarkername, String inputsection, String outputsection) {
      Class clazz = ClassTools.toClass(sectionmarkername, getClass().getPackage().getName() + ".Tools");
      Constructor c = ClassTools.getAssignableConstructor(clazz, SectionMarker.class, Extractor.class, String.class, String.class);
      SectionMarker marker = (SectionMarker) ClassTools.construct(c, this, inputsection, outputsection);
      ArrayList<SectionMarker> list = sectionmarkers.get(inputsection);
      if (list == null) {
         list = new ArrayList<SectionMarker>();
         sectionmarkers.put(inputsection, list);
      }
      list.add(marker);
      if (!sections.contains(inputsection)) {
         sections.add(inputsection);
      }
   }

   public String getConfigurationString(String process, String identifier, String defaultstring) {
      return conf.getSubString("extractor." + process + "." + identifier, defaultstring);
   }

   public String[] getConfigurationStrings(String process, String identifier, String defaultstring[]) {
      return conf.getStrings("extractor." + process + "." + identifier, defaultstring);
   }

   public boolean getConfigurationBoolean(String process, String identifier, boolean defaultboolean) {
      return conf.getBoolean("extractor." + process + "." + identifier, defaultboolean);
   }

   public int getConfigurationInt(String process, String identifier, int defaultint) {
      return conf.getInt("extractor." + process + "." + identifier, defaultint);
   }

   public float getConfigurationFloat(String process, String identifier, float defaultfloat) {
      return conf.getFloat("extractor." + process + "." + identifier, defaultfloat);
   }

   public void createProcess(String process) {
      ArrayList<ExtractorProcessor> list = new ArrayList<ExtractorProcessor>();
      this.processor.put(process, list);
      for (String processor : conf.getStrings("extractor." + process, new String[]{process})) {
         Class clazz = ClassTools.toClass(processor, getClass().getPackage().getName() + ".Tools");
         Constructor c = ClassTools.getAssignableConstructor(clazz, ExtractorProcessor.class, Extractor.class, String.class);
         list.add((ExtractorProcessor) ClassTools.construct(c, this, process));
         //log.info("createProcess %s %s", process, processor);
      }
   }

   public ExtractorProcessor findProcessor(String process, Class clazz) {
      for (ExtractorProcessor p : processor.get(process)) {
         if (clazz.equals(p.getClass())) {
            return p;
         }
      }
      return null;
   }

   public Section getAll(Entity entity) {
      ArrayList<Section> list = entity.getSectionPos("all");
      if (list.size() == 0) {
         entity.addSectionPos("all", 0, 0, entity.content.length, entity.content.length);
         list = entity.getSectionPos("all");
      }
      return list.get(0);
   }

   /**
    * Processes the entity according to the configured extraction process.
    *
    * @param entity
    */
   public void process(Entity entity) {
      int bufferpos = 0;
      int bufferend = entity.content.length;
      //log.info("extract() bufferpos %d bufferend %d", bufferpos, bufferend);
      if (bufferpos >= bufferend) {
         return;
      }
      try {
         for (ExtractorProcessor proc : this.preprocess) {
            proc.process(entity, getAll(entity), null);
         }
         this.processSectionMarkers(entity, bufferpos, bufferend);
         for (SectionProcess p : this.processors) {
            //log.info("process %s sections %s", p.process, p.section);
            for (Section section : entity.getSectionPos(p.section)) {
               //log.info("section %s %d %d", p.section, section.open, section.close );
               for (ExtractorProcessor proc : processor.get(p.process)) {
                  //log.info("proc.process %s", proc.toClass().getCanonicalName()); 
                  proc.process(entity, section, p.entityattribute);
               }
            }
         }
      } catch (EntityRemovedException ex) {
      }
   }

   public void removeProcessor(String process, Class processclass) {
      ArrayList<ExtractorProcessor> get = processor.get(process);
      Iterator<ExtractorProcessor> iter = get.iterator();
      while (iter.hasNext()) {
         ExtractorProcessor p = iter.next();
         if (processclass.isAssignableFrom(p.getClass())) {
            iter.remove();
         }
      }
   }

   void processSectionMarkers(Entity entity, int bufferpos, int bufferend) {
      //entity.addSectionPos( "all", bufferpos, bufferpos, bufferend, bufferend );
      for (int section = 0; section < sections.size(); section++) {
         String sectionname = sections.get(section);
         //log.info("processSectionMarkers %s", sectionname);
         ExtractorPatternMatcher patternmatcher = patternmatchers.get(section);
         for (Section pos : entity.getSectionPos(sectionname)) {
            patternmatcher.processSectionMarkers(entity, pos.open, pos.close);
         }
      }
   }

   private class SectionProcess {

      String section;
      String process;
      String entityattribute;

      public SectionProcess(String section, String process, String entityattribute) {
         this.section = section;
         this.process = process;
         this.entityattribute = entityattribute;
      }
   }
}
