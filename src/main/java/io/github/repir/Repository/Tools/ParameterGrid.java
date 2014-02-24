package io.github.repir.Repository.Tools;
import java.math.BigDecimal; 
import io.github.repir.tools.Lib.Log;

/**
 *
 * @author Jeroen Vuurens
 */
public class ParameterGrid extends Parameter {
   public static Log log = new Log(ParameterGrid.class);


  public ParameterGrid(String setting) {
      super(setting);
  }

   @Override
   public void generatePoints() {
      for (BigDecimal b = this.lower; b.compareTo(this.upper) <= 0; b = b.add(this.targetstep)) {
         add(b);  
      }
   }
}
