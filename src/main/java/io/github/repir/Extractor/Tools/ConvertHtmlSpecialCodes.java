package io.github.repir.Extractor.Tools;

import io.github.repir.Extractor.Extractor;
import io.github.repir.tools.Lib.Log;

/**
 * Replaces certain HTML special codes with a byte equivalent. Using Translator
 * the replacements should have the same byte length.
 * <p/>
 * @author jeroen
 */
public class ConvertHtmlSpecialCodes extends Translator {

   public static Log log = new Log(ConvertHtmlSpecialCodes.class);

   public ConvertHtmlSpecialCodes(Extractor extractor, String process) {
      super(extractor, process);
      add("&quot;", "\"");
      add("&amp;", "&");
      add("&lt;", "<");
      add("&gt;", ">\000\000\000");
      add("&nbsp;", " \000\000\000\000");
      add("&ndash;", "-");
      add("&mdash;", "-");
      add("&ldquo;", "\"");
      add("&rdquo;", "\"");
      add("&lsquo;", "'");
      add("&rsquo;", "'");
   }
}
