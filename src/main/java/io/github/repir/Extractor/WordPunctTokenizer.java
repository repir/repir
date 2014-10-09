package io.github.repir.Extractor;

import io.github.repir.Extractor.Tools.ConvertHtmlASCIICodes;
import io.github.repir.Extractor.Tools.ConvertHtmlAmpersand;
import io.github.repir.Extractor.Tools.ConvertHtmlSpecialCodes;
import io.github.repir.Extractor.Tools.ConvertUnicodeDiacritics;
import io.github.repir.Extractor.Tools.RemoveHtmlComment;
import io.github.repir.Extractor.Tools.RemoveHtmlSpecialCodes;
import io.github.repir.Extractor.Tools.RemoveHtmlTags;
import io.github.repir.Extractor.Tools.RemoveNonASCII;
import io.github.repir.Extractor.Tools.TokenProcessor;
import io.github.repir.Extractor.Tools.TokenWord;
import io.github.repir.Extractor.Tools.TokenizerRegex;
import io.github.repir.tools.Lib.Log;
import java.util.ArrayList;
/**
 *
 * @author jeroen
 */
public class WordPunctTokenizer extends Extractor {
   public static final Log log = new Log( WordPunctTokenizer.class );

   public WordPunctTokenizer() {
       super();
       //this.addPreProcessor(RemoveHtmlComment.class);
       this.addPreProcessor(ConvertHtmlASCIICodes.class);
       this.addPreProcessor(ConvertHtmlSpecialCodes.class);
       //this.addPreProcessor(ConvertUnicodeDiacritics.class);
       //this.addPreProcessor(new RemoveNonASCII(this, true));

       this.addSectionProcess("all", "tokenize", "result");

       this.addProcess("tokenize", ConvertHtmlAmpersand.class);
       //this.addProcess("tokenize", RemoveHtmlTags.class);
       //this.addProcess("tokenize", RemoveHtmlSpecialCodes.class);
       this.addProcess("tokenize", getTokenizer());
   }
   
   private TokenizerRegex getTokenizer() {
       TokenizerRegex tokenizerRegex = new TokenizerRegex(this, "tokenize");
       tokenizerRegex.setupTokenProcessor("word", TokenWord.class);
       tokenizerRegex.setupTokenProcessor("space", TokenWord.class);
       return tokenizerRegex;
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
