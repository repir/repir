package io.github.repir.Strategy.Operator;

import java.util.ArrayList;

/**
 * This class is used to contain the values used for scoring a Feature.
 * <p/>
 * @author jeroen
 */
public class OperatorValues {

   /**
    * positions of feature occurrences document
    */
   public int pos[];
   /**
    * word span of feature occurrences in the document
    */
   public int dist[];
   /**
    * frequency of the feature in the document
    */
   public double frequency;
   public ArrayList<Double> frequencylist;
   /**
    * document frequency of the feature in the corpus
    */
   public long df = -1;
   /**
    * frequency of the feature in the corpus
    */
   public long cf = -1;
   /**
    * query feature weight (for queries this can be set using term#weight, or
    * used to adjust the weight for query expansion terms)
    */
   public double secondaryfrequency;
   public double queryweight = 1;
   public double documentprior = 1;

   @Override
   public OperatorValues clone() {
      OperatorValues v = new OperatorValues();
      copy(v);
      return v;
   }

   public void copy(OperatorValues v) {
      v.df = df;
      v.cf = cf;
   }
}
