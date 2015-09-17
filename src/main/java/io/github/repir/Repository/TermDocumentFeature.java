package io.github.repir.Repository;

import io.github.repir.Retriever.Document;
import io.github.repir.Repository.Stopwords.StopWords;
import io.github.htools.io.EOCException;
import io.github.htools.io.struct.StructuredFileIntID;
import io.github.htools.lib.ArrayTools;
import io.github.htools.lib.Log;

/**
 * A stored feature that uses a term-document structure similar to a textbook inverted index. This 
 * data structure is best used for sparse data that is to be accessed by termID, which gives an ordered
 * list of the documents in which the term appears. The base class can be extended to define the
 * exact data that needs to be stored, such as the term frequency or the list of positions of the
 * term in the document. 
 * @author jeroen
 * @param <F> StructuredFileIntID that contains the data per termID 
 * @param <C> datatype returned when values are read with {@link #getValue(io.github.repir.Retriever.Document)}
 */
public abstract class TermDocumentFeature<F extends StructuredFileIntID, C> 
       extends StoredReportableFeature<F, C> 
       implements Comparable<TermDocumentFeature>, ResidentFeature {

   public static Log log = new Log(TermDocumentFeature.class); 
   Term term;
   public int docid = -1;
   public int sequence; // used to assign an identifier during retrieval to set to the n-th query term
   private TermDocumentFeature dependencies[][];
   private final TermDocumentFeature NODEPENDENCIES[][] = new  TermDocumentFeature[0][];

   public TermDocumentFeature(Repository repository, String field) {
      super(repository, field);
   }

   public TermDocumentFeature(Repository repository, String field, Term term) {
      super(repository, field);
      this.term = term;
   }
   
   @Override
   public void openRead() {
      super.openRead();
      if (term.exists()) {
         find(term.getID());
         docid = -1;
      }
   }
   
   @Override
   public String getCanonicalName() {
      if (term == null)
         return canonicalName( getClass(), getField() );
      else
         return canonicalName( getClass(), getField(), term.getProcessedTerm() );
   }
   
   /**
    * Sets the TDF's dependencies. This is used to make retrieval more efficient by
    * skipping documents that do not contain any scorable term combinations, e.g.
    * for the query "albert-einstein" documents with only albert or only einstein can
    * be omitted. Independent terms should call this with an array length zero
    * overriding all other dependencies as documents containing this term are always
    * scorable.
    * @param dep 
    */
   public void setDependencies( TermDocumentFeature[] dep ) {
      if (dep.length == 0)
         dependencies = NODEPENDENCIES;
      else if (dependencies == null || dependencies.length > 0) {
         if (dependencies == null) {
            dependencies = new TermDocumentFeature[1][];
            dependencies[0] = dep;
         } else {
            ArrayTools.addObjectToArr(dependencies, dep);
         }
      }
   }
   
   public void setNoDependencies( ) {
      dependencies = NODEPENDENCIES;
   }
   
   public void resetDependencies() {
      dependencies = null;
   }
   
   public boolean meetsDependencies() {
      if (dependencies == null)
         return false;
      if (dependencies == NODEPENDENCIES)
         return true;
      //log.info("meetsDependencies %s", this.term);
      NEXT:
      for (TermDocumentFeature dep[] : dependencies) {
         //log.info("dep %d", dep.length);
         for (TermDocumentFeature f : dep ) {
            //log.info("%s", f);
            if (f.docid != docid)
               continue NEXT;
         }
         return true;
      }
      return false;
   }
   
   @Override
   public void readResident() {
      try {
         getFile().readResident(term.getID());
      } catch (EOCException ex) {
         log.exception(ex, "Find id %d", term.getID());
      }
   }
   
   public boolean isReadResident() {
      return getFile().isReadResident();
   }
   
   @Override
   public void reuse() {
      super.reuse();
      docid = -1;
   }
   
   public void find() {
      find(term.getID());
   }

   @Override
   public boolean hasNext() {
      return docid >= 0;
   }

   abstract protected int readNextID();

   public abstract C getValue(Document doc);

   @Override
   public long getLength() {
      long size = 0;
      try {
         getFile().find(term.getID());
         size = file.getCeiling() - file.getOffset();
      } catch (EOCException ex) {
         log.exception(ex, "getBytesSize id=%d", term.getID());
      }
      return size;
   }
   
   @Override
   public boolean next() {
      docid = readNextID();
      return hasNext();
   }

   public int getCurrentDocID() {
      return docid;
   }

   public int getTermID() {
      return term.getID();
   }

   @Override
   public int compareTo(TermDocumentFeature o) {
      return (docid < o.docid) ? -1 : 1;
   }
}
