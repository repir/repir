package io.github.repir.Extractor;

import io.github.repir.tools.Lib.Log;

/**
 * This class feeds input data to a Extractor as arrays of bytes.
 * Implementations of this class focus on a specific type of source.
 */
public interface Reader {

   /**
    * read a buffer of bytes from a data source.
    * <p/>
    * @param buffer
    * @param offset
    * @return the number of bytes read
    */
   public abstract int read(byte[] buffer, int offset);

   /**
    * skip towards an offset
    * <p/>
    * @param offset
    */
   public abstract void setOffset(long offset);

   public abstract String printOffset();

   public abstract long getBufferStart();
}
