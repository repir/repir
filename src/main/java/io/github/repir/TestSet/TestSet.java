package io.github.repir.TestSet;

import io.github.repir.tools.Content.Datafile;
import io.github.repir.tools.Content.FSDir;
import io.github.repir.tools.Content.FSFile;
import io.github.repir.Extractor.Entity;
import io.github.repir.Extractor.Extractor;
import io.github.repir.Extractor.ExtractorTestSet;
import io.github.repir.Repository.DocLiteral;
import io.github.repir.Repository.Repository;
import io.github.repir.Retriever.Document;
import io.github.repir.Retriever.Retriever;
import io.github.repir.Retriever.Query;
import io.github.repir.tools.Lib.Log;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeSet;
import io.github.repir.tools.Content.Dir;
import io.github.repir.tools.Content.HDFSDir;
import io.github.repir.tools.DataTypes.Configuration;
import io.github.repir.tools.Lib.HDTools;

public class TestSet {

   public static Log log = new Log(TestSet.class);
   public Repository repository;
   public HashMap<Integer, Topic> topics;
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
      topics = new HashMap<Integer, Topic>();
      this.qrels = new HashMap<Integer, HashMap<String, Integer>>();
      topics.put(0, new Topic(0, null, "test"));
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

//   public TestSet(Query referencequery) {
//      topics = new HashMap<Integer, Topic>();
//      this.qrels = new HashMap<Integer, HashMap<String, Integer>>();
//      topics.put(referencequery.id, new Topic( referencequery.id, null, referencequery.originalquery));
//      this.qrels.put(referencequery.id, getQrels( referencequery ));
//   }
//   
//   public static HashMap<String, Integer> getQrels( Query q ) {
//      HashMap<String, Integer> qrels = new HashMap<String, Integer>();
//      for ( Document d : q.queryresults ) {
//         qrels.put(d.getLiteral("DocLiteral:collectionid"), 1);
//      }
//      return qrels;
//   }
//
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
      Iterator<Map.Entry<Integer, Topic>> iter = topics.entrySet().iterator();
      while (iter.hasNext()) {
         Map.Entry<Integer, Topic> entry = iter.next();
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
      Integer relevant = getQrels().get(topic).get(collectionidfeature.valueReported(doc));
      return (relevant == null) ? 0 : relevant;
   }

   public static HashMap<Integer, Topic> readTopics(Repository repository) {
      Datafile df = new Datafile(repository.getConfigurationString("testset.topics"));
      if (!df.exists()) {
         //String filename = repository.getConfigurationString("testset.topics").substring(repository.getConfigurationString("repir.dir").length());
         df = new Datafile(repository.getFS(), repository.getConfigurationString("testset.topics"));
      }
      if (!df.exists()) {
         log.fatal("topicfile %s does not exists", df.getFullPath());
      }
      String topicreaderclass = repository.getConfigurationString("testset.topicreader", "TrecTopic");
      //log.info("%s", topicreaderclass);
      Class clazz = io.github.repir.tools.Lib.ClassTools.toClass(topicreaderclass, TrecTopicReader.class.getPackage().getName());
      Constructor c = io.github.repir.tools.Lib.ClassTools.getAssignableConstructor(clazz, TrecTopicReader.class, Datafile.class);
      //TrecTopicReader tr = new TrecTopic(df);
      TrecTopicReader tr = (TrecTopicReader) io.github.repir.tools.Lib.ClassTools.construct(c, df);
      HashMap<Integer, Topic> topics = tr.getTopics();
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
            //String filename = repository.getConfigurationString("testset.topics").substring(repository.getConfigurationString("repir.dir").length());
         }
         list.addAll(d.matchDatafiles(f.getFilename()));
      }
      return list;
   }

   public static HashMap<Integer, HashMap<String, Integer>> readQrels(Repository repository) {
      String qrelreadername = repository.getConfigurationString("testset.qrelreader", TrecQrels.class.getSimpleName());
      Class qrelreaderclass = io.github.repir.tools.Lib.ClassTools.toClass(qrelreadername, TestSet.class.getPackage().getName());
      Method readqrels = io.github.repir.tools.Lib.ClassTools.getMethod(qrelreaderclass, "readQrels", Collection.class);

      HashMap<Integer, HashMap<String, Integer>> qrels =
              (HashMap<Integer, HashMap<String, Integer>>) io.github.repir.tools.Lib.ClassTools.invoke(readqrels, null, getQrelFiles(repository));
      //HashMap<Integer, HashMap<String, Integer>> qrel = TrecQrels.readQrels(d.matchDatafiles(f.getFilename()));
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
