package io.github.repir.Strategy;

import io.github.repir.Repository.DocForward;
import io.github.repir.Repository.DocTF;
import io.github.repir.Repository.Feature;
import io.github.repir.Strategy.Operator.Term;
import io.github.repir.Strategy.Operator.Operator;
import io.github.repir.Strategy.Tools.StopWords;
import io.github.repir.Strategy.ScoreFunction.ScoreFunctionKLD;
import io.github.repir.Strategy.Collector.CollectorDocument;
import io.github.repir.Retriever.Document;
import io.github.repir.Retriever.Retriever;
import io.github.repir.Retriever.Query;
import io.github.repir.tools.Lib.Log;
import io.github.repir.Repository.TermString;
import io.github.repir.Strategy.RetrievalModel;
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
      fbmaxdocs = repository.getConfigurationInt("rm3.fbdocs", 10);
      alpha = repository.getConfigurationDouble("rm3.alpha", 0.3);
      fbstopwords = repository.getConfigurationBoolean("rm3.fbstopwords", false);
      fbterms = repository.getConfigurationInt("rm3.fbterms", 100);
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
      TermString termstring = (TermString) repository.getFeature(TermString.class);
      termstring.openRead();

      Document documents[] = ((CollectorDocument) collectors.get(0)).getRetrievedDocs();
      HashMap<Integer, ExpansionTerm> termDocFrequencies = getTermDocFrequencies(documents);
      HashMap<Document, Double> documentposteriors = getDocumentPosteriors(documents, Math.E);
      TreeSet<ExpansionTerm> expansionterms = getExpansionTerms(documentposteriors, termDocFrequencies);

      HashMap<Integer, ExpansionTerm> newTerms = new HashMap<Integer, ExpansionTerm>();

      double sumexpansionweight = 0;
      for (ExpansionTerm term : expansionterms) {
         if (newTerms.size() > fbterms) {
            break;
         }
         term.stemmedterm = termstring.readValue(term.termid);
         if ((fbstopwords || !StopWords.get(repository).isStemmedStopWord(term.stemmedterm)) && !Character.isDigit(term.stemmedterm.charAt(0))) {
            newTerms.put(term.termid, term);
            sumexpansionweight += term.weight;
         }
      }
      for (ExpansionTerm term : newTerms.values()) {
          term.weight *= (1 - alpha) / sumexpansionweight;
      }

      double sumoriginalqueryweight = 0;
      for (Operator node : root.containednodes) {
         if (node instanceof Term) {
            sumoriginalqueryweight += node.featurevalues.queryweight;
         }
      }
      for (Operator node : root.containednodes) {
         if (node instanceof Term && node.featurevalues.queryweight > 0) {
            Term n = (Term) node;
            ExpansionTerm et = newTerms.get(n.termid);
            if (et == null) {
               newTerms.put(n.termid, et = new ExpansionTerm(n.termid));
               et.stemmedterm = n.stemmedterm;
            }
            et.weight += alpha * n.featurevalues.queryweight / sumoriginalqueryweight;
         }
      }

      TreeSet<ExpansionTerm> sortedterms = new TreeSet<ExpansionTerm>(newTerms.values());

      StringBuilder sb = new StringBuilder();
      for (ExpansionTerm t : sortedterms) {
         String term = t.stemmedterm;
         sb.append(t.stemmedterm).append("#").append(t.weight).append(" ");
      }
      query.stemmedquery = sb.toString();
      query.setStrategyClass(RetrievalModel.class);
      query.removeStopwords = false;
      return query;
   }

   public TreeSet<ExpansionTerm> getExpansionTerms(HashMap<Document, Double> documentposteriors,
           HashMap<Integer, ExpansionTerm> termDocFrequencies) {
      TreeSet<ExpansionTerm> terms = new TreeSet<ExpansionTerm>();
      DocTF doctf = (DocTF)repository.getFeature(DocTF.class, "all");
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
      DocForward forward = (DocForward)repository.getFeature(DocForward.class, "all");
      for (Document d : documents) {
         for (int termid : d.getIntArray(forward)) {
            ExpansionTerm t = doctermfrequencies.get(termid);
            if (t == null) {
               doctermfrequencies.put(termid, t = new ExpansionTerm(termid));
            }
            Integer docfrequency = t.docfrequencies.get(d);
            t.docfrequencies.put(d, (docfrequency == null) ? 1 : docfrequency + 1);
         }
      }
      return doctermfrequencies;
   }

   class ExpansionTerm implements Comparable<ExpansionTerm> {

      HashMap<Document, Integer> docfrequencies = new HashMap<Document, Integer>();
      int termid;
      String stemmedterm;
      double weight;

      public ExpansionTerm(int termid) {
         this.termid = termid;
      }

      public int hashCode() {
         return termid;
      }

      public boolean equals(Object o) {
         return (o instanceof ExpansionTerm && ((ExpansionTerm) o).termid == termid);
      }

      @Override
      public int compareTo(ExpansionTerm o) {
         return (weight < o.weight) ? 1 : -1;
      }
   }
}
