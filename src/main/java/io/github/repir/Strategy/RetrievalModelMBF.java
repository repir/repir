package io.github.repir.Strategy;

import io.github.repir.Repository.DocForward;
import io.github.repir.Repository.DocTF;
import io.github.repir.Repository.Feature;
import io.github.repir.Repository.ReportableFeature;
import io.github.repir.Repository.Repository;
import io.github.repir.Repository.Stopwords.StopWords;
import io.github.repir.Repository.Term;
import io.github.repir.Repository.TermCF;
import io.github.repir.Repository.TermString;
import io.github.repir.Retriever.Document;
import io.github.repir.Retriever.Query;
import io.github.repir.Retriever.ReportedFeature;
import io.github.repir.Retriever.Retriever;
import io.github.repir.Strategy.Collector.CollectorDocument;
import io.github.repir.Strategy.Operator.Operator;
import io.github.repir.Strategy.Operator.QTerm;
import io.github.repir.Strategy.RetrievalModel;
import io.github.repir.Strategy.ScoreFunction.ScoreFunctionKLD;
import io.github.repir.tools.lib.Log;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

/**
 * Implementation of Model Based Feedback proposed by Zhai and Lafferty (2001).
 * @author jer
 */
public class RetrievalModelMBF extends RetrievalModel {

   public static Log log = new Log(RetrievalModelMBF.class);
   public int fbmaxdocs;
   public double lambda;
   public double alpha;
   public boolean fbstopwords;
   public int fbterms;

   public RetrievalModelMBF(Retriever retriever) {
      super(retriever);
      lambda = repository.getConf().getFloat("mbf.lambda", 0.95f);
      alpha = repository.getConf().getFloat("mbf.alpha", 0.5f);
      fbmaxdocs = repository.getConf().getInt("mbf.fbdocs", 10);
      fbstopwords = repository.getConf().getBoolean("mbf.fbstopwords", false);
      fbterms = repository.getConf().getInt("mbf.fbterms", 1000);
   }
   
   @Override
   public String getQueryToRetrieve() {
      query.setScorefunctionClassname(ScoreFunctionKLD.class.getSimpleName());
      return super.getQueryToRetrieve();
   }
   
   @Override
   public ArrayList<String> getReportedFeatures() {
      ArrayList<String> features = new ArrayList<String>();
      features.add(Feature.canonicalName(DocForward.class, "all")); // need to test
      return features;
   }
   
   @Override
   public int getDocumentLimit() {
      return fbmaxdocs;
   }

   @Override
   public String getScorefunctionClass() {
      return ScoreFunctionKLD.class.getSimpleName();
   }
   
   /**
    * The model is re-estimated by taking the top-k documents returned in the first
    * retrieval pass, using an Expectation Maximization algorithm to estimate whether 
    * words are more likely to originate from the pseudo-relevance documents or the
    * general collection model.
    * @return 
    */
   @Override
   public Query finishReduceTask() {
      // fill fbterm with the words in the top-fbmaxdocs documents
      FBModel fbterm = new FBModel(this, retriever, fbmaxdocs, fbstopwords);
      FBModel fbmax = null;

      // EM to estimate p based on feedback
      for (int i = 0; i < 20; i++) {
         fbterm.EM(lambda);
         if (fbmax == null || fbmax.score < fbterm.score) {
            fbmax = fbterm.clone();
         }
      }
      for (Operator f : root.containednodes) {
         if (f instanceof QTerm) {
            QTerm ft = (QTerm) f;
            if (ft.exists()) {
               T t = fbterm.get(ft.term.getID());
               log.info("existing term %s mle %f", t.term, t.p);
            }
         }
      }

      double sumoldtermweight = root.containednodes.size();

      // addQueue fbmaxdocs terms to root
      TreeSet<T> sorted = new TreeSet<T>(fbmax.values());
      int expandterms = 0;
      TreeSet<T> newterms = new TreeSet<T>();
      for (T term : sorted) {
         // addQueue to querymodel: existing terms and terms above the cutoff point
         QTerm e = null;
         QTerm n = root.getTerm(term.term.getProcessedTerm());
         Operator f = root.find(n);
         if (f != null) {
            newterms.add(new T(term.term, (1 - alpha) * f.getQueryWeight() / sumoldtermweight + alpha * term.p));
         } else if (term.p >= 0.001 && expandterms++ < fbterms) {
            newterms.add(new T(term.term, alpha * term.p));
         }
      }
      double sumweight = 0;
      for (T t : newterms) {
         sumweight += t.p;
      }
      for (T t : newterms) {
         t.p /= sumweight;
      }
      StringBuilder sb = new StringBuilder();
      for (T t : newterms) {
         sb.append(t.term).append("#").append(t.p).append(" ");
      }
      query.query = sb.toString();
      query.setStrategyClassname("RetrievalModel");
      query.removeStopwords = false;
      query.clearResults();
      return query;
   }

