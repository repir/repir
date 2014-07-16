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
import java.util.TreeMap;

/**
 * File Structure to store the results of a {@link TestSet} in a text file.
 *
 * @author jer
 */
public class ResultFileTREC extends StructuredTextCSV implements ResultFile {

    TestSet ts;
    public String runname = "identifier";
    public IntField topic = addInt("topic", "", "\\s", "", " ");
    public StringField Q0 = addString("Q0", "", "\\s", "", " ");
    public StringField collectionid = addString("collectionid", "", "\\s", "", " ");
    public IntField rank = addInt("rank", "", "\\s", "", " ");
    public DoubleField score = addDouble("score", "", "\\s", "", " ");
    public StringField identifier = addString("identifier", "", "($|\\s)", "", "");

    public ResultFileTREC(TestSet ts, Datafile df) {
        super(df);
        this.ts = ts;
    }

    public ResultFileTREC(Datafile df) {
        super(df);
    }

    /**
     * @return Collection of {@link Query}s that are specified in the
     * {@link TestSet}, containing the results stored in the given file.
     */
    public ArrayList<Query> getResults() {
        TreeMap<Integer, Query> results = new TreeMap<Integer, Query>();
        Query current = null;
        this.openRead();

        while (next()) {
            if (ts.topics.containsKey(topic.get())) {
                if (current == null || topic.get() != current.getID()) {
                    current = results.get(topic.get());
                    if (current == null) {
                        current = new Query();
                        current.id = topic.get();
                        results.put(topic.get(), current);
                    }
                }
                Document doc = new Document(collectionid.get());
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
     *
     * @param results
     */
    public void writeresults(ArrayList<Query> results) {
        openWrite();
        for (Query q : results) {
            int row = 0;
            for (Document d : q.getQueryResults()) {
                topic.set(q.getID());
                Q0.set("Q0");
                collectionid.set(d.getCollectionID());
                rank.set(++row);
                score.set(d.score);
                identifier.set(runname);
                write();
            }
        }
        closeWrite();
    }
}
