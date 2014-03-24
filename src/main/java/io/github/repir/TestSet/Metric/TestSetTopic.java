package io.github.repir.TestSet.Metric;

public class TestSetTopic {
     public int id;
     public String domain;
     public String query;
     
     public TestSetTopic( int id, String domain, String query ) {
        this.id = id;
        this.domain = domain;
        this.query = query;
     }
     
     @Override
     public boolean equals(Object o) {
        return ((o instanceof TestSetTopic) && ((TestSetTopic)o).id == id);
     }

   @Override
   public int hashCode() {
      int hash = 3;
      hash = 79 * hash + this.id;
      return hash;
   }
}
