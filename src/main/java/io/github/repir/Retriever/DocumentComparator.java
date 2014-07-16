package io.github.repir.Retriever;
import java.util.Comparator;

/**
 * Sorts documents descending on score. Sorting is stabilized for Documents
 * that obtain the same score, by sorting these on partition and docid. 
 * @author Jeroen Vuurens
 */
public class DocumentComparator implements Comparator<Document> {

   @Override
   public int compare(Document o1, Document o2) {
      if (o1.score != o2.score)
         return (o1.score > o2.score) ? -1 : 1;
      // To create stable output when the score of two documents is exactly the same
      return o1.getCollectionID().compareTo(o2.getCollectionID());
   }
}
