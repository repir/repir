package io.github.repir.TestSet;

import io.github.repir.tools.Content.Datafile;
import io.github.repir.tools.Content.StructuredTextfileCSV;
import io.github.repir.Repository.DocLiteral;
import io.github.repir.Repository.Repository;
import io.github.repir.Retriever.Document;
import io.github.repir.Retriever.Retriever;
import io.github.repir.Retriever.Query;
import io.github.repir.Strategy.Strategy;
import java.util.ArrayList;
import java.util.Collection;
import java.util.TreeMap;
import io.github.repir.Strategy.RetrievalModel;
import java.util.Map;

/**
 * File Structure to store the results of a {@link TestSet} in a text file.
 * @author jer
 */
public class ResultFile extends StructuredTextfileCSV {

   TestSet ts;
   Repository repository;
   public IntField topic = this.addInt("topic");
   public IntField id = this.addInt("id");
   public IntField segment = this.addInt("segment");
   public StringField docid = this.addString("docid");
   public DoubleField score = this.addDouble("score");

   public ResultFile(TestSet ts, String file) {
      super(ts.getResultsFile(file));
      this.ts = ts;
      repository = ts.repository;
   }

   public ResultFile(Repository repository, Datafile df) {
      super(df);
      this.repository = repository;
   }

   public ResultFile(Datafile df) {
      super(df);
   }

   /**
    * @return Collection of {@link Query}s that are specified in the {@link TestSet}, 
    * containing the results stored in the given file. 
    */
   public Collection<Query> getResults() {
      TreeMap<Integer, RetrievalModel> results = new TreeMap<Integer, RetrievalModel>();
      int current = -1;
      Retriever retriever = new Retriever(repository);
      RetrievalModel currentrm = null;
      this.openRead();
      for (Query q : ts.getQueries(retriever)) {
         q.addFeature(ts.repository.getCollectionIDFeature());
         currentrm = RetrievalModel.create(retriever, q);
         results.put(q.id, currentrm);
      }
      while (this.next()) {
         if (ts.topics.containsKey(topic.value)) {
            if (current != topic.value) {
               current = topic.value;
               if (results.containsKey(current)) {
                  currentrm = results.get(current);
               } 
            }
            Document doc = currentrm.createDocument(id.value, segment.value);
            doc.reportdata[0] = docid.value;
            doc.score = score.value;
            doc.retrievalmodel = currentrm;
            currentrm.query.add(doc);
         }
      }
      this.closeRead();
      ArrayList<Query> r = new ArrayList<Query>();
      for (Strategy q : results.values()) {
         q.query.originalquery = ts.topics.get(q.query.id).query;
         r.add(q.query);
         q.query.setQueryResults();
         q.query.resultsarraylist = null;
      }
      return r;
   }

   /**
    * Stores the results in the list of {@link Query}s, in the file.
    * @param results 
    */
   public void writeresults(ArrayList<Query> results) {
      openWrite();
      for (Query q : results) {
         DocLiteral collectionid = repository.getCollectionIDFeature();
         int row = 0;
         for (Document d : q.queryresults) {
            topic.write(q.getID());
            id.write(d.docid);
            segment.write(d.partition);
            docid.write(collectionid.valueReported(d, 0));
            score.write(d.score);
         }
      }
      closeWrite();
   }
}
