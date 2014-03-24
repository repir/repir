// Generated from Query.g4 by ANTLR 4.1

package io.github.repir.QueryParser;

import java.util.ArrayList;
import io.github.repir.Repository.*;
import io.github.repir.Retriever.*;
import io.github.repir.Strategy.*;
import io.github.repir.Strategy.Operator.*;
import io.github.repir.tools.Lib.Log;

/** 
 * !!!Do not edit this code!!! 
 * It was generated with Antlr using the Query.g4 source code. 
 * <p/>
 * This parser converts a query string into a Graph that is processed for every
 * entity retrieved by a {@link RetrievalModel}. Prasing uses the following syntax rules:
 * <ul>
 * <li>termA -> Term( termA ) (i.e. termA is converted into a Term object with termA as parameter)
 * <li>termA termB" -> Term( termA ) Term( termsB ) (i.e. whitespace separates terms)
 * <li>{termA termB ...} -> FeatureProximityUnordered( termA, termB, ... ) by default maximum span = unlimited
 * <li>(termA termB ...) -> FeatureProximityOrdered( termA, termB, ...), by default maximum span = #terms
 * <li>termA-termB-... -> (termA termB ...)
 * <li>[termA termB ...] -> FeatureSynonym( Term(termA) Term(termB) )
 * <li>termA|termB|... -> same as [termA termB ...]
 * <li>ClassName:(termA termB ...) -> constructs a instance of class 'ClassName', and passes an arraylist of features contained within the brackets
 * </ul>
 * Some constructions allow optional parameters to be passed. requirements are that 
 * parameternames must be lowercase, the Feature MUST have methods set<parametername>() implemented,
 * (which is not checked, so use at you own risk), and only long (ints) and double values
 * are supported. For example:
 * <ul>
 * <li>termA#weight  weight must be a float or scientific notation, this uses setweight(value) on the feature
 * <li>termA-termB#weight sets the weight for the entire ProximityOperator, not for termB
 * <li>{termA termB ... [span=?] [cf=?] [df=?]} uses setspan(long) setcf(long) and setdf(long) on the feature
 * <li> (termA termB ... [span=?] [cf=?] [df=?]) same as {} but for ordered phrases
 * <li> [termA termB ... [cf=?] [df=?]] to set the cf/df fr proper processing of synonym occurrences
 * <li> ClassName:( termA [parama=?] [parama=?] )#weight uses setparama(?) and setparamb(?)
 * <li> termA~channel uses setchannel(channel) to set channel (e.g. title), may not be implemented 
 * </ul>
*/

