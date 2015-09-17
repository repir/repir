package io.github.repir.Repository;

import io.github.htools.hadoop.io.archivereader.RecordKey;
import io.github.htools.hadoop.io.archivereader.RecordValue;
import io.github.repir.Retriever.Document;

/**
 * A feature must implement this, if this feature can be reported in the resultset
 * of a retrieval task. The features data is stored in a Document, but all handling 
 * of the data is performed through the Feature. 
 * @author jer
 * @param <C> 
 */
public interface ReportableFeature<C> {
   
   /**
    * decode an encoded array of bytes to restore the features data of a Document
    * that has been received. This method is called automatically by the MapReduce implementations
    * of RR retrieval jobs, just after receiving the Document across the cluster.
    * @param d 
    */
   public abstract void decode(Document d, int reportid);

   /**
    * encode the data as an array of bytes, to allow the Document to send itself
    * across nodes. This method is called automatically by the MapReduce implementations
    * of RR retrieval jobs, just before sending the Document across the cluster.
    * @param d 
    */
   public abstract void encode(Document d, int reportid);

   /*
    * Asks the feature to store it's value in the given Document object.
    */
   public abstract void report(Document doc, int reportid);
   
   /*
    * Return the Feature's value for this Document.
    */
   public abstract C valueReported(Document doc, int reportid);
   

   public abstract String getCanonicalName();
}
