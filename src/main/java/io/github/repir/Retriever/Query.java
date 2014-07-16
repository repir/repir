package io.github.repir.Retriever;

import io.github.repir.tools.Structure.StructureReader;
import io.github.repir.tools.Structure.StructureWriter;
import io.github.repir.Repository.Feature;
import io.github.repir.Repository.Repository;
import io.github.repir.tools.Lib.Log;
import io.github.repir.Strategy.Strategy;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import io.github.repir.Strategy.RetrievalModel;
import io.github.repir.tools.Buffer.BufferSerializable;
import io.github.repir.tools.Content.EOCException;
import io.github.repir.tools.Lib.ClassTools;
import java.util.Collections;

/**
 * A Query object contains the query request, settings such as retrievalmodel,
 * scorefunction, retrieved features, query specific parameter settings,
 * and optionally contains the query results after retrieval. 
 * Query itself does not contain much logic, but is more a data container.
 * <p/>
 * @author jeroen
 */
public class Query implements BufferSerializable,Comparable<Query> {

   public static Log log = new Log(Query.class);
   public Strategy strategy;
   public Repository repository;
   /**
    * The document class used to construct new Document objects.
    */
   public String documentclass;
   private Constructor documentconstructor;
   private Constructor documentconstructor2;
   
   public String documentcomparatorclass;
   private Comparator<Document> documentcomparator;

   /**
    * The original query string can be reformulated, by the tokenizer used,
    * which will remove any meaningless code, or by the retrieval model.
    */
   public String originalquery;
   public String query;

   /**
    * If true then stopwords are removed. This is usually done during the
    * preRetrieval phase, meaning that the InferenceModel initially contains
    * stopwords, so that for instances literal phrases that contain stopwords
    * can set their span to a correct width. When the stopwords are removed from
    * the InferenceModel, the phrase spans are already set.
    */
   public boolean removeStopwords;
   /**
    * The maximum number of documents to readValue.
    */
   public int documentlimit;
   /**
    * Query ID that can be used by the caller to identify results.
    */
   public int id;

   public String domain;
   
   /**
    * The returned ranked set of documents. Although this is commonly returned,
    * a custom Strategy could also return custom data through the
    * Collector.
    */
   private Document queryresults[] = new Document[0];
   /**
    * The requestedfeatures that are required to be fetched in order to
    * score/report each document. If the Boolean is true, the Feature will be
    * reported
    */
   public ArrayList<String> reportedFeatures = new ArrayList<String>();
   
   public int partition;

   public ArrayList<Document> resultsarraylist = new ArrayList<Document>();
   public ArrayList<Variant> variants = new ArrayList<Variant>();
   ArrayList<Integer> dovariants = new ArrayList<Integer>();
   
   /**
    * A common way to construct a Query object is to use {@link IndexReader.IndexReader#constructQueryRequest(int, java.lang.String)
    * }
    * which sets all parameter to the repository defaults.
    */
   public Query() {
   }

   public Query(Repository repository, int queryid, String query) {
      addVariant(new Variant(
              repository.getConfiguration().get("retriever.strategy", "RetrievalModel"),
              repository.getConfiguration().get("retriever.scorefunction", "ScoreFunctionKLD"), 
              null));
      this.repository = repository;
      this.id = queryid;
      this.originalquery = query;
      documentlimit = repository.getConfiguration().getInt("retriever.documentlimit", 10);
      documentclass = repository.getConfiguration().get("retriever.documentclass", Document.class.getCanonicalName());
      documentcomparatorclass = repository.getConfiguration().get("retriever.documentcomparatorclass", DocumentComparator.class.getCanonicalName());
      removeStopwords = repository.getConfiguration().getBoolean("retriever.removestopwords", false);
   }

   /**
    * @param queryid Query ID that can be used by the caller to identify results
    * @param query String that is used to readValue documents
    * @param strategy name of the Strategy to use
    * @param documentlimit maximum number retrieved documents
    */
   public Query(Repository repository, int queryid, String query, String retrievalmodel, int limit) {
      this(repository, queryid, query);
      this.setStrategyClassname(retrievalmodel);
      this.documentlimit = limit;
   }

   public Query(Query q) {
      repository = q.repository;
      id = q.id;
      originalquery = q.originalquery;
      query = q.query;
      documentlimit = q.documentlimit;
      documentclass = q.documentclass;
      documentcomparatorclass = q.documentcomparatorclass;
      for (String f : q.reportedFeatures)
         reportedFeatures.add(f);
      removeStopwords = q.removeStopwords;
      partition = q.partition;
      domain = q.domain;
      for (Variant v : q.variants)
         variants.add(new Variant(v.retrievalmodelclass, v.scorefunctionclass, v.configuration));
      dovariants.addAll(q.dovariants);
   }
   