import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.misc.*;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class QueryLexer extends Lexer {
	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		TERM=1, PHRASETERM=2, VARIABLE=3, ALTTERM=4, FUNCTION=5, CHANNEL=6, WS=7, 
		WEIGHT=8, FLOAT=9, BRACKOPEN=10, BRACKCLOSE=11, BRACEOPEN=12, BRACECLOSE=13, 
		BLOCKOPEN=14, BLOCKCLOSE=15;
	public static String[] modeNames = {
		"DEFAULT_MODE"
	};

	public static final String[] tokenNames = {
		"<INVALID>",
		"TERM", "PHRASETERM", "VARIABLE", "ALTTERM", "FUNCTION", "CHANNEL", "WS", 
		"WEIGHT", "FLOAT", "'('", "')'", "'{'", "'}'", "'['", "']'"
	};
	public static final String[] ruleNames = {
		"TERM", "PHRASETERM", "VARIABLE", "ALTTERM", "FUNCTION", "CHANNEL", "WS", 
		"WEIGHT", "FLOAT", "DIGIT", "CHAR", "BRACKOPEN", "BRACKCLOSE", "BRACEOPEN", 
		"BRACECLOSE", "BLOCKOPEN", "BLOCKCLOSE"
	};


	   public static Log log = new Log( QueryParser.class );
	   public GraphRoot root;
	   public int channels;
	   public int bodychannel;
	   public int titlechannel;


	public QueryLexer(CharStream input) {
		super(input);
		_interp = new LexerATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	@Override
	public String getGrammarFileName() { return "Query.g4"; }

	@Override
	public String[] getTokenNames() { return tokenNames; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String[] getModeNames() { return modeNames; }

	@Override
	public ATN getATN() { return _ATN; }

	public static final String _serializedATN =
		"\3\uacf5\uee8c\u4f5d\u8b0d\u4a45\u78bd\u1b2f\u3378\2\21}\b\1\4\2\t\2\4"+
		"\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4\13\t"+
		"\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\4\22\t\22"+
		"\3\2\6\2\'\n\2\r\2\16\2(\3\3\6\3,\n\3\r\3\16\3-\3\3\3\3\3\4\6\4\63\n\4"+
		"\r\4\16\4\64\3\4\3\4\3\4\3\5\6\5;\n\5\r\5\16\5<\3\5\3\5\3\6\6\6B\n\6\r"+
		"\6\16\6C\3\6\3\6\3\7\3\7\6\7J\n\7\r\7\16\7K\3\b\6\bO\n\b\r\b\16\bP\3\t"+
		"\3\t\3\t\3\n\6\nW\n\n\r\n\16\nX\3\n\3\n\6\n]\n\n\r\n\16\n^\3\n\3\n\5\n"+
		"c\n\n\3\n\6\nf\n\n\r\n\16\ng\5\nj\n\n\5\nl\n\n\3\13\3\13\3\f\3\f\3\r\3"+
		"\r\3\16\3\16\3\17\3\17\3\20\3\20\3\21\3\21\3\22\3\22\2\23\3\3\1\5\4\1"+
		"\7\5\1\t\6\1\13\7\1\r\b\1\17\t\1\21\n\1\23\13\1\25\2\1\27\2\1\31\f\1\33"+
		"\r\1\35\16\1\37\17\1!\20\1#\21\1\3\2\6\5\2\13\f\17\17\"\"\4\2GGgg\4\2"+
		"--//\7\2()\60\60\62;C\\c|\u0087\2\3\3\2\2\2\2\5\3\2\2\2\2\7\3\2\2\2\2"+
		"\t\3\2\2\2\2\13\3\2\2\2\2\r\3\2\2\2\2\17\3\2\2\2\2\21\3\2\2\2\2\23\3\2"+
		"\2\2\2\31\3\2\2\2\2\33\3\2\2\2\2\35\3\2\2\2\2\37\3\2\2\2\2!\3\2\2\2\2"+
		"#\3\2\2\2\3&\3\2\2\2\5+\3\2\2\2\7\62\3\2\2\2\t:\3\2\2\2\13A\3\2\2\2\r"+
		"G\3\2\2\2\17N\3\2\2\2\21R\3\2\2\2\23V\3\2\2\2\25m\3\2\2\2\27o\3\2\2\2"+
		"\31q\3\2\2\2\33s\3\2\2\2\35u\3\2\2\2\37w\3\2\2\2!y\3\2\2\2#{\3\2\2\2%"+
		"\'\5\27\f\2&%\3\2\2\2\'(\3\2\2\2(&\3\2\2\2()\3\2\2\2)\4\3\2\2\2*,\5\27"+
		"\f\2+*\3\2\2\2,-\3\2\2\2-+\3\2\2\2-.\3\2\2\2./\3\2\2\2/\60\7/\2\2\60\6"+
		"\3\2\2\2\61\63\5\27\f\2\62\61\3\2\2\2\63\64\3\2\2\2\64\62\3\2\2\2\64\65"+
		"\3\2\2\2\65\66\3\2\2\2\66\67\7?\2\2\678\5\23\n\28\b\3\2\2\29;\5\27\f\2"+
		":9\3\2\2\2;<\3\2\2\2<:\3\2\2\2<=\3\2\2\2=>\3\2\2\2>?\7~\2\2?\n\3\2\2\2"+
		"@B\5\27\f\2A@\3\2\2\2BC\3\2\2\2CA\3\2\2\2CD\3\2\2\2DE\3\2\2\2EF\7<\2\2"+
		"F\f\3\2\2\2GI\7\u0080\2\2HJ\5\27\f\2IH\3\2\2\2JK\3\2\2\2KI\3\2\2\2KL\3"+
		"\2\2\2L\16\3\2\2\2MO\t\2\2\2NM\3\2\2\2OP\3\2\2\2PN\3\2\2\2PQ\3\2\2\2Q"+
		"\20\3\2\2\2RS\7%\2\2ST\5\23\n\2T\22\3\2\2\2UW\5\25\13\2VU\3\2\2\2WX\3"+
		"\2\2\2XV\3\2\2\2XY\3\2\2\2Yk\3\2\2\2Z\\\7\60\2\2[]\5\25\13\2\\[\3\2\2"+
		"\2]^\3\2\2\2^\\\3\2\2\2^_\3\2\2\2_i\3\2\2\2`b\t\3\2\2ac\t\4\2\2ba\3\2"+
		"\2\2bc\3\2\2\2ce\3\2\2\2df\5\25\13\2ed\3\2\2\2fg\3\2\2\2ge\3\2\2\2gh\3"+
		"\2\2\2hj\3\2\2\2i`\3\2\2\2ij\3\2\2\2jl\3\2\2\2kZ\3\2\2\2kl\3\2\2\2l\24"+
		"\3\2\2\2mn\4\62;\2n\26\3\2\2\2op\t\5\2\2p\30\3\2\2\2qr\7*\2\2r\32\3\2"+
		"\2\2st\7+\2\2t\34\3\2\2\2uv\7}\2\2v\36\3\2\2\2wx\7\177\2\2x \3\2\2\2y"+
		"z\7]\2\2z\"\3\2\2\2{|\7_\2\2|$\3\2\2\2\20\2(-\64<CKPX^bgik";
	public static final ATN _ATN =
		ATNSimulator.deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}