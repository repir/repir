package io.github.repir.TestSet.Topic;

import java.util.ArrayList;

public class TestSetTopicSession extends TestSetTopic {
   public ArrayList<String> priorqueries = new ArrayList<String>();
   public ArrayList<String> clickeddocuments = new ArrayList<String>();
   public ArrayList<String> unclickeddocuments = new ArrayList<String>();
   public ArrayList<String> unseen = new ArrayList<String>();
   
     public TestSetTopicSession( int id, int qrelid, String domain, String query ) {
        super( id, qrelid, domain, query );
     }
}
