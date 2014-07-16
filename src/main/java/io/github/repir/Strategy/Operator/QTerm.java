package io.github.repir.Strategy.Operator;

import io.github.repir.Repository.Repository;
import io.github.repir.Repository.Stopwords.StopWords;
import io.github.repir.Repository.Term;
import io.github.repir.Repository.TermCF;
import io.github.repir.Repository.TermDF;
import io.github.repir.Repository.TermDocumentFeature;
import io.github.repir.Repository.TermInverted;
import io.github.repir.Retriever.Document;
import io.github.repir.Strategy.GraphRoot;
import io.github.repir.tools.Lib.Log;
import io.github.repir.tools.Lib.PrintTools;
import java.util.ArrayList;

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
public class QTerm extends Operator {

   public static Log log = new Log(QTerm.class);
   public TermDocumentFeature storefeature;
   public io.github.repir.Repository.Term term;
   public String channel = "all";

   public QTerm(GraphRoot root, Term term) {
      super(root);
      this.term = term;
   }

   public QTerm(GraphRoot root, ArrayList<Operator> term) {
      super(root);
      this.term = ((QTerm)term.get(0)).term;
   }

   public QTerm(GraphRoot root) {
      super(root);
   }

   @Override
   public void readStatistics() {
      if (willbescored) {
         this.cf = ((TermCF) repository.getFeature(TermCF.class)).readValue(term.getID());
         this.df = ((TermDF) repository.getFeature(TermDF.class)).readValue(term.getID());
         this.documentprior = (channel.equals("all")) ? queryweight : 0;
      }
   }

   @Override
   public void doAnnounce() {
      if (!exists()) {
         log.info("nonexist %s", term.getProcessedTerm());
         super.announce(ANNOUNCEKEY.NONEXIST, this);
      } else {
         if (term.isStopword()) {
            super.announce(ANNOUNCEKEY.STOPWORD, this);
         }
         super.announce(ANNOUNCEKEY.TERM, this);
         super.announce(ANNOUNCEKEY.SCORABLE, this);
      }
   }

   @Override
   public void prepareRetrieval() {
      storefeature = (TermInverted) root.retrievalmodel.requestFeature(TermInverted.class,
              channel, term.getProcessedTerm());
      storefeature.setTerm(term);
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
      } else {
         //log.info("setTDFDependencies() storefeature is null, must be prepass?");
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
      return term.exists();
   }

   @Override
   public void process(Document doc) {
      if (storefeature != null) {
         pos = (int[])storefeature.getValue(doc);
         frequency = pos.length;
      }
   }

   @Override
   public String toString() {
      return PrintTools.sprintf("Term[%d,%s] #%f %.12e ", term.getID(), term.getProcessedTerm(), queryweight, this.cf / (double) repository.getCF());
   }

   @Override
   public String toTermString() {
      return term.toString();
   }

   public boolean isStopword() {
      return term.isStopword();
   }

   public int getTermID() {
      return term.getID();
   }

   public String getProcessedTerm() {
      return term.getProcessedTerm();
   }

   @Override
   public boolean equals(Object o) {
      if (o instanceof QTerm) {
         QTerm e = (QTerm) o;
         return term.getID() == e.term.getID() && channel.equals(e.channel);
      }
      return false;
   }

   @Override
   public Operator clone(GraphRoot newmodel) {
      if (term.exists()) {
         QTerm e = new QTerm(newmodel, term);
         e.storefeature = storefeature;
         return e;
      } else {
         return null;
      }
   }

   @Override
   public String postReform() {
      return (queryweight == 1) ? term.toString() : 
              PrintTools.sprintf("%s#%g", term.toString(), queryweight);
   }

   @Override
   public String postReformUnweighted() {
      return term.toString();
   }

   @Override
   public void configureFeatures() {
   }
}
