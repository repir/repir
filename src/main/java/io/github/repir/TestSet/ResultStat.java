package io.github.repir.TestSet;

import io.github.repir.tools.Content.Datafile;
import io.github.repir.Retriever.Query;
import io.github.repir.tools.Lib.Log;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

public class ResultStat {
   public static Log log = new Log( ResultStat.class );
   public TestSet testset;
   public String system;
   public ArrayList<Query> queries;
   public QueryMetric metric;
   public double[] queryresult;
   public double avg;
   public int possiblequeries;
   public double sig;
   public double n2;

   public ResultStat(QueryMetric metric, TestSet ts, String file) {
      this(metric, ts, new ResultFile(ts, file).getResults());
      this.system = file;
   }

   public ResultStat(QueryMetric metric, TestSet ts, Collection<Query> queries) {
      this.testset = ts;
      this.metric = metric;
      setQueries(queries);
   }

   public ResultStat(QueryMetric metric, TestSet ts) {
      this(metric, ts, ts.getBaseline().getResults());
      this.system = "baseline";
   }

   public void setQueries(Collection<Query> q) {
      queries = new ArrayList<Query>(q);
      Collections.sort(queries);
      queryresult = new double[queries.size()];
   }

   public void calculateMeasure() {
      for (int q = 0; q < queries.size(); q++) {
         //log.info("%s %s %s", metric, testset, queries);
         if (queryresult[q] == 0) {
            queryresult[q] = metric.calculate(testset, queries.get(q));
            if (Double.isNaN(queryresult[q]))
               log.info("Warning NaN fro query %d", queries.get(q).id);
         }
      }
      possiblequeries = testset.possibleQueries();
      if (possiblequeries == 0)
         log.info("warning: 0 possible queries");
      avg = io.github.repir.tools.Lib.MathTools.Sum(queryresult) / possiblequeries;
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
