package io.github.repir.TestSet;

import io.github.repir.TestSet.Qrel.QrelReaderTREC;
import io.github.repir.TestSet.Topic.TopicReader;
import io.github.repir.TestSet.Metric.TestSetTopic;
import io.github.repir.tools.Content.Datafile;
import io.github.repir.tools.Content.FSFile;
import io.github.repir.EntityReader.Entity;
import io.github.repir.Extractor.Extractor;
import io.github.repir.Extractor.ExtractorTestSet;
import io.github.repir.Repository.DocLiteral;
import io.github.repir.Repository.Repository;
import io.github.repir.Retriever.Document;
import io.github.repir.Retriever.Retriever;
import io.github.repir.Retriever.Query;
import io.github.repir.TestSet.Qrel.QrelReader;
import io.github.repir.TestSet.Topic.TopicReaderTREC;
import io.github.repir.tools.Lib.Log;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeSet;
import io.github.repir.tools.Content.Dir;
import io.github.repir.tools.DataTypes.Configuration;
import io.github.repir.tools.Lib.ClassTools;
import io.github.repir.tools.Lib.HDTools;
import java.util.Collection;

/**
 * A TestSet contains a set of {@link Query}s, for a collection, for which relevance
 * judgments can be used to compute IR metrics to evaluate the performance of systems.
 * The {@link Query}s in a TestSet are usually read from a source text file that 
 * was supplied for an existing collection, e.g. TREC. The queries can be configured
 * by supplying a filename as "testset.topics". The relevance judgments can be configured
 * using "testset.qrels". To use the original source files, which can have one of
 * many different formats/structures, a TopicReader and QrelReader must be configured 
 * using "testset.topicreader" and "testset.qrelreader" to specify the class names.
 * @author jer
 */
public class TestSet {

   public static Log log = new Log(TestSet.class);
   public Repository repository;
   public HashMap<Integer, TestSetTopic> topics;
   private HashMap<Integer, HashMap<String, Integer>> qrels;
   public DocLiteral collectionid;
   public Extractor extractor;
   HashMap<Integer, String> tunedParameters;
   private int possiblequeries = -1;

   private TestSet() {
   }

   public TestSet(Repository repository) {
      this.repository = repository;
      topics = readTopics(repository);
      tunedParameters = getTunedParameters();
   }

   protected TestSet(HashMap<String, Integer> qrels) {
      topics = new HashMap<Integer, TestSetTopic>();
      this.qrels = new HashMap<Integer, HashMap<String, Integer>>();
      topics.put(0, new TestSetTopic(0, null, "test"));
      this.qrels.put(0, qrels);
   }

   protected HashMap<Integer, String> getTunedParameters() {
      HashMap<Integer, String> parameters = new HashMap<Integer, String>();
      String parameterfile = repository.getParameterFile();
      if (parameterfile.length() > 0 && FSFile.exists(parameterfile)) {
         Configuration queryparameters = HDTools.readConfigNoMR(parameterfile);
         for (int i : topics.keySet()) {
            String p = queryparameters.get("query." + i, null);
            if (p != null) {
               parameters.put(i, p);
            }
         }
      }
      return parameters;
   }

   public ResultFile getBaseline() {
      return getResults(repository.getConfigurationString("testset.baselineextension"));
   }

   public ResultFile getResults(String f) {
      return new ResultFile(this, f);
   }

   public HashMap<Integer, HashMap<String, Integer>> getQrels() {
      if (qrels == null) {
         qrels = readQrels(repository);
      }
      return qrels;
   }

   public void purgeTopics() {
      getQrels();
      Iterator<Map.Entry<Integer, TestSetTopic>> iter = topics.entrySet().iterator();
      while (iter.hasNext()) {
         Map.Entry<Integer, TestSetTopic> entry = iter.next();
         if (!qrels.containsKey(entry.getKey())) {
            iter.remove();
         } else {
            boolean exists = false;
            for (int relevance : qrels.get(entry.getKey()).values()) {
               if (relevance > 0) {
                  exists = true;
                  break;
               }
            }
            if (!exists) {
               iter.remove();
            }
         }
      }
   }

   public ArrayList<Query> getQueries(Retriever retriever) {
      ArrayList<Query> list = new ArrayList<Query>();
      for (Integer topic : topics.keySet()) {
         list.add(getQuery(topic, retriever));
      }
      return list;
   }

   public Datafile getResultsFile(String file) {
      return getResultsFile(repository, file);
   }

   public static String getName(Repository repository) {
      return repository.getConfigurationString("testset.name");
   }

   public static Datafile getResultsFile(Repository repository, String file) {
      String filename = repository.getConfigurationString("testset.results") + "/"
              + getName(repository) + "." + file;
      return new Datafile(filename);
   }

   public ArrayList<Integer> getTopicIDs() {
      TreeSet<Integer> list = new TreeSet<Integer>(topics.keySet());
      return new ArrayList<Integer>(list);
   }

