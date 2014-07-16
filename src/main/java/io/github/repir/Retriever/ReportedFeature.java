package io.github.repir.Retriever;
import io.github.repir.Repository.ReportableFeature; 
import io.github.repir.tools.Lib.Log;

/**
 *
 * @author Jeroen Vuurens
 */
public class ReportedFeature<F extends ReportableFeature> {
   public static Log log = new Log(ReportedFeature.class);
   public String canonicalname; 
   public F feature;
   public int reportID;

  public ReportedFeature(String canonicalname, ReportableFeature f) {
      this.canonicalname = canonicalname;
      this.feature = (F) f;
  }

   public ReportedFeature(ReportedFeature f) {
      this.canonicalname = f.canonicalname;
      this.feature = (F) f.feature;
      this.reportID = f.reportID;
   }

   @Override
   public int hashCode() {
      return canonicalname.hashCode();
   }

   @Override
   public boolean equals(Object o) {
      return (o instanceof String) && ((String) o).equals(canonicalname) || (o instanceof ReportableFeature) && ((ReportableFeature) o).getCanonicalName().equals(canonicalname) || (o instanceof ReportedFeature) && ((ReportedFeature) o).canonicalname.equals(canonicalname);
   }

}
