package io.github.repir.TestSet;

import io.github.repir.Repository.DocLiteral;
import io.github.repir.Repository.Repository;
import io.github.repir.Retriever.Document;
import io.github.repir.Retriever.Query;
import io.github.repir.Retriever.Retriever;
import io.github.repir.Strategy.RetrievalModel;
import io.github.repir.Strategy.Strategy;
import io.github.htools.io.Datafile;
import io.github.htools.io.struct.StructuredTextCSV;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.TreeMap;

/**
 * File Structure to store the results of a {@link TestSet} in a text file.
 * @author jer
 */
public class ResultFileRR2 extends StructuredTextCSV implements ResultFile {

   TestSet ts;
   Repository repository;
  public IntField topic = this.addInt("topic", "", "\\s", "", " ");
  public IntField id = this.addInt("id", "", "\\s", "", " ");
  public IntField segment = this.addInt("segment", "", "\\s", "", " ");
  public StringField docid = this.addString("docid", "", "\\s", "", " ");
  public DoubleField score = this.addDouble("score", "", "($|\\s)", "", "");

   public ResultFileRR2(TestSet ts, String file) {
      super(ts.getResultsFile(file));
      this.ts = ts;
      repository = ts.repository;
   }

   public ResultFileRR2(Repository repository, Datafile df) {
      super(df);
      this.repository = repository;
   }

   public ResultFileRR2(Datafile df) {
      super(df);
   }

   /**
    * @return Collection of {@link Query}s that are specified in the {@link TestSet}, 
    * containing the results stored in the given file. 
    */
   public ArrayList<Query> getResults() {
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
      while (nextRecord()) {
         if (ts.topics.containsKey(topic.get())) {
            if (current != topic.get()) {
               current = topic.get();
               if (results.containsKey(current)) {
                  currentrm = results.get(current);
               } 
            }
            Document doc = currentrm.createDocument(id.get(), segment.get());
            doc.reportdata[0] = docid.get();
            doc.score = score.get();
            doc.retrievalmodel = currentrm;
            currentrm.query.add(doc);
         }
      }
      this.closeRead();
      ArrayList<Query> r = new ArrayList<Query>();
      for (Strategy q : results.values()) {
         q.query.originalquery = ts.topics.get(q.query.id).query;
         r.add(q.query);
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
         int row = 0;
         for (Document d : q.getQueryResults()) {
            topic.set(q.getID());
            id.set(d.docid);
            segment.set(d.partition);
            docid.set(d.getCollectionID());
            score.set(d.score);
            write();
         }
      }
      closeWrite();
   }
}
