package io.github.repir.Retriever;
import java.util.Comparator;

/**
 *
 * @author Jeroen Vuurens
 */
public class DocumentComparator implements Comparator<Document> {

   public int compare(Document o1, Document o2) {
      if (o1.score != o2.score)
         return (o1.score > o2.score) ? -1 : 1;
      // To create stable output when the score of two documents is exactly the same
      if (o1.partition != o2.partition)
         return (o1.partition < o2.partition)? -1 : 1;
      return (o1.docid < o2.docid)? -1 : 1;
   }
}
