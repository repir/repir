package io.github.repir.TestSet;

import io.github.repir.Retriever.Query;
import io.github.repir.tools.Lib.Log;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.apache.commons.math3.stat.inference.TestUtils;

public class ResultSet {
   public static Log log = new Log( ResultSet.class );
   public QueryMetric metric;
   public ResultStat[] result;
   public TestSet testset;

   public ResultSet(QueryMetric metric, TestSet testset, String[] systems) {
      this.testset = testset;
      this.metric = metric;
      setSystems( systems );
   }
   
   public void setSystems( String systems[] ) { 
      result = new ResultStat[ systems.length ];
      for (int sys = 0; sys < systems.length; sys++) {
         log.info("system %s", systems[sys]);
         result[sys] = new ResultStat(metric, testset, systems[sys]);
      }
   }

   public ResultSet(QueryMetric metric, TestSet testset, Collection<Query> queries) {
      this.metric = metric;
      result = new ResultStat[2];
      this.testset = testset;
      result[0] = new ResultStat(metric, testset);
      result[1] = new ResultStat(metric, testset, queries);
   }

   public ResultSet(QueryMetric metric, TestSet testset, final Query query) {
      this(metric, testset, new ArrayList<Query>() {
         {
            add(query);
         }
      });
   }
   
   public int getResultNumber(int queryid) {
      for (int i = 0; i < result[0].queries.size(); i++) {
         if (result[0].queries.get(i).id == queryid) {
            return i;
         }
      }
      return -1;
   }

   public void calulateMeasure() {
      for (ResultStat r : result) {
         r.calculateMeasure();
      }
   }

   public void calculateSig() {
      for (int i = 1; i < result.length; i++) {
         result[i].sig = TestUtils.pairedTTest(result[0].queryresult, result[i].queryresult) / 2;
      }
   }
   
   public void calculateN2() {
      for (int i = 1; i < result.length; i++) {
         double n2 = 0;
         for (int j = 0; j < result[0].queryresult.length; j++) {
            if (result[0].queryresult[j] > result[i].queryresult[j]) 
               n2 += Math.pow(result[i].queryresult[j] - result[0].queryresult[j], 2);
         }
         result[i].n2 = n2;
      }
   }
   
   public double calculateRI( int imp ) {
         int pos = 0;
         int neg = 0;
         for (int j = 0; j < result[0].queryresult.length; j++) {
            if (result[0].queryresult[j] > result[imp].queryresult[j]) 
               neg ++;
            if (result[0].queryresult[j] < result[imp].queryresult[j]) 
               pos ++;
         }
         return (pos - neg) / (double)result[0].queryresult.length;
   }
   
   public double sigOver( int base, int imp ) {
      return TestUtils.pairedTTest(result[base].queryresult, result[imp].queryresult )/2;
   }
}