   public void setRepository(Repository repository) {
      this.repository = repository;
   }

   public RetrievalModel getRetrievalModel() {
      return (RetrievalModel)strategy;
   }
   
   public boolean done() {
      return getStrategyClass() == null;
   }

   public void addFeature(String featurename) {
      if (!reportedFeatures.contains(featurename)) {
         reportedFeatures.add(featurename);
      }
   }

   public void addCollectionID() {
      String name = repository.getCollectionIDFeature().getCanonicalName();
      if (!reportedFeatures.contains(name)) {
         reportedFeatures.add(name);
      }
   }

   public void addFeatureClass(Class featureclass, String ... param) {
      addFeature(Feature.canonicalName(featureclass, param));
   }

   public void clearFeatures() {
      reportedFeatures = new ArrayList<String>();
   }

   public String getConfiguration() {
      return variants.get(getVariantID()).configuration;  
   }
   
   public String getStrategyClass() {
      return variants.get(getVariantID()).retrievalmodelclass;  
   }
   
   public String getScorefunctionClass() {
      return variants.get(getVariantID()).scorefunctionclass;  
   }
   
   public void setStrategyClassname(String strategyclass) {
      variants.get(getVariantID()).retrievalmodelclass = strategyclass;  
   }
   
   public void setStrategyClass(Class strategyclass) {
      variants.get(getVariantID()).retrievalmodelclass = strategyclass.getSimpleName();  
   }
   
   public void setScorefunctionClassname(String scorefunctionclass) {
      variants.get(getVariantID()).scorefunctionclass = scorefunctionclass;  
   }
   
   public void setScorefunctionClass(Class scorefunctionclass) {
      variants.get(getVariantID()).scorefunctionclass = scorefunctionclass.getSimpleName();  
   }
   
   public void setConfiguration(String configuration) {
      variants.get(getVariantID()).configuration = configuration;  
   }
   
   public void addFeature(Feature feature) {
      String name = feature.getLabel();
      addFeature(name);
   }

   public void add(Document d) {
      this.resultsarraylist.add(d);
   }
   
   public void clearResults() {
      resultsarraylist.clear();
      queryresults = null;
   }

   public Document[] getQueryResults() {
       if (queryresults == null || (resultsarraylist != null && queryresults.length != resultsarraylist.size())) {
           Collections.sort(resultsarraylist, new DocumentComparator());
           queryresults = this.resultsarraylist.toArray(new Document[resultsarraylist.size()]);
       }
       return queryresults;
   }
   
   @Override
   public void write(StructureWriter writer) {
      writer.write(partition);
      writer.write(id);
      writer.write(domain);
      writer.write(originalquery);
      writer.write(query);
      writer.write(documentlimit);
      writer.write(documentclass);
      writer.write(documentcomparatorclass);
      writer.write(removeStopwords);
      writer.writeStr(reportedFeatures);
      writer.write(variants.size());
      for (Variant v : variants) {
         v.write(writer);
      }
      writer.writeC(dovariants);
      writer.write(getQueryResults().length);
      for (Document doc : getQueryResults()) {
         doc.write(writer);
      }
   }

   @Override
   public void read(StructureReader reader) throws EOCException {
      readHeader(reader);
      int results = reader.readInt();
      queryresults = new Document[results];
      for (int i = 0; i < results; i++) {
         Document doc = createDocument();
         doc.read(reader);
         queryresults[i] = doc;
      }
   }

   public void readHeader(StructureReader reader) throws EOCException {
      partition = reader.readInt();
      id = reader.readInt();
      domain = reader.readString();
      originalquery = reader.readString();
      query = reader.readString();
      documentlimit = reader.readInt();
      documentclass = reader.readString();
      documentcomparatorclass = reader.readString();
      removeStopwords = reader.readBoolean();
      reportedFeatures = reader.readStrArrayList();
      int variants = reader.readInt();
      for (int i = 0; i < variants; i++) {
         Variant v = new Variant();
         v.read(reader);
         this.variants.add(v);
      }
      dovariants = reader.readCIntArrayList();
   }

   /**
    * for debug purposes
    */
   public void print(String feature) {
      int rank = 1;
      for (Document d : getQueryResults()) {
         log.printf("%d %5d#%3d %f", rank++, d.docid, d.partition, d.score);
         if (rank > 10) {
            break;
         }
      }
   }

