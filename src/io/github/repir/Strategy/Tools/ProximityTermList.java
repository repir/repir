package io.github.repir.Strategy.Tools;
import java.util.TreeSet; 
import io.github.repir.Strategy.Tools.ProximitySet.ProximityTerm;

/**
 * List that contains the current state of the Operators that are in the document
 * from the current position.
 * @author Jeroen Vuurens
 */
public class ProximityTermList extends TreeSet<ProximityTerm> {

  public ProximityTermList() {
      super();
  }
}