   static class FBModel extends HashMap<Term, T> {

      double score;

      private FBModel() {
      }

      public FBModel(RetrievalModel rm, Retriever retriever, int fb, boolean fbstopwords) {
         Repository repository = retriever.repository;
         ReportedFeature forward = rm.getReportedFeature(DocForward.class, "all");
         TermCF termcf = TermCF.get(repository);
         termcf.loadMem();
         for (Operator f : rm.root.containednodes) {
            if (f instanceof QTerm) {
               QTerm term = (QTerm) f;
               if (term.exists()) {
                  T t = new T(term.term, term.getCF() / (double)repository.getCF());
                  put(term.term, t);
               }
            }
         }
         int doccount = 0;
         int showterm = 0;
         TermString termstring = TermString.get(repository);
         termstring.openRead();
         for (Document d : ((CollectorDocument) rm.collectors.get(0)).getRetrievedDocs()) {
            if (doccount++ >= fb) {
               break;
            }
            int tokens[] = d.getIntArray(forward);
            for (int termid : tokens) {
               T t = get(termid);
               if (t == null) {
                  long cf = termcf.readValue(termid);
                  Term term = repository.getTerm(termid);
                  if (fbstopwords || !term.isStopword()) {
                     t = new T(term, cf / (double) retriever.getRepository().getCF());
                     t.cf = 1;
                     put(term, t);
                  }
               } else {
                  t.cf++;
               }
            }
         }
      }

      @Override
      public FBModel clone() {
         FBModel fb = new FBModel();
         for (Map.Entry<Term, T> entry : entrySet()) {
            T t = new T(entry.getValue().term, entry.getValue().termcorpusmle);
            t.p = entry.getValue().p;
            t.weight = entry.getValue().weight;
            fb.put(t.term, t);
         }
         fb.score = score;
         return fb;
      }

      public void EM(double lambda) {
         double sum = 0;
         for (T term : values()) {
            term.p = term.cf;//Lib.Random.getDouble();      
            sum += term.p;
         }
         for (T term : values()) {
            term.p /= sum;
         }
         double diff = 1;
         while (diff > 0.001) {
            diff = 0;
            // E-step
            for (T term : values()) {
               double newweight = (1 - lambda) * term.p / ((1 - lambda) * term.p + lambda * term.termcorpusmle);
               diff += Math.abs(term.weight - newweight);
               term.weight = newweight;
            }
            // M-step
            double sump = 0;
            for (T term : values()) {
               term.p = term.cf * term.weight;
               sump += term.p;
            }
            for (T term : values()) {
               term.p /= sump;
            }
         }
      }
   }

   static class T implements Comparable<T> {

      Term term;
      double termcorpusmle;
      int cf;
      double weight;
      double p;
      double querymle;

      public T(Term term, double corpusmle) {
         this.term = term;
         termcorpusmle = p = corpusmle;
      }

      @Override
      public int compareTo(T o) {
         return (p < o.p) ? 1 : -1;
      }
   }

   static class NEWTERM {

      Term term;
      double p;

      public NEWTERM(Term term, double p) {
         this.term = term;
         this.p = p;
         term.hashCode();
      }

      public int hashCode() {
         return term.getID();
      }

      public boolean equals(Object o) {
         return (o instanceof NEWTERM && ((NEWTERM) o).term.getID() == term.getID());
      }
   }
}
