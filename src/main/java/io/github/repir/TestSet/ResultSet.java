package io.github.repir.TestSet;

import io.github.repir.TestSet.Metric.QueryMetric;
import io.github.repir.tools.Content.Datafile;
import io.github.repir.Retriever.Query;
import io.github.repir.tools.Lib.Log;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

/**
 * Computes a {@link QueryMetric} for the results of the {@link Query}s of a 
 * {@link TestSet}.
 * @author jer
 */
public class ResultSet {
   public static Log log = new Log( ResultSet.class );
   public TestSet testset;
   public String system;
   public ArrayList<Query> queries;
   public QueryMetric querymetric;
   public double[] queryresult;
   private double mean = -1;
   public int possiblequeries;

   public ResultSet(QueryMetric metric, TestSet ts, String file) {
      this(metric, ts, new ResultFile(ts, file).getResults());
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

   public ResultSet(QueryMetric metric, TestSet ts) {
      this(metric, ts, ts.getBaseline().getResults());
      this.system = "baseline";
      calculateMeasure();
   }

   public void setQueries(Collection<Query> q) {
      queries = new ArrayList<Query>(q);
      Collections.sort(queries);
      queryresult = new double[queries.size()];
      calculateMeasure();
   }

   public void setQuery(Query q) {
      queries = new ArrayList<Query>();
      queries.add(q);
      queryresult = new double[1];
      calculateMeasure();
   }

   private void calculateMeasure() {
      for (int q = 0; q < queries.size(); q++) {
         if (queryresult[q] == 0) {
            queryresult[q] = querymetric.calculate(testset, queries.get(q));
            if (Double.isNaN(queryresult[q]))
               log.info("Warning NaN for query %d", queries.get(q).id);
         }
      }
      possiblequeries = testset.possibleQueries();
      if (possiblequeries == 0)
         log.info("warning: 0 possible queries");
   }
   
   public double getMean() {
      if (mean == -1 && possiblequeries > 0) {
         mean = io.github.repir.tools.Lib.MathTools.Sum(queryresult) / possiblequeries;
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