   public Query getQuery(int id, Retriever retriever) {
      String q = topics.get(id).query;
      q = filterString(q);
      Query query = retriever.constructQueryRequest(id, q);
      query.domain = topics.get(id).domain;
      query.addFeature("DocLiteral:collectionid");
      query.setConfiguration(tunedParameters.get(id));
      return query;
   }

   public int possibleQueries() {
      if (possiblequeries < 0) {
         possiblequeries = 0;
         for (HashMap<String, Integer> topic : getQrels().values()) {
            for (Integer rel : topic.values()) {
               if (rel > 0) {
                  possiblequeries++;
                  break;
               }
            }
         }
      }
      return possiblequeries;
   }

   public int isRelevant(int topic, Document doc) {
      DocLiteral collectionidfeature = doc.retrievalmodel.repository.getCollectionIDFeature();
      Integer relevant = getQrels().get(topic).get(collectionidfeature.valueReported(doc, 0));
      return (relevant == null) ? 0 : relevant;
   }

   /**
    * Return a Map of topics configured in the Repository. "testset.topicreader" 
    * contains the class name of the TopicReader used and "testset.topics" a (list of)
    * files that describe the topics.
    * @param repository
    * @return 
    */
   public static HashMap<Integer, TestSetTopic> readTopics(Repository repository) {
      Datafile df = new Datafile(repository.getConfigurationString("testset.topics"));
      if (!df.exists()) {
         df = new Datafile(repository.getFS(), repository.getConfigurationString("testset.topics"));
      }
      if (!df.exists()) {
         log.fatal("topicfile %s does not exists", df.getFullPath());
      }
      String topicreaderclass = repository.getConfigurationString("testset.topicreader", TopicReaderTREC.class.getSimpleName());
      Class clazz = io.github.repir.tools.Lib.ClassTools.toClass(topicreaderclass, TopicReader.class.getPackage().getName());
      Constructor c = io.github.repir.tools.Lib.ClassTools.getAssignableConstructor(clazz, TopicReader.class, Datafile.class);

      TopicReader tr = (TopicReader) io.github.repir.tools.Lib.ClassTools.construct(c, df);
      HashMap<Integer, TestSetTopic> topics = tr.getTopics();
      for (String t : repository.getConfigurationSubStrings("testset.droppedtopics")) {
         if (t.trim().length() > 0)
            topics.remove(Integer.parseInt(t.trim()));
      } 
      return topics;
   }

   public static ArrayList<Datafile> getQrelFiles(Repository repository) {
      String qr = repository.getConfigurationString("testset.qrels");
      if (qr == null) {
         log.fatal("testset.qrels not set");
      }
      String qrs[] = qr.split(",");
      ArrayList<Datafile> list = new ArrayList<Datafile>();
      for (String p : qrs) {
         Datafile f = new Datafile(p);
         Dir d = f.getDir();
         if (!d.exists()) {
            f = new Datafile(repository.getFS(), p);
            d = f.getDir();
         }
         list.addAll(d.matchDatafiles(f.getFilename()));
      }
      return list;
   }

   /**
    * Return a Map of relevance judgments as configured in the {@link Reposotory}.
    * "testset.qrelreader" contains the classname of the QrelReader to be used and 
    * "testset.qrels" contains a (list of) filenames that contain the judgments.
    * @param repository
    * @return 
    */
   public static HashMap<Integer, HashMap<String, Integer>> readQrels(Repository repository) {
      String qrelreadername = repository.getConfigurationString("testset.qrelreader", QrelReaderTREC.class.getSimpleName());
      Class qrelreaderclass = ClassTools.toClass(qrelreadername, QrelReader.class.getPackage().getName());
      Constructor constr = ClassTools.getAssignableConstructor(qrelreaderclass, QrelReader.class, Datafile.class);
      
      HashMap<Integer, HashMap<String, Integer>> qrels = new HashMap<Integer, HashMap<String, Integer>>();
      for (Datafile df : getQrelFiles(repository)) {
         QrelReader qr = (QrelReader)ClassTools.construct(constr, df);
         qrels.putAll(qr.getQrels());
      }

      filterQrels(repository, qrels);
      return qrels;
   }

   public static void filterQrels(Repository repository, HashMap<Integer, HashMap<String, Integer>> qrels) {
      String docfilter = repository.getConfigurationString("testset.docfilter");
      if (docfilter.length() > 0) {
         for (HashMap<String, Integer> set : qrels.values()) {
            Iterator<String> docs = set.keySet().iterator();
            while (docs.hasNext()) {
               String doc = docs.next();
               if (!doc.startsWith(docfilter)) {
                  docs.remove();
               }
            }
         }
      }
   }

   public String filterString(String query) {
      if (extractor == null) {
         extractor = new ExtractorTestSet(repository);
      }
      Entity entity = new Entity();
      entity.setContent(query.getBytes());
      extractor.process(entity);
      return io.github.repir.tools.Lib.ByteTools.toString(entity.content, 0, entity.content.length);
   }
}
