package io.github.repir.Extractor.Tools;

import io.github.repir.tools.Lib.Log;
import io.github.repir.Extractor.Entity;
import io.github.repir.Extractor.Extractor;
import java.util.ArrayList;

/**
 *
 * @author jeroen
 */
public class Tokenizer extends ExtractorProcessor {

   public static Log log = new Log(Tokenizer.class);
   public byte[] buffer;
   public int bufferpos = 0, bufferend = 0;
   public boolean[] tokensplitbefore = new boolean[128];
   public boolean[] tokensplitafter = new boolean[128];
   public boolean[] tokenpoststripper = new boolean[128];
   public boolean[] tokenprestripper = new boolean[128];
   public boolean[][] splitpeek = new boolean[128][128];
   public final char maxbyte = Byte.MAX_VALUE;
   public final char minbyte = 0x0;
   public final int maxtokenlength;
   // The following variables are for use within the tokenizer. Using class variables
   // is faster than using local variables.
   boolean readingToken;
   byte byte0, byte1;
   boolean splittoken = false, splitafter = false;
   boolean skip = false;

   public Tokenizer(Extractor extractor, String process) {
      super(extractor, process);
      setBooleanArray(tokensplitbefore, minbyte, maxbyte, false);
      setBooleanArray(tokensplitafter, minbyte, maxbyte, false);
      addTokenSplitBefore("< \t\n\r");
      setBooleanArray(tokenpoststripper, minbyte, maxbyte, true);
      setBooleanArray(tokenpoststripper, 'A', 'Z', false);
      setBooleanArray(tokenpoststripper, '0', '9', false);
      setBooleanArray(tokenpoststripper, 'a', 'z', false);
      setBooleanArray(tokenpoststripper, false, minbyte);
      setBooleanArray(tokenprestripper, minbyte, maxbyte, true);
      setBooleanArray(tokenprestripper, 'A', 'Z', false);
      setBooleanArray(tokenprestripper, 'a', 'z', false);
      setBooleanArray(tokenprestripper, '0', '9', false);
      setBooleanArray(tokenprestripper, false, '.', '-', '$', '&');
      setBooleanArray(tokensplitbefore, minbyte, maxbyte, false);
      setBooleanArray(tokensplitbefore, true, ' ', '\t', '\n', '.', '!', '?', '-', ':', '"', '/', '\\');

      setBooleanArray(tokensplitbefore, minbyte, maxbyte, false);
      addTokenSplitBefore("\r\n\t ");
      addTokenSplitAfter("\r\n\t ");
      //log.info("tokensplitbefore %s", extractor.getConfigurationString(process, "splittokenafter", ""));
      addTokenSplitAfter(extractor.getConfigurationString(process, "splittokenafter", ""));
      addTokenSplitBefore(extractor.getConfigurationString(process, "splittokenbefore", ""));
      setBooleanArray(tokenpoststripper, minbyte, maxbyte, true);
      setBooleanArray(tokenprestripper, minbyte, maxbyte, true);
      for (String channels : extractor.getConfigurationStrings(process, "leavefirst", new String[0])) {
         String args[] = io.github.repir.tools.Lib.StrTools.split(channels, " ");
         for (String set : args) {
            if (set.indexOf('-') == 1 && set.length() == 3) {
               setBooleanArray(tokenprestripper, set.charAt(0), set.charAt(2), false);
            } else {
               for (char c : set.toCharArray()) {
                  setBooleanArray(tokenprestripper, c, c, false);
               }
            }
         }
      }
      for (String channels : extractor.getConfigurationStrings(process, "leavelast", new String[0])) {
         String args[] = io.github.repir.tools.Lib.StrTools.split(channels, " ");
         for (String set : args) {
            if (set.indexOf('-') == 1 && set.length() == 3) {
               setBooleanArray(tokenpoststripper, set.charAt(0), set.charAt(2), false);
            } else {
               for (char c : set.toCharArray()) {
                  setBooleanArray(tokenpoststripper, c, c, false);
               }
            }
         }
      }
      if (extractor.getConfigurationBoolean(process, "splitnumbers", false)) {
         this.splitpeek('a', 'z', '0', '9');
         this.splitpeek('A', 'Z', '0', '9');
         this.splitpeek('0', '9', 'a', 'z');
         this.splitpeek('0', '9', 'A', 'Z');
      }
      maxtokenlength = extractor.conf.getInt("extractor." + process + ".maxtokenlength", Integer.MAX_VALUE);
   }

