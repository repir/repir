package io.github.repir.Strategy;

import io.github.repir.Repository.DocForward;
import io.github.repir.Repository.DocTF;
import io.github.repir.Repository.Feature;
import io.github.repir.Repository.Stopwords.StopWords;
import io.github.repir.Repository.Term;
import io.github.repir.Repository.TermString;
import io.github.repir.Retriever.Document;
import io.github.repir.Retriever.Query;
import io.github.repir.Retriever.Retriever;
import io.github.repir.Strategy.Collector.CollectorDocument;
import io.github.repir.Strategy.Operator.Operator;
import io.github.repir.Strategy.Operator.QTerm;
import io.github.repir.Strategy.RetrievalModel;
import io.github.repir.Strategy.ScoreFunction.ScoreFunctionKLD;
import io.github.repir.tools.Lib.Log;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeSet;

/**
 * Implementation of Relevance Based Language Models (Lavrenko & Croft, 2001),
 * the variant which is commonly referred to as RM3.
 * @author jer
 */
public class RetrievalModelRM3 extends RetrievalModel {

   public static Log log = new Log(RetrievalModelRM3.class);
   public int fbmaxdocs;
   public double mu;
   public double alpha;
   public boolean fbstopwords;
   public int fbterms;

   public RetrievalModelRM3(Retriever retriever) {
      super(retriever);
      fbmaxdocs = repository.configuredInt("rm3.fbdocs", 10);
      alpha = repository.configuredDouble("rm3.alpha", 0.3);
      fbstopwords = repository.configuredBoolean("rm3.fbstopwords", false);
      fbterms = repository.configuredInt("rm3.fbterms", 100);
   }
   
   @Override
   public int getDocumentLimit() {
      return fbmaxdocs;  
   }
   
   @Override
   public String getScorefunctionClass() {
      return ScoreFunctionKLD.class.getSimpleName();
   }
   
   @Override
   public ArrayList<String> getReportedFeatures() {
      ArrayList<String> features = new ArrayList<String>();
      features.add(Feature.canonicalName(DocTF.class, "all")); // need to test
      features.add(Feature.canonicalName(DocForward.class, "all")); // need to test
      return features;
   }

   @Override
   public Query finishReduceTask() {
      TermString termstring = TermString.get(repository);
      termstring.openRead();

      Document documents[] = ((CollectorDocument) collectors.get(0)).getRetrievedDocs();
      HashMap<Integer, ExpansionTerm> termDocFrequencies = getTermDocFrequencies(documents);
      HashMap<Document, Double> documentposteriors = getDocumentPosteriors(documents, Math.E);
      TreeSet<ExpansionTerm> expansionterms = getExpansionTerms(documentposteriors, termDocFrequencies);

      HashMap<Term, ExpansionTerm> newTerms = new HashMap<Term, ExpansionTerm>();

      double sumexpansionweight = 0;
      for (ExpansionTerm term : expansionterms) {
         if (newTerms.size() > fbterms) {
            break;
         }
         if ((fbstopwords || !term.term.isStopword()) && !Character.isDigit(term.term.getProcessedTerm().charAt(0))) {
            newTerms.put(term.term, term);
            sumexpansionweight += term.weight;
         }
      }
      for (ExpansionTerm term : newTerms.values()) {
          term.weight *= (1 - alpha) / sumexpansionweight;
      }

      double sumoriginalqueryweight = 0;
      for (Operator node : root.containednodes) {
         if (node instanceof QTerm) {
            sumoriginalqueryweight += node.getQueryWeight();
         }
      }
      for (Operator node : root.containednodes) {
         if (node instanceof QTerm && node.getQueryWeight() > 0) {
            QTerm n = (QTerm) node;
            ExpansionTerm et = newTerms.get(n.term);
            if (et == null) {
               newTerms.put(n.term, et = new ExpansionTerm(n.term));
               et.term = n.term;
            }
            et.weight += alpha * n.getQueryWeight() / sumoriginalqueryweight;
         }
      }

      TreeSet<ExpansionTerm> sortedterms = new TreeSet<ExpansionTerm>(newTerms.values());

      StringBuilder sb = new StringBuilder();
      for (ExpansionTerm t : sortedterms) {
         sb.append(t.term.getProcessedTerm()).append("#").append(t.weight).append(" ");
      }
      query.query = sb.toString();
      query.setStrategyClass(RetrievalModel.class);
      query.removeStopwords = false;
      return query;
   }

   public TreeSet<ExpansionTerm> getExpansionTerms(HashMap<Document, Double> documentposteriors,
           HashMap<Integer, ExpansionTerm> termDocFrequencies) {
      TreeSet<ExpansionTerm> terms = new TreeSet<ExpansionTerm>();
      DocTF doctf = DocTF.get(repository, "all");
      for (int termid : termDocFrequencies.keySet()) {
         ExpansionTerm term = termDocFrequencies.get(termid);
         term.weight = 0;
         for (Document d : term.docfrequencies.keySet()) {
            int frequency = term.docfrequencies.get(d);
            double p_term_doc = frequency / (double) d.getInt(doctf);
            term.weight += p_term_doc * documentposteriors.get(d);
         }
         terms.add(term);
      }
      return terms;
   }

   public HashMap<Document, Double> getDocumentPosteriors(Document documents[], double base) {
      HashMap<Document, Double> documentposterior = new HashMap<Document, Double>();
      double sumscore = 0;
      for (Document d : documents) {
         sumscore += Math.pow(base, d.score);
      }
      for (Document d : documents) {
         documentposterior.put(d, Math.pow(base, d.score) / sumscore);
      }
      return documentposterior;
   }

   public HashMap<Integer, ExpansionTerm> getTermDocFrequencies(Document documents[]) {
      HashMap<Integer, ExpansionTerm> doctermfrequencies = new HashMap<Integer, ExpansionTerm>();
      DocForward forward = DocForward.get(repository, "all");
      for (Document d : documents) {
         for (int termid : d.getIntArray(forward)) {
            ExpansionTerm t = doctermfrequencies.get(termid);
            if (t == null) {
               doctermfrequencies.put(termid, t = new ExpansionTerm(repository.getTerm(termid)));
            }
            Integer docfrequency = t.docfrequencies.get(d);
            t.docfrequencies.put(d, (docfrequency == null) ? 1 : docfrequency + 1);
         }
      }
      return doctermfrequencies;
   }

   class ExpansionTerm implements Comparable<ExpansionTerm> {

      HashMap<Document, Integer> docfrequencies = new HashMap<Document, Integer>();
      Term term;
      double weight;

      public ExpansionTerm(Term term) {
         this.term = term;
      }

      public int hashCode() {
         return term.hashCode();
      }

      public boolean equals(Object o) {
         return (o instanceof ExpansionTerm && ((ExpansionTerm) o).term.equals(term));
      }

      @Override
      public int compareTo(ExpansionTerm o) {
         return (weight < o.weight) ? 1 : -1;
      }
   }
}
