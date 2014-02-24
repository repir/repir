package io.github.repir.Strategy;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import io.github.repir.Repository.DocForward;
import io.github.repir.Repository.DocLiteral;
import io.github.repir.Repository.Feature;
import io.github.repir.Repository.ReportableFeature;
import io.github.repir.Repository.ReportedUnstoredFeature;
import io.github.repir.Repository.Repository;
import io.github.repir.Repository.StoredReportableFeature;
import io.github.repir.tools.Lib.Log; 

/**
 *
 * @author Jeroen Vuurens
 */
public class RetrievalModelReportedFeatures extends ArrayList<String> {
  public static Log log = new Log( RetrievalModelReportedFeatures.class );
  public HashMap<String, Feature> featuresmap;
  public Repository repository;
   private ArrayList<ReportedUnstoredFeature> reportableunstoredfeatures;
   private ArrayList<StoredReportableFeature> storedreportablefeatures;
   private ArrayList<ReportableFeature> reportablefeatures;

  public RetrievalModelReportedFeatures(Repository repository) {
     this.repository = repository;
  }
  
  @Override
  public boolean add(String a) {
     featuresmap = null;
     return super.add(a);
  }
  
   private HashMap<String, Feature> getFeaturesMap() {
      if (this.featuresmap == null) {
         int featurenr = 0;
         featuresmap = new HashMap<String, Feature>();
         for (String featurename : this) {
            if (!featuresmap.containsKey(featurename)) {
               Feature f = repository.getFeature(featurename);
               featuresmap.put(featurename, f);
               if (f instanceof ReportableFeature)
                 ((ReportableFeature)f).setReportID(featurenr++);
            }
         }
      }
      return featuresmap;
   }

   public Collection<ReportableFeature> getReportableFeatures() {
      if (reportablefeatures == null) {
         reportablefeatures = new ArrayList<ReportableFeature>();
         for (Feature f : getFeaturesMap().values()) {
            if (f instanceof ReportableFeature)
               reportablefeatures.add((ReportableFeature)f);
         }
      }
      return reportablefeatures;
   }

   public Collection<StoredReportableFeature> getReportedStoredFeatures() {
      if (storedreportablefeatures == null) {
         storedreportablefeatures = new ArrayList<StoredReportableFeature>();
         for (ReportableFeature f : getReportableFeatures()) {
            if (f instanceof StoredReportableFeature) {
               storedreportablefeatures.add((StoredReportableFeature) f);
            }
         }
      }
      return storedreportablefeatures;
   }

   public Collection<ReportedUnstoredFeature> getReportedUnstoredFeatures() {
      if (reportableunstoredfeatures == null) {
         reportableunstoredfeatures = new ArrayList<ReportedUnstoredFeature>();
         for ( ReportableFeature f : getReportableFeatures() ) {
            if (f instanceof ReportedUnstoredFeature) {
               reportableunstoredfeatures.add((ReportedUnstoredFeature)f);
            }
         }
      }
      return reportableunstoredfeatures;
   }

   public ReportableFeature getReportedFeature(String name) {
      Feature f = getFeaturesMap().get(name);
      return (f instanceof ReportableFeature)?((ReportableFeature)f):null;
   }

   public DocLiteral getLiteral(String name) {
      return (DocLiteral) getFeaturesMap().get(name);
   }

   public DocForward getForward(String name) {
      return (DocForward) getFeaturesMap().get(name);
   }

}
