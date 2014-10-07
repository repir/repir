package io.github.repir.TestSet;

import io.github.repir.Repository.DocLiteral;
import io.github.repir.Repository.Repository;
import io.github.repir.Retriever.Document;
import io.github.repir.Retriever.Query;
import io.github.repir.Retriever.Retriever;
import io.github.repir.Strategy.RetrievalModel;
import io.github.repir.Strategy.Strategy;
import io.github.repir.tools.Content.Datafile;
import io.github.repir.tools.Structure.StructuredTextCSV;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.TreeMap;

/**
 * File Structure to store the results of a {@link TestSet} in a text file.
 * @author jer
 */
public class ResultFileRR extends StructuredTextCSV implements ResultFile {

   TestSet ts;
   Repository repository;
  public IntField topic = this.addInt("topic", "", "\\s", "", " ");
  public IntField id = this.addInt("id", "", "\\s", "", " ");
  public IntField segment = this.addInt("segment", "", "\\s", "", " ");
  public StringField collectionid = this.addString("docid", "", "\\s", "", " ");
  public DoubleField score = this.addDouble("score", "", "($|\\s)", "", "");

   public ResultFileRR(TestSet ts, String file) {
      super(ts.getResultsFile(file));
      this.ts = ts;
      repository = ts.repository;
   }

   public ResultFileRR(Repository repository, Datafile df) {
      super(df);
      this.repository = repository;
   }

   public ResultFileRR(Datafile df) {
      super(df);
   }

   /**
    * @return Collection of {@link Query}s that are specified in the {@link TestSet}, 
    * containing the results stored in the given file. 
    */
   public ArrayList<Query> getResults() {
      TreeMap<Integer, Query> results = new TreeMap<Integer, Query>();
      Query current = null;
      //Retriever retriever = new Retriever(repository);
      //RetrievalModel currentrm = null;
      this.openRead();
      //for (Query q : ts.getQueries(retriever)) {
      //   q.addFeature(ts.repository.getCollectionIDFeature());
      //   currentrm = RetrievalModel.create(retriever, q);
      //   results.put(q.id, currentrm);
      //}
      while (nextRecord()) {
         if (ts.topics.containsKey(topic.get())) {
                if (current == null ||topic.get() != current.getID()) {
                    current = results.get(topic.get());
                    if (current == null) {
                       current = new Query();
                       current.id = topic.get();
                       results.put(topic.get(), current);
                    }
                }
                
                Document doc = new Document(id.get(), segment.get());
                doc.setCollectionID(collectionid.get());
                doc.score = score.get();
                current.add(doc);
         }
      }
      this.closeRead();
      for (Query q : results.values()) {
         q.originalquery = ts.topics.get(q.id).query;
      }
      return new ArrayList<Query>(results.values());
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
            collectionid.set(d.getCollectionID());
            score.set(d.score);
            write();
         }
      }
      closeWrite();
   }
}