   protected void splitpeek(char firststart, char firstend, char secondstart, char secondend) {
      for (int f = firststart; f <= firstend; f++) {
         for (int s = secondstart; s <= secondend; s++) {
            splitpeek[f][s] = true;
         }
      }
   }

   @Override
   public void process(Entity entity, Entity.SectionPos section, String attribute) {
      this.buffer = entity.content;
      this.bufferpos = section.open;
      this.bufferend = section.close;
      if (bufferpos >= bufferend) {
         return;
      }
      initialize();
      ArrayList<String> list = loadTokens();
      entity.get(attribute).addAll(list);
   }

   protected void initialize() {
      readingToken = false;
      skip = false;
      splittoken = false;
   }

   public void setBooleanArray(boolean[] a, char from, char to, boolean s) {
      for (; from <= to; from++) {
         a[from] = s;
      }
   }

   public void setBooleanArray(boolean a[], boolean s, char... c) {
      for (char b : c) {
         a[b] = s;
      }
   }

   public void addTokenSplitBefore(String s) {
      byte tokensepbyte[] = s.getBytes();
      for (byte b : tokensepbyte) {
         this.tokensplitbefore[b] = true;
      }
   }

   public void addTokenSplitAfter(String s) {
      byte tokensepbyte[] = s.getBytes();
      for (byte b : tokensepbyte) {
         this.tokensplitafter[b] = true;
      }
   }

   protected ArrayList<String> loadTokens() {
      ArrayList<String> chunks = new ArrayList<String>();
      int pos = 0, tokenStart = 0;
      boolean inToken = false;
      byte peek = (bufferpos < bufferend) ? buffer[bufferpos] : 0;
      for (pos = bufferpos; pos < bufferend; pos++) {
         byte0 = peek;
         peek = (pos + 1 < bufferend) ? buffer[pos + 1] : 0;
         if (!inToken) {
            if (!this.tokenprestripper[byte0]) {
               tokenStart = pos;
               inToken = true;
               if (this.tokensplitafter[byte0]) {
                  addToken(chunks, tokenStart, pos + 1);
                  inToken = false;
               } else if (splitpeek[byte0][peek]) {
                  addToken(chunks, tokenStart, pos + 1);
                  tokenStart = pos + 1;
               }
            }
         } else {
            if (this.tokensplitbefore[byte0]) {
               addToken(chunks, tokenStart, pos);
               tokenStart = pos;
               inToken = !tokenprestripper[byte0];
            }
            if (this.tokensplitafter[byte0]) {
               addToken(chunks, tokenStart, pos + 1);
               inToken = false;
            } else if (splitpeek[byte0][peek]) {
               addToken(chunks, tokenStart, pos + 1);
               tokenStart = pos + 1;
            }
         }
      }
      if (inToken) {
         addToken(chunks, tokenStart, bufferend);
      }
      return chunks;
   }

   private void addToken(ArrayList<String> list, int tokenStart, int tokenend) {
      char c[];
      //log.info("addToken %s", new String(buffer, tokenStart, tokenend - tokenStart));
      while (--tokenend >= tokenStart && this.tokenpoststripper[buffer[tokenend]]);
      if (tokenend >= tokenStart) {
         int realchars = 0;
         for (int p = tokenStart; p <= tokenend; p++) {
            if (buffer[p] > 0) {
               realchars++;
            }
         }
         if (realchars > 0 && realchars < maxtokenlength) {
            c = new char[realchars];
            for (int cnr = 0, p = tokenStart; p <= tokenend; p++) {
               if (buffer[p] > 0) {
                  c[cnr++] = (char) buffer[p];
               }
            }
            //log.info("addToken %s", new String(c));
            list.add(new String(c));
         }
      }
   }
}
