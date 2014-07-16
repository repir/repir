package io.github.repir.Repository;

import io.github.repir.tools.Buffer.BufferSerializable;
import io.github.repir.tools.Content.EOCException;
import io.github.repir.tools.Structure.StructureReader;
import io.github.repir.tools.Structure.StructureWriter;
import io.github.repir.tools.Lib.MathTools;

/**
 * Represents a Term feature, that should constructed using the repository
 * {@link Repository#getTerm(java.lang.String orginalterm)}, {@link Repository#getTerm(int)} or
 * {@link Repository#getProcessedTerm(java.lang.String processedterm)}. The orginal term
 * should only be used as input, for which {@link #getProcessedTerm()} will then return the lowercased and stemmed variant.
 * {@link #getID()} will return the internal term ID from the vocabulary. If the
 * original ID is not used in the constructor, it is set to null.
 * @author Jeroen Vuurens
 */
public class Term implements BufferSerializable {
  private String originalTerm;
  private int termid;
  private int hashcode = -1;
  private String processedTerm;
  private boolean isstopword;

  protected Term() {}
  
  protected Term(int termid, String term, String stemmedterm, boolean isstopword) {
     this.processedTerm = stemmedterm;
     this.termid = termid;
     this.originalTerm = term;
     this.isstopword = isstopword;
  }
  
  public boolean exists() {
     return termid > -1;
  }
  
  public boolean isStopword() {
     return isstopword;
  }
  
  @Override
  public int hashCode() {
     if (hashcode == -1)
        hashcode = MathTools.hash(termid);
     return hashcode;
  }
  
  public int getID() {
     return termid;
  }
  
  public String getProcessedTerm() {
     return processedTerm;
  }
  
  public String getOriginalTerm() {
     return originalTerm;
  }
  
  @Override
  public boolean equals(Object o) {
     if (o instanceof Term) {
        Term t = (Term) o;
        if (termid >= 0)
           return termid == t.termid;
        else if (t.termid < 0)
           return false;
        else 
           return originalTerm.equals(t.originalTerm);
     }
     return false; 
  }
  
  @Override
  public String toString() {
     if (originalTerm != null)
        return originalTerm;
     if (processedTerm != null)
        return "@" + processedTerm;
     return "@#" + termid;
  }
  
   @Override
   public void read(StructureReader reader) throws EOCException {
      originalTerm = reader.readString();
      processedTerm = reader.readString();
      termid = reader.readInt();
      isstopword = reader.readBoolean();
   }

   @Override
   public void write(StructureWriter writer) {
      writer.write(originalTerm);
      writer.write(processedTerm);
      writer.write(termid);
      writer.write(isstopword);
   }
}
