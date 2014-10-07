package io.github.repir.Extractor;

import io.github.repir.Extractor.Tools.ConvertHtmlASCIICodes;
import io.github.repir.Extractor.Tools.ConvertHtmlAmpersand;
import io.github.repir.Extractor.Tools.ConvertHtmlSpecialCodes;
import io.github.repir.Extractor.Tools.ConvertUnicodeDiacritics;
import io.github.repir.Extractor.Tools.RemoveHtmlComment;
import io.github.repir.Extractor.Tools.RemoveHtmlSpecialCodes;
import io.github.repir.Extractor.Tools.RemoveHtmlTags;
import io.github.repir.Extractor.Tools.RemoveNonASCII;
import io.github.repir.tools.Lib.Log;
import java.util.ArrayList;
/**
 *
 * @author jeroen
 */
public class DefaultTokenizer extends Extractor {
   public static final Log log = new Log( DefaultTokenizer.class );

   public DefaultTokenizer() {
       super();
       this.addPreProcessor(RemoveHtmlComment.class);
       this.addPreProcessor(ConvertHtmlASCIICodes.class);
       this.addPreProcessor(ConvertHtmlSpecialCodes.class);
       this.addPreProcessor(ConvertUnicodeDiacritics.class);
       this.addPreProcessor(new RemoveNonASCII(this, true));

       this.addSectionProcess("all", "tokenize", "result");

       this.addProcess("tokenize", ConvertHtmlAmpersand.class);
       this.addProcess("tokenize", RemoveHtmlTags.class);
       this.addProcess("tokenize", RemoveHtmlSpecialCodes.class);
       this.addProcess("tokenize", getTokenizer());
   }
   
   private io.github.repir.Extractor.Tools.Tokenizer getTokenizer() {
       String splitbefore = "%[*^<{($#@,?-+&";
       String splitafter = "\\~|\"'`]})>/_!:;=";
       String leavefirst = "A-Z a-z 0-9";
       String leavelast = "A-Z a-z 0-9";
       boolean splitnumbers = false;
       boolean lowercase = false;
       int maxtokenlength = 25;
       return new io.github.repir.Extractor.Tools.Tokenizer(this, "tokenize",
              splitbefore, splitafter, leavefirst, leavelast,
              splitnumbers, lowercase, maxtokenlength);
   }
   
   public ArrayList<String> tokenize(byte content[]) {
       Entity entity = new Entity();
       entity.setContent(content);
       this.process(entity);
       return entity.get("result");
   }
   
   public ArrayList<String> tokenize(String text) {
       return tokenize(text.getBytes());
   }
}
