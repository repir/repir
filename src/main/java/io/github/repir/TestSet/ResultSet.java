package io.github.repir.TestSet;

import io.github.repir.Retriever.Document;
import io.github.repir.Retriever.Query;
import io.github.repir.TestSet.Metric.QueryMetric;
import io.github.repir.tools.Content.Datafile;
import io.github.repir.tools.Lib.Log;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

/**
 * Computes a {@link QueryMetric} for the results of the {@link Query}s of a
 * {@link TestSet}.
 *
 * @author jer
 */
public class ResultSet {

    public static Log log = new Log(ResultSet.class);
    public TestSet testset;
    public String system;
    public ArrayList<Query> queries;
    public QueryMetric querymetric;
    public double[] queryresult;
    private double mean = -1;
    public HashSet<Integer> validqueries;

    public ResultSet(QueryMetric metric, TestSet ts, String file) {
        this(metric, ts, new ResultFileRR(ts, file).getResults());
        this.system = file;
    }

    public ResultSet(QueryMetric metric, TestSet ts, Collection<Query> queries) {
        this.testset = ts;
        this.querymetric = metric;
        setQueries(queries);
    }

    public ResultSet(QueryMetric metric, TestSet ts, Query query) {
        this.testset = ts;
        this.querymetric = metric;
        setQuery(query);
    }

    public void setQueries(Collection<Query> queries) {
        this.queries = new ArrayList<Query>(queries);
        Collections.sort(this.queries);
        queryresult = new double[queries.size()];
        mean = -1;
        calculateMeasure();
    }

    public void setQuery(final Query q) {
        setQueries( new ArrayList<Query>() {{ add(q); }} );
    }

    private void calculateMeasure() {
        validqueries = testset.possibleQueries();
        for (int q = 0; q < queries.size(); q++) {
            if (queryresult[q] == 0) {
                int topicid = testset.getQRelId(queries.get(q));
                double result = querymetric.calculate(testset, queries.get(q));
                if (Double.isNaN(queryresult[q])) {
                    log.info("Warning NaN for query %d", queries.get(q).id);
                    validqueries.remove(testset.getQRelId(queries.get(q)));
                } else if (result < 0) {
                    validqueries.remove(testset.getQRelId(queries.get(q)));
                } else {
                    queryresult[q] = result;
                }
            }
        }
        if (validqueries.size() == 0) {
            log.info("warning: 0 possible queries");
        }
        //log.info("validqueries %d", validqueries.size());
    }

    public double getMean() {
        if (mean == -1 && validqueries.size() > 0) {
            mean = io.github.repir.tools.Lib.MathTools.sum(queryresult) / validqueries.size();
        }
        return mean;
    }

    public int getResultNumber(int queryid) {
        for (int i = 0; i < queries.size(); i++) {
            if (queries.get(i).id == queryid) {
                return i;
            }
        }
        return -1;
    }

}