   public int compareTo(Query o) {
      return (this.id < o.id)?-1:1;
   }
   
   public Document createDocument() {
        if (documentconstructor == null) {
           Class dclass = ClassTools.toClass(this.documentclass, Document.class.getPackage().getName());
           documentconstructor = ClassTools.getAssignableConstructor(dclass, Document.class);
        }
        Document d = (Document)ClassTools.construct(documentconstructor);
        return d;
   }
   
   public Document createDocument(RetrievalModel retrievalmodel, int id, int partition) {
        if (documentconstructor2 == null) {
           Class dclass = ClassTools.toClass(this.documentclass, Document.class.getPackage().getName());
           documentconstructor2 = ClassTools.getAssignableConstructor(dclass, Document.class, RetrievalModel.class, int.class, int.class);
        }
        Document d = (Document)ClassTools.construct(documentconstructor2, retrievalmodel, id, partition);
        return d;
   }
   
   public Comparator<Document> getDocumentComparator() {
      if (documentcomparator == null) {
         Class clazz = ClassTools.toClass(documentcomparatorclass, DocumentComparator.class.getPackage().getName());
         Constructor c = ClassTools.getAssignableConstructor(clazz, Comparator.class);
         documentcomparator = (Comparator<Document>)ClassTools.construct(c);
      }
      return documentcomparator;
   }
   
   public void setVariantID( int variant ) {
      id = createVariantID( variant );
   }
   
   private int createVariantID( int variant ) {
      int newid = getID(id);
      newid |= (variant << 10);
      return newid;
   }
   
   public int getVariantID() {
      return getVariantID(id);  
   }
   
   public int getID() {
      return getID(id);  
   }
   
   public static int getID( int queryid ) {
      return queryid & 1023;  
   }
   
   public static int getVariantID( int queryid ) {
      return queryid >> 10;  
   }
   
   public Collection<String> getReducerID() {
      ArrayList<String> reducers = new ArrayList<String>();
      for (int i : dovariants) {
         reducers.add(Integer.toString(createVariantID(i)));
      }
      return reducers;
   }
   
   public void initVariants() {
      variants = new ArrayList<Variant>();
      dovariants = new ArrayList<Integer>();
   }
   
   public void addVariant( String retrievalmodelclass, String scorefunctionclass, String settings ) {
      addVariant(new Variant( retrievalmodelclass, scorefunctionclass, settings));
   }
   
   public void addVariant( Variant v ) {
      dovariants.add(variants.size());
      variants.add( v );
   }
   
   public int variantCount() {
      return dovariants.size();
   }
   
   public Iterable<Query> variantIterator() {
      return new VariantIter();
   }
   
   public Query splitVariants() {
      Query q = new Query( this );
      q.dovariants = new ArrayList<Integer>();
      int count = variantCount() / 2;
      for (int i = 0; i < count; i++)
         q.dovariants.add(dovariants.remove(0));
      return q;
   }

   
   public static class Variant implements BufferSerializable {
      /**
       * The name of the next Strategy to use. In Query request, this is the
       * initial Strategy, which can be followed by consecutive
       * RetrievalModels for multi-pass retrieval strategies. Query objects that
       * are returned with results have a null-value for the strategyclass,
       * indicating that no next retrieval pass is necessary.
       */
      public String retrievalmodelclass;
      public String scorefunctionclass;
      public String configuration;

      public Variant() {}
      
      public Variant(String retrievalmodelclass, String scorefunctionclass, String settings ) {
         this.retrievalmodelclass = retrievalmodelclass;
         this.scorefunctionclass = scorefunctionclass;
         this.configuration = settings;
      }
      
      @Override
      public void read(StructureReader reader) throws EOCException {
         retrievalmodelclass = reader.readString();
         scorefunctionclass = reader.readString();
         configuration = reader.readString();
      }

      @Override
      public void write(StructureWriter writer) {
         writer.write(retrievalmodelclass);
         writer.write(scorefunctionclass);
         writer.write(configuration);
      }
   }
   
   class VariantIter implements Iterator<Query>, Iterable<Query> {
      Iterator<Integer> iter;
      
      public VariantIter() {
         iter = dovariants.iterator();
      }
      
      @Override
      public boolean hasNext() {
         return iter.hasNext();
      }

      @Override
      public Query next() {
         int v = iter.next();
         setVariantID(v);
         return Query.this;
      }

      @Override
      public void remove() {
         throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
      }

      @Override
      public Iterator<Query> iterator() {
         return this;
      }
      
   }
}
