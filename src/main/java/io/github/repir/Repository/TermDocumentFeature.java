package io.github.repir.Repository;

import java.io.EOFException;
import io.github.repir.Retriever.Document;
import io.github.repir.Strategy.Tools.StopWords;
import io.github.repir.tools.Content.RecordIdentity;
import io.github.repir.tools.Lib.ArrayTools;
import io.github.repir.tools.Lib.Log;

/**
 * A stored feature that uses a term-document structure similar to a textbook inverted index. This 
 * data structure is best used for sparse data that is to be accessed by term, which gives an ordered
 * list of the documents in which the term appears. The base class can be extended to define the
 * exact data that needs to be stored, such as the term frequency or the list of positions of the
 * term in the document. 
 * @author jeroen
 * @param <F>
 * @param <C> 
 */
public abstract class TermDocumentFeature<F extends RecordIdentity, C> extends StoredReportableFeature<F, C> implements Comparable<TermDocumentFeature> {

   public static Log log = new Log(TermDocumentFeature.class); 
   public int termid;
   String term;
   public boolean isstopword;
   public int docid = -1;
   public int maxbuffersize;
   public int sequence; // used to assign an identifier during retrieval to set to the n-th query term
   private TermDocumentFeature dependencies[][];

   public TermDocumentFeature(Repository repository, String field) {
      super(repository, field);
   }

   public void setTerm(String term) {
      setTerm(term, repository.termToID(term), StopWords.get(repository).isStemmedStopWord(term));
   }

   public void setTerm(String term, int termid, boolean isstopword) {
      this.term = term;
      this.termid = termid;
      this.isstopword = isstopword;
   }

   @Override
   public void openRead() {
      super.openRead();
      if (termid >= 0) {
         find(termid);
         docid = -1;
      }
   }
   
   /**
    * Sets the TDF's dependencies. This is used to make retrieval more efficient by
    * skipping documents that do not contain any scorable term combinations, e.g.
    * fr the query "albert-einstein" documents with only albert or only einstein can
    * be omitted. Independent terms should call this with an array length zero
    * overriding all other dependencies as documents containing this term are always
    * scorable.
    * @param dep 
    */
   public void setDependencies( TermDocumentFeature[] dep ) {
      if (dep.length == 0)
         dependencies = new TermDocumentFeature[0][];
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
      dependencies = new TermDocumentFeature[0][];
   }
   
   public void resetDependencies() {
      dependencies = null;
   }
   
   public boolean meetsDependencies() {
      if (dependencies == null)
         return false;
      if (dependencies.length == 0)
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
         getFile().readResident(termid);
      } catch (EOFException ex) {
         log.exception(ex, "Find id %d", termid);
      }
   }
   
   public boolean isReadResident() {
      return getFile().isReadResident();
   }
   
   public void find() {
      find(termid);
   }

   @Override
   public boolean hasNext() {
      return docid >= 0;
   }

   abstract protected int readNextID();

   public abstract C getValue(Document doc);

   @Override
   public long getBytesSize() {
      long size = 0;
      try {
         getFile().find(termid);
         size = file.getCeiling() - file.getOffset();
      } catch (EOFException ex) {
         log.exception(ex, "getBytesSize id=%d", termid);
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
      return termid;
   }

   @Override
   public int compareTo(TermDocumentFeature o) {
      return (docid < o.docid) ? -1 : 1;
   }
   
   @Override
   public void reuse() {
      log.info("reuse()");
      super.reuse();
      docid = -1;
   }
}
