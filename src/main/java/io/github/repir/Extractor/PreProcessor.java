package io.github.repir.Extractor;

/**
 *
 * @author Jeroen
 */
public abstract interface PreProcessor {

   /**
    *
    * @param buffer
    * @param offset
    * @param end
    */
   public void processBytes(byte[] buffer, int offset, int end);
}
