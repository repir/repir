package io.github.repir.Strategy;

import java.util.ArrayList;
import java.util.Collection;
import io.github.repir.Strategy.Tools.StopWords;
import io.github.repir.Repository.Repository;
import io.github.repir.Retriever.Document;
import io.github.repir.tools.Lib.Log;
import io.github.repir.Repository.TermDF;
import io.github.repir.Repository.TermDocumentFeature;
import io.github.repir.Repository.TermInverted;
import io.github.repir.Repository.TermTF;
import io.github.repir.tools.Lib.PrintTools;

/**
 * A Term scores the occurrences of a single term in a document.
 * <p/>
 * @author jeroen
 */
public class Term extends GraphNode {

   public static Log log = new Log(Term.class);
   public TermInverted storefeature;
   public boolean isstopword;
   public int termid;
   public String originalterm;
   public String channel = "all";
   public String stemmedterm;

   public Term(GraphRoot root, String originalterm, String stemmedterm) {
      super(root);
      this.originalterm = originalterm;
      this.stemmedterm = stemmedterm;
      termid = repository.termToID(stemmedterm);
      if (termid >= 0) {
         isstopword = StopWords.get(repository).isStemmedStopWord(stemmedterm);
      }
   }

   public Term(Repository repository, String stemmedterm) {
      super(repository);
      this.stemmedterm = stemmedterm;
      termid = repository.termToID(stemmedterm);
   }

   public Term(GraphRoot root) {
      super(root.repository);
      this.root = root;
   }
   
   @Override
   public void readStatistics() {
      //log.info("readStatistics %s %d", this.stemmedterm, this.termid);
      if (willbescored) {
         this.featurevalues.corpusfrequency = ((TermTF) repository.getFeature(TermTF.class.getSimpleName())).readValue(termid);
         this.featurevalues.documentfrequency = ((TermDF) repository.getFeature(TermDF.class.getSimpleName())).readValue(termid);
         this.featurevalues.documentprior = (channel.equals("all")) ? featurevalues.queryweight : 0;
      }
   }

   @Override
   public void doAnnounce() {
      if (!exists()) {
         log.info("nonexist %s", this.stemmedterm);
         super.announce(ANNOUNCEKEY.NONEXIST, this);
      } else {
         if (isstopword) {
            super.announce(ANNOUNCEKEY.STOPWORD, this);
         }
         super.announce(ANNOUNCEKEY.TERM, this);
         super.announce(ANNOUNCEKEY.SCORABLE, this);
      }
   }

   @Override
   public void prepareRetrieval() {
      storefeature = (TermInverted) root.retrievalmodel.requestFeature("TermInverted:" + channel + ":" + stemmedterm);
      storefeature.setTerm(stemmedterm, termid, isstopword);
   }
   
   /**
    * By default terms are assumed independent of other terms, so an empty list
    * describes the dependencies.
    */
   @Override
   public void setTDFDependencies() {
      if (storefeature != null) // is null when term is not needed in a prepass
         storefeature.setNoDependencies();
   }
   
   @Override
   public ArrayList<TermDocumentFeature> getRequiredTDF() {
      if (storefeature == null)
         log.info("aaaargh %s %s", this.stemmedterm, ((GraphNode)parent).toTermString());
      return new ArrayList<TermDocumentFeature>() {{ add(storefeature); }};
   }
   
   /**
    * 
    * @return the sequence nr of the term in the query, as given by the features
    * sequence nr.
    */
   public int getTermSequence() {
      return storefeature.sequence;
   }

   @Override
   public void setchannel(String channel) {
      this.channel = channel;
   }

   public boolean exists() {
      return termid >= 0;
   }

   @Override
   public void process(Document doc) {
      if (storefeature != null) {
         featurevalues.pos = storefeature.getValue(doc);
         featurevalues.frequency = featurevalues.pos.length;
      }
   }

   @Override
   public String toString() {
      return io.github.repir.tools.Lib.PrintTools.sprintf("Term[%d,%s] #%f %.12e ", termid, stemmedterm, this.featurevalues.queryweight, this.featurevalues.corpusfrequency / (double) repository.getCorpusTF());
   }

   @Override
   public String toTermString() {
      return stemmedterm;
   }

   @Override
   public boolean equals(Object o) {
      if (o instanceof Term) {
         Term e = (Term) o;
         return originalterm.equals(e.originalterm);
      }
      return false;
   }

   @Override
   public GraphNode clone(GraphRoot newmodel) {
      if (termid >= 0) {
         Term e = new Term(newmodel, originalterm, stemmedterm);
         e.storefeature = storefeature;
         return e;
      } else {
         return null;
      }
   }

   @Override
   public String postReform() {
      return (featurevalues.queryweight == 1)?stemmedterm:PrintTools.sprintf("%s#%g", stemmedterm, featurevalues.queryweight);
   }

   @Override
   public String postReformUnweighted() {
      return stemmedterm;
   }

   @Override
   public void configureFeatures() {
   }
}
