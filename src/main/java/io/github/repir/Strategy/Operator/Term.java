package io.github.repir.Strategy.Operator;

import java.util.ArrayList;
import io.github.repir.Strategy.Tools.StopWords;
import io.github.repir.Repository.Repository;
import io.github.repir.Retriever.Document;
import io.github.repir.tools.Lib.Log;
import io.github.repir.Repository.TermDF;
import io.github.repir.Repository.TermDocumentFeature;
import io.github.repir.Repository.TermInverted;
import io.github.repir.Repository.TermCF;
import io.github.repir.Strategy.GraphRoot;
import io.github.repir.tools.Lib.PrintTools;

/**
 * A Term represents the positions in a document at which this term appears.
 * this {@link Operator} is seeded with the (positional) values stored in
 * {@link TermInverted}. A Term may be {@link Scorable} by a
 * {@link ScoreFunction} or have its values used by a parent {@link Operator}
 * such as {@link ProximityOperator} or {SynonymOperator}.
 * <p/>
 * A Term determines if it is in the list of {@link StopWords}, but rather than
 * act it will simply announce itself as a stop word, leaving it up to the
 * parent operators and GraphRoot (in that order) to decide the appropriate
 * cause of action.
 * <p/>
 * @author jeroen
 */
public class Term extends Operator {

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
      if (willbescored) {
         this.featurevalues.cf = ((TermCF) repository.getFeature(TermCF.class)).readValue(termid);
         this.featurevalues.df = ((TermDF) repository.getFeature(TermDF.class)).readValue(termid);
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
      storefeature = (TermInverted) root.retrievalmodel.requestFeature(TermInverted.class,
              channel, stemmedterm);
      storefeature.setTerm(stemmedterm, termid, isstopword);
   }

   /**
    * By default terms are assumed independent of other terms, so an empty list
    * describes the dependencies.
    */
   @Override
   public void setTDFDependencies() {
      if (storefeature != null) // is null when term is not needed in a prepass
      {
         storefeature.setNoDependencies();
      }
   }

   @Override
   public ArrayList<TermDocumentFeature> getRequiredTDF() {
      return new ArrayList<TermDocumentFeature>() {
         {
            add(storefeature);
         }
      };
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
      return io.github.repir.tools.Lib.PrintTools.sprintf("Term[%d,%s] #%f %.12e ", termid, stemmedterm, this.featurevalues.queryweight, this.featurevalues.cf / (double) repository.getCF());
   }

   @Override
   public String toTermString() {
      return stemmedterm;
   }

   @Override
   public boolean equals(Object o) {
      if (o instanceof Term) {
         Term e = (Term) o;
         return originalterm.equals(e.originalterm) && channel.equals(e.channel);
      }
      return false;
   }

   @Override
   public Operator clone(GraphRoot newmodel) {
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
      return (featurevalues.queryweight == 1) ? stemmedterm : PrintTools.sprintf("%s#%g", stemmedterm, featurevalues.queryweight);
   }

   @Override
   public String postReformUnweighted() {
      return stemmedterm;
   }

   @Override
   public void configureFeatures() {
   }
}
