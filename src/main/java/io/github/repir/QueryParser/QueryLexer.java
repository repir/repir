// Generated from Query.g4 by ANTLR 4.2.2

package io.github.repir.QueryParser;

import java.util.ArrayList;
import io.github.repir.Repository.*;
import io.github.repir.Retriever.*;
import io.github.repir.Strategy.*;
import io.github.repir.Strategy.Operator.*;
import io.github.repir.tools.lib.Log;

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
		T__1=1, T__0=2, TERM=3, VARIABLE=4, ALTTERM=5, FUNCTION=6, CHANNEL=7, 
		WS=8, WEIGHT=9, FLOAT=10, BRACKOPEN=11, BRACKCLOSE=12, BRACEOPEN=13, BRACECLOSE=14, 
		BLOCKOPEN=15, BLOCKCLOSE=16;
	public static String[] modeNames = {
		"DEFAULT_MODE"
	};

	public static final String[] tokenNames = {
		"<INVALID>",
		"'-'", "'|'", "TERM", "VARIABLE", "ALTTERM", "FUNCTION", "CHANNEL", "WS", 
		"WEIGHT", "FLOAT", "'('", "')'", "'{'", "'}'", "'['", "']'"
	};
	public static final String[] ruleNames = {
		"T__1", "T__0", "TERM", "VARIABLE", "ALTTERM", "FUNCTION", "CHANNEL", 
		"WS", "WEIGHT", "FLOAT", "DIGIT", "CHAR", "BRACKOPEN", "BRACKCLOSE", "BRACEOPEN", 
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
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public String[] getModeNames() { return modeNames; }

	@Override
	public ATN getATN() { return _ATN; }

	public static final String _serializedATN =
		"\3\u0430\ud6d1\u8206\uad2d\u4417\uaef1\u8d80\uaadd\2\22\u0088\b\1\4\2"+
		"\t\2\4\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4"+
		"\13\t\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\4\22"+
		"\t\22\4\23\t\23\3\2\3\2\3\3\3\3\3\4\5\4-\n\4\3\4\6\4\60\n\4\r\4\16\4\61"+
		"\3\4\3\4\3\4\6\4\67\n\4\r\4\16\48\5\4;\n\4\3\5\6\5>\n\5\r\5\16\5?\3\5"+
		"\3\5\3\5\3\6\6\6F\n\6\r\6\16\6G\3\6\3\6\3\7\6\7M\n\7\r\7\16\7N\3\7\3\7"+
		"\3\b\3\b\6\bU\n\b\r\b\16\bV\3\t\6\tZ\n\t\r\t\16\t[\3\n\3\n\3\n\3\13\6"+
		"\13b\n\13\r\13\16\13c\3\13\3\13\6\13h\n\13\r\13\16\13i\3\13\3\13\5\13"+
		"n\n\13\3\13\6\13q\n\13\r\13\16\13r\5\13u\n\13\5\13w\n\13\3\f\3\f\3\r\3"+
		"\r\3\16\3\16\3\17\3\17\3\20\3\20\3\21\3\21\3\22\3\22\3\23\3\23\2\2\24"+
		"\3\3\5\4\7\5\t\6\13\7\r\b\17\t\21\n\23\13\25\f\27\2\31\2\33\r\35\16\37"+
		"\17!\20#\21%\22\3\2\6\5\2\13\f\17\17\"\"\4\2GGgg\4\2--//\7\2))\60\60\62"+
		";C\\c|\u0094\2\3\3\2\2\2\2\5\3\2\2\2\2\7\3\2\2\2\2\t\3\2\2\2\2\13\3\2"+
		"\2\2\2\r\3\2\2\2\2\17\3\2\2\2\2\21\3\2\2\2\2\23\3\2\2\2\2\25\3\2\2\2\2"+
		"\33\3\2\2\2\2\35\3\2\2\2\2\37\3\2\2\2\2!\3\2\2\2\2#\3\2\2\2\2%\3\2\2\2"+
		"\3\'\3\2\2\2\5)\3\2\2\2\7:\3\2\2\2\t=\3\2\2\2\13E\3\2\2\2\rL\3\2\2\2\17"+
		"R\3\2\2\2\21Y\3\2\2\2\23]\3\2\2\2\25a\3\2\2\2\27x\3\2\2\2\31z\3\2\2\2"+
		"\33|\3\2\2\2\35~\3\2\2\2\37\u0080\3\2\2\2!\u0082\3\2\2\2#\u0084\3\2\2"+
		"\2%\u0086\3\2\2\2\'(\7/\2\2(\4\3\2\2\2)*\7~\2\2*\6\3\2\2\2+-\7B\2\2,+"+
		"\3\2\2\2,-\3\2\2\2-/\3\2\2\2.\60\5\31\r\2/.\3\2\2\2\60\61\3\2\2\2\61/"+
		"\3\2\2\2\61\62\3\2\2\2\62;\3\2\2\2\63\64\7B\2\2\64\66\7%\2\2\65\67\5\27"+
		"\f\2\66\65\3\2\2\2\678\3\2\2\28\66\3\2\2\289\3\2\2\29;\3\2\2\2:,\3\2\2"+
		"\2:\63\3\2\2\2;\b\3\2\2\2<>\5\31\r\2=<\3\2\2\2>?\3\2\2\2?=\3\2\2\2?@\3"+
		"\2\2\2@A\3\2\2\2AB\7?\2\2BC\5\25\13\2C\n\3\2\2\2DF\5\31\r\2ED\3\2\2\2"+
		"FG\3\2\2\2GE\3\2\2\2GH\3\2\2\2HI\3\2\2\2IJ\7~\2\2J\f\3\2\2\2KM\5\31\r"+
		"\2LK\3\2\2\2MN\3\2\2\2NL\3\2\2\2NO\3\2\2\2OP\3\2\2\2PQ\7<\2\2Q\16\3\2"+
		"\2\2RT\7\u0080\2\2SU\5\31\r\2TS\3\2\2\2UV\3\2\2\2VT\3\2\2\2VW\3\2\2\2"+
		"W\20\3\2\2\2XZ\t\2\2\2YX\3\2\2\2Z[\3\2\2\2[Y\3\2\2\2[\\\3\2\2\2\\\22\3"+
		"\2\2\2]^\7%\2\2^_\5\25\13\2_\24\3\2\2\2`b\5\27\f\2a`\3\2\2\2bc\3\2\2\2"+
		"ca\3\2\2\2cd\3\2\2\2dv\3\2\2\2eg\7\60\2\2fh\5\27\f\2gf\3\2\2\2hi\3\2\2"+
		"\2ig\3\2\2\2ij\3\2\2\2jt\3\2\2\2km\t\3\2\2ln\t\4\2\2ml\3\2\2\2mn\3\2\2"+
		"\2np\3\2\2\2oq\5\27\f\2po\3\2\2\2qr\3\2\2\2rp\3\2\2\2rs\3\2\2\2su\3\2"+
		"\2\2tk\3\2\2\2tu\3\2\2\2uw\3\2\2\2ve\3\2\2\2vw\3\2\2\2w\26\3\2\2\2xy\4"+
		"\62;\2y\30\3\2\2\2z{\t\5\2\2{\32\3\2\2\2|}\7*\2\2}\34\3\2\2\2~\177\7+"+
		"\2\2\177\36\3\2\2\2\u0080\u0081\7}\2\2\u0081 \3\2\2\2\u0082\u0083\7\177"+
		"\2\2\u0083\"\3\2\2\2\u0084\u0085\7]\2\2\u0085$\3\2\2\2\u0086\u0087\7_"+
		"\2\2\u0087&\3\2\2\2\22\2,\618:?GNV[cimrtv\2";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}