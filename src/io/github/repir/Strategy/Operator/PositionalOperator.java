package io.github.repir.Strategy.Operator;

import io.github.repir.Retriever.Document;
import java.util.ArrayList;

/**
 * <p/>
 * @author jeroen
 */
public interface PositionalOperator {
   public boolean equals(Object o);
   
   public int getSpan();
   
   public int[] getPos();
   
   public void process( Document doc );
   
   public void setQueryWeight( double weight );
   
   public void setDocumentPrior( double prior );
   
   public void clearFrequencyList();
   
   public ArrayList<Double> getFrequencyList();
   
   public void setFrequency( double freq );
   
   public boolean isStopword( );

}
