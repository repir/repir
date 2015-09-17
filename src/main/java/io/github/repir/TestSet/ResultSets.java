package io.github.repir.TestSet;

import io.github.repir.TestSet.Metric.QueryMetric;
import io.github.repir.Retriever.Query;
import io.github.htools.lib.Log;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import org.apache.commons.math3.stat.inference.TestUtils;

/**
 * ResultSets is a List of two or more {@link ResultSet}s of the same
 * {@link TestSet}, and additionally provides comparisons between sets, such as
 * {@link #sigOver(int, int)} to test for significant improvement.
 * @author jer
 */
public class ResultSets extends ArrayList<ResultSet> {
   public static Log log = new Log( ResultSets.class );
   public QueryMetric metric;
   public TestSet testset;

   public ResultSets(QueryMetric metric, TestSet testset, String[] systems) throws IOException {
      this.testset = testset;
      this.metric = metric;
      setSystems( systems );
   }
   
   public void setSystems( String systems[] ) throws IOException { 
      for (int sys = 0; sys < systems.length; sys++) {
         log.info("system %s", systems[sys]);
         add( new ResultSet(metric, testset, systems[sys]) );
      }
   }
   
   /**
    * @param base int of the baseline to compare over
    * @param imp int of the alternative results that are tested for significant improvement
    * @return Robustness Index of the alternative system vs baseline, which is 
    * defined as (#improved queries - #hurt queries)/all queries
    */
   public double riOver( int base, int imp ) throws IOException {
         int pos = 0;
         int neg = 0;
         ResultSet b = get(base);
         ResultSet i = get(imp);
         for (int j = get(0).queryresult.length-1; j >= 0; j--) {
            if (b.queryresult[j] > i.queryresult[j]) 
               neg ++;
            if (b.queryresult[j] < i.queryresult[j]) 
               pos ++;
         }
         return (pos - neg) / (double)testset.possibleQueries().size();
   }
   
   /**
    * @param base int of the baseline to compare over
    * @param imp int of the alternative results that are tested for significant improvement
    * @return p-value of paired Student's T-test, 1-tailed.
    */
   public double sigOver( int base, int imp ) {
       try {
           return TestUtils.pairedTTest(get(base).queryresult, get(imp).queryresult )/2;
       } catch (Exception ex) {
           log.fatalexception(ex, "sigOver( %d, %d)", base, imp);
       }
       return -2;
   }
}
