package io.github.repir.Retriever.Tuner;
import java.math.BigDecimal; 
import io.github.repir.tools.Lib.Log;

/**
 * Parameter that is tuned using grid search. Initialize with a parameter name
 * and a range e.g. 100..1000..100 means the values 100, 200, ..., 1000 are
 * valid.
 * @author Jeroen Vuurens
 */
public class ParameterGrid extends Parameter {
   public static Log log = new Log(ParameterGrid.class);


  public ParameterGrid(String parameter, String range) {
      super(parameter, range);
  }

   @Override
   public void generatePoints() {
      for (BigDecimal b = this.lower; b.compareTo(this.upper) <= 0; b = b.add(this.targetstep)) {
         add(b);  
      }
   }
}
