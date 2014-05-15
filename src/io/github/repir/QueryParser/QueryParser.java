// Generated from Query.g4 by ANTLR 4.2.2

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

import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.*;
import org.antlr.v4.runtime.tree.*;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class QueryParser extends Parser {
	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		T__1=1, T__0=2, TERM=3, VARIABLE=4, ALTTERM=5, FUNCTION=6, CHANNEL=7, 
		WS=8, WEIGHT=9, FLOAT=10, BRACKOPEN=11, BRACKCLOSE=12, BRACEOPEN=13, BRACECLOSE=14, 
		BLOCKOPEN=15, BLOCKCLOSE=16;
	public static final String[] tokenNames = {
		"<INVALID>", "'-'", "'|'", "TERM", "VARIABLE", "ALTTERM", "FUNCTION", 
		"CHANNEL", "WS", "WEIGHT", "FLOAT", "'('", "')'", "'{'", "'}'", "'['", 
		"']'"
	};
	public static final int
		RULE_prog = 0, RULE_endterm = 1, RULE_term = 2, RULE_phrase = 3, RULE_syn2 = 4, 
		RULE_set = 5, RULE_phrase2 = 6, RULE_syn = 7, RULE_func = 8, RULE_weight = 9, 
		RULE_channel = 10;
	public static final String[] ruleNames = {
		"prog", "endterm", "term", "phrase", "syn2", "set", "phrase2", "syn", 
		"func", "weight", "channel"
	};

	@Override
	public String getGrammarFileName() { return "Query.g4"; }

	@Override
	public String[] getTokenNames() { return tokenNames; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public ATN getATN() { return _ATN; }


	   public static Log log = new Log( QueryParser.class );
	   public GraphRoot root;
	   public int channels;
	   public int bodychannel;
	   public int titlechannel;

	public QueryParser(TokenStream input) {
		super(input);
		_interp = new ParserATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}
	public static class ProgContext extends ParserRuleContext {
		public EndtermContext endterm;
		public EndtermContext endterm(int i) {
			return getRuleContext(EndtermContext.class,i);
		}
		public List<EndtermContext> endterm() {
			return getRuleContexts(EndtermContext.class);
		}
		public ProgContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_prog; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof QueryListener ) ((QueryListener)listener).enterProg(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof QueryListener ) ((QueryListener)listener).exitProg(this);
		}
	}

	public final ProgContext prog() throws RecognitionException {
		ProgContext _localctx = new ProgContext(_ctx, getState());
		enterRule(_localctx, 0, RULE_prog);
		 
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(25); 
			_errHandler.sync(this);
			_la = _input.LA(1);
			do {
				{
				{
				setState(22); ((ProgContext)_localctx).endterm = endterm();
				 
				          root.add( ((ProgContext)_localctx).endterm.feature );
				      
				}
				}
				setState(27); 
				_errHandler.sync(this);
				_la = _input.LA(1);
			} while ( (((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << TERM) | (1L << FUNCTION) | (1L << BRACKOPEN) | (1L << BRACEOPEN) | (1L << BLOCKOPEN))) != 0) );
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class EndtermContext extends ParserRuleContext {
		public ArrayList<Operator> feature;
		public TermContext term;
		public WeightContext weight;
		public ChannelContext channel;
		public TermContext term() {
			return getRuleContext(TermContext.class,0);
		}
		public ChannelContext channel() {
			return getRuleContext(ChannelContext.class,0);
		}
		public List<TerminalNode> WS() { return getTokens(QueryParser.WS); }
		public WeightContext weight() {
			return getRuleContext(WeightContext.class,0);
		}
		public TerminalNode WS(int i) {
			return getToken(QueryParser.WS, i);
		}
		public EndtermContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_endterm; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof QueryListener ) ((QueryListener)listener).enterEndterm(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof QueryListener ) ((QueryListener)listener).exitEndterm(this);
		}
	}

	public final EndtermContext endterm() throws RecognitionException {
		EndtermContext _localctx = new EndtermContext(_ctx, getState());
		enterRule(_localctx, 2, RULE_endterm);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(29); ((EndtermContext)_localctx).term = term();
			 ((EndtermContext)_localctx).feature =  ((EndtermContext)_localctx).term.feature;
			      
			setState(32);
			switch ( getInterpreter().adaptivePredict(_input,1,_ctx) ) {
			case 1:
				{
				setState(31); match(WS);
				}
				break;
			}
			setState(37);
			switch ( getInterpreter().adaptivePredict(_input,2,_ctx) ) {
			case 1:
				{
				setState(34); ((EndtermContext)_localctx).weight = weight();
				 
				        _localctx.feature.get( _localctx.feature.size() - 1).setweight( ((EndtermContext)_localctx).weight.value );
				      
				}
				break;
			}
			setState(40);
			switch ( getInterpreter().adaptivePredict(_input,3,_ctx) ) {
			case 1:
				{
				setState(39); match(WS);
				}
				break;
			}
			setState(45);
			switch ( getInterpreter().adaptivePredict(_input,4,_ctx) ) {
			case 1:
				{
				setState(42); ((EndtermContext)_localctx).channel = channel();
				 _localctx.feature.get( _localctx.feature.size() - 1).setchannel( ((EndtermContext)_localctx).channel.value );
				      
				}
				break;
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class TermContext extends ParserRuleContext {
		public ArrayList<Operator> feature;
		public PhraseContext phrase;
		public PhraseContext phrase() {
			return getRuleContext(PhraseContext.class,0);
		}
		public TermContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_term; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof QueryListener ) ((QueryListener)listener).enterTerm(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof QueryListener ) ((QueryListener)listener).exitTerm(this);
		}
	}

	public final TermContext term() throws RecognitionException {
		TermContext _localctx = new TermContext(_ctx, getState());
		enterRule(_localctx, 4, RULE_term);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(47); ((TermContext)_localctx).phrase = phrase();
			 ((TermContext)_localctx).feature =  ((TermContext)_localctx).phrase.feature; 
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class PhraseContext extends ParserRuleContext {
		public ArrayList<Operator> feature;
		public Syn2Context syn2;
		public TermContext term;
		public Token VARIABLE;
		public List<TermContext> term() {
			return getRuleContexts(TermContext.class);
		}
		public TerminalNode BRACKOPEN() { return getToken(QueryParser.BRACKOPEN, 0); }
		public List<TerminalNode> WS() { return getTokens(QueryParser.WS); }
		public TerminalNode BRACKCLOSE() { return getToken(QueryParser.BRACKCLOSE, 0); }
		public List<TerminalNode> VARIABLE() { return getTokens(QueryParser.VARIABLE); }
		public Syn2Context syn2() {
			return getRuleContext(Syn2Context.class,0);
		}
		public TerminalNode VARIABLE(int i) {
			return getToken(QueryParser.VARIABLE, i);
		}
		public TerminalNode WS(int i) {
			return getToken(QueryParser.WS, i);
		}
		public TermContext term(int i) {
			return getRuleContext(TermContext.class,i);
		}
		public PhraseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_phrase; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof QueryListener ) ((QueryListener)listener).enterPhrase(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof QueryListener ) ((QueryListener)listener).exitPhrase(this);
		}
	}

	public final PhraseContext phrase() throws RecognitionException {
		PhraseContext _localctx = new PhraseContext(_ctx, getState());
		enterRule(_localctx, 6, RULE_phrase);
		 ((PhraseContext)_localctx).feature =  new ArrayList<Operator>(); 
		           ArrayList<String> varl = new ArrayList<String>(); 
		           ArrayList<String> vard = new ArrayList<String>(); 
		int _la;
		try {
			setState(72);
			switch (_input.LA(1)) {
			case TERM:
			case FUNCTION:
			case BRACEOPEN:
			case BLOCKOPEN:
				enterOuterAlt(_localctx, 1);
				{
				setState(50); ((PhraseContext)_localctx).syn2 = syn2();
				 ((PhraseContext)_localctx).feature =  ((PhraseContext)_localctx).syn2.feature; 
				}
				break;
			case BRACKOPEN:
				enterOuterAlt(_localctx, 2);
				{
				{
				setState(53); match(BRACKOPEN);
				{
				setState(55);
				_la = _input.LA(1);
				if (_la==WS) {
					{
					setState(54); match(WS);
					}
				}

				}
				setState(67); 
				_errHandler.sync(this);
				_la = _input.LA(1);
				do {
					{
					{
					setState(62);
					switch (_input.LA(1)) {
					case TERM:
					case FUNCTION:
					case BRACKOPEN:
					case BRACEOPEN:
					case BLOCKOPEN:
						{
						setState(57); ((PhraseContext)_localctx).term = term();
						 _localctx.feature.addAll( ((PhraseContext)_localctx).term.feature ); 
						}
						break;
					case VARIABLE:
						{
						setState(60); ((PhraseContext)_localctx).VARIABLE = match(VARIABLE);
						 if ((((PhraseContext)_localctx).VARIABLE!=null?((PhraseContext)_localctx).VARIABLE.getText():null).indexOf(".") >= 0)
						                        vard.add( (((PhraseContext)_localctx).VARIABLE!=null?((PhraseContext)_localctx).VARIABLE.getText():null) );
						                     else 
						                        varl.add( (((PhraseContext)_localctx).VARIABLE!=null?((PhraseContext)_localctx).VARIABLE.getText():null) ); 
						}
						break;
					default:
						throw new NoViableAltException(this);
					}
					setState(65);
					_la = _input.LA(1);
					if (_la==WS) {
						{
						setState(64); match(WS);
						}
					}

					}
					}
					setState(69); 
					_errHandler.sync(this);
					_la = _input.LA(1);
				} while ( (((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << TERM) | (1L << VARIABLE) | (1L << FUNCTION) | (1L << BRACKOPEN) | (1L << BRACEOPEN) | (1L << BLOCKOPEN))) != 0) );
				setState(71); match(BRACKCLOSE);
				}
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
			 if (_localctx.feature.size() > 1) {
			               ProximityOperatorOrdered o = new ProximityOperatorOrdered( root, _localctx.feature );
			               o.setMinimalSpan();
			               for (String v : varl)
			                  o.setGenericL( v );
			               for (String v : vard)
			                  o.setGenericD( v );
			               ((PhraseContext)_localctx).feature =  new ArrayList<Operator>();
			               _localctx.feature.add(o);
			            }
			          
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Syn2Context extends ParserRuleContext {
		public ArrayList<Operator> feature;
		public SetContext set;
		public TermContext term;
		public Token VARIABLE;
		public List<TermContext> term() {
			return getRuleContexts(TermContext.class);
		}
		public List<TerminalNode> WS() { return getTokens(QueryParser.WS); }
		public List<TerminalNode> VARIABLE() { return getTokens(QueryParser.VARIABLE); }
		public TerminalNode BLOCKCLOSE() { return getToken(QueryParser.BLOCKCLOSE, 0); }
		public TerminalNode VARIABLE(int i) {
			return getToken(QueryParser.VARIABLE, i);
		}
		public TerminalNode WS(int i) {
			return getToken(QueryParser.WS, i);
		}
		public SetContext set() {
			return getRuleContext(SetContext.class,0);
		}
		public TermContext term(int i) {
			return getRuleContext(TermContext.class,i);
		}
		public TerminalNode BLOCKOPEN() { return getToken(QueryParser.BLOCKOPEN, 0); }
		public Syn2Context(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_syn2; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof QueryListener ) ((QueryListener)listener).enterSyn2(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof QueryListener ) ((QueryListener)listener).exitSyn2(this);
		}
	}

	public final Syn2Context syn2() throws RecognitionException {
		Syn2Context _localctx = new Syn2Context(_ctx, getState());
		enterRule(_localctx, 8, RULE_syn2);
		 ((Syn2Context)_localctx).feature =  new ArrayList<Operator>();
		           ArrayList<String> varl = new ArrayList<String>(); 
		           ArrayList<String> vard = new ArrayList<String>();  
		int _la;
		try {
			setState(96);
			switch (_input.LA(1)) {
			case TERM:
			case FUNCTION:
			case BRACEOPEN:
				enterOuterAlt(_localctx, 1);
				{
				setState(74); ((Syn2Context)_localctx).set = set();
				 ((Syn2Context)_localctx).feature =  ((Syn2Context)_localctx).set.feature; 
				}
				break;
			case BLOCKOPEN:
				enterOuterAlt(_localctx, 2);
				{
				setState(77); match(BLOCKOPEN);
				setState(79);
				_la = _input.LA(1);
				if (_la==WS) {
					{
					setState(78); match(WS);
					}
				}

				setState(91); 
				_errHandler.sync(this);
				_la = _input.LA(1);
				do {
					{
					{
					setState(86);
					switch (_input.LA(1)) {
					case TERM:
					case FUNCTION:
					case BRACKOPEN:
					case BRACEOPEN:
					case BLOCKOPEN:
						{
						setState(81); ((Syn2Context)_localctx).term = term();
						 _localctx.feature.addAll( ((Syn2Context)_localctx).term.feature ); 
						}
						break;
					case VARIABLE:
						{
						setState(84); ((Syn2Context)_localctx).VARIABLE = match(VARIABLE);
						 if ((((Syn2Context)_localctx).VARIABLE!=null?((Syn2Context)_localctx).VARIABLE.getText():null).indexOf(".") >= 0)
						                        vard.add( (((Syn2Context)_localctx).VARIABLE!=null?((Syn2Context)_localctx).VARIABLE.getText():null) );
						                     else 
						                        varl.add( (((Syn2Context)_localctx).VARIABLE!=null?((Syn2Context)_localctx).VARIABLE.getText():null) ); 
						}
						break;
					default:
						throw new NoViableAltException(this);
					}
					setState(89);
					_la = _input.LA(1);
					if (_la==WS) {
						{
						setState(88); match(WS);
						}
					}

					}
					}
					setState(93); 
					_errHandler.sync(this);
					_la = _input.LA(1);
				} while ( (((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << TERM) | (1L << VARIABLE) | (1L << FUNCTION) | (1L << BRACKOPEN) | (1L << BRACEOPEN) | (1L << BLOCKOPEN))) != 0) );
				setState(95); match(BLOCKCLOSE);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
			 if ( _localctx.feature.size() > 1) {
			               Operator o = new SynonymOperator( root, _localctx.feature );
			               for (String v : varl)
			                  o.setGenericL( v );
			               for (String v : vard)
			                  o.setGenericD( v );
			               ((Syn2Context)_localctx).feature =  new ArrayList<Operator>();
			               _localctx.feature.add(o);
			            }
			          
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class SetContext extends ParserRuleContext {
		public ArrayList<Operator> feature;
		public Phrase2Context phrase2;
		public TermContext term;
		public Token VARIABLE;
		public List<TermContext> term() {
			return getRuleContexts(TermContext.class);
		}
		public List<TerminalNode> WS() { return getTokens(QueryParser.WS); }
		public List<TerminalNode> VARIABLE() { return getTokens(QueryParser.VARIABLE); }
		public TerminalNode BRACEOPEN() { return getToken(QueryParser.BRACEOPEN, 0); }
		public Phrase2Context phrase2() {
			return getRuleContext(Phrase2Context.class,0);
		}
		public TerminalNode VARIABLE(int i) {
			return getToken(QueryParser.VARIABLE, i);
		}
		public TerminalNode BRACECLOSE() { return getToken(QueryParser.BRACECLOSE, 0); }
		public TerminalNode WS(int i) {
			return getToken(QueryParser.WS, i);
		}
		public TermContext term(int i) {
			return getRuleContext(TermContext.class,i);
		}
		public SetContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_set; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof QueryListener ) ((QueryListener)listener).enterSet(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof QueryListener ) ((QueryListener)listener).exitSet(this);
		}
	}

	public final SetContext set() throws RecognitionException {
		SetContext _localctx = new SetContext(_ctx, getState());
		enterRule(_localctx, 10, RULE_set);
		 ((SetContext)_localctx).feature =  new ArrayList<Operator>(); 
		           ArrayList<String> varl = new ArrayList<String>(); 
		           ArrayList<String> vard = new ArrayList<String>(); 
		int _la;
		try {
			setState(120);
			switch (_input.LA(1)) {
			case TERM:
			case FUNCTION:
				enterOuterAlt(_localctx, 1);
				{
				setState(98); ((SetContext)_localctx).phrase2 = phrase2();
				 ((SetContext)_localctx).feature =  ((SetContext)_localctx).phrase2.feature; 
				}
				break;
			case BRACEOPEN:
				enterOuterAlt(_localctx, 2);
				{
				setState(101); match(BRACEOPEN);
				{
				setState(103);
				_la = _input.LA(1);
				if (_la==WS) {
					{
					setState(102); match(WS);
					}
				}

				}
				setState(115); 
				_errHandler.sync(this);
				_la = _input.LA(1);
				do {
					{
					{
					setState(110);
					switch (_input.LA(1)) {
					case TERM:
					case FUNCTION:
					case BRACKOPEN:
					case BRACEOPEN:
					case BLOCKOPEN:
						{
						setState(105); ((SetContext)_localctx).term = term();
						 _localctx.feature.addAll( ((SetContext)_localctx).term.feature ); 
						}
						break;
					case VARIABLE:
						{
						setState(108); ((SetContext)_localctx).VARIABLE = match(VARIABLE);
						 if ((((SetContext)_localctx).VARIABLE!=null?((SetContext)_localctx).VARIABLE.getText():null).indexOf(".") >= 0)
						                        vard.add( (((SetContext)_localctx).VARIABLE!=null?((SetContext)_localctx).VARIABLE.getText():null) );
						                     else 
						                        varl.add( (((SetContext)_localctx).VARIABLE!=null?((SetContext)_localctx).VARIABLE.getText():null) ); 
						}
						break;
					default:
						throw new NoViableAltException(this);
					}
					setState(113);
					_la = _input.LA(1);
					if (_la==WS) {
						{
						setState(112); match(WS);
						}
					}

					}
					}
					setState(117); 
					_errHandler.sync(this);
					_la = _input.LA(1);
				} while ( (((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << TERM) | (1L << VARIABLE) | (1L << FUNCTION) | (1L << BRACKOPEN) | (1L << BRACEOPEN) | (1L << BLOCKOPEN))) != 0) );
				setState(119); match(BRACECLOSE);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
			 if (_localctx.feature.size() > 1) {
			               Operator o = new ProximityOperatorUnordered( root, _localctx.feature );
			               for (String v : varl)
			                  o.setGenericL( v );
			               for (String v : vard)
			                  o.setGenericD( v );
			               ((SetContext)_localctx).feature =  new ArrayList<Operator>();
			               _localctx.feature.add( o );
			            }
			          
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Phrase2Context extends ParserRuleContext {
		public ArrayList<Operator> feature;
		public SynContext syn;
		public EndtermContext endterm;
		public EndtermContext endterm(int i) {
			return getRuleContext(EndtermContext.class,i);
		}
		public List<EndtermContext> endterm() {
			return getRuleContexts(EndtermContext.class);
		}
		public SynContext syn() {
			return getRuleContext(SynContext.class,0);
		}
		public Phrase2Context(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_phrase2; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof QueryListener ) ((QueryListener)listener).enterPhrase2(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof QueryListener ) ((QueryListener)listener).exitPhrase2(this);
		}
	}

	public final Phrase2Context phrase2() throws RecognitionException {
		Phrase2Context _localctx = new Phrase2Context(_ctx, getState());
		enterRule(_localctx, 12, RULE_phrase2);
		 ((Phrase2Context)_localctx).feature =  new ArrayList<Operator>();  
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(122); ((Phrase2Context)_localctx).syn = syn();
			 _localctx.feature.addAll( ((Phrase2Context)_localctx).syn.feature ); 
			setState(130);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,20,_ctx);
			while ( _alt!=2 && _alt!=ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(124); match(1);
					setState(125); ((Phrase2Context)_localctx).endterm = endterm();
					 _localctx.feature.addAll( ((Phrase2Context)_localctx).endterm.feature ); 
					}
					} 
				}
				setState(132);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,20,_ctx);
			}
			}
			 if (_localctx.feature.size() > 1) {
			               ProximityOperatorOrdered o = new ProximityOperatorOrdered( root, _localctx.feature );
			               o.setMinimalSpan();
			               ((Phrase2Context)_localctx).feature =  new ArrayList<Operator>();
			               _localctx.feature.add(o);
			            }
			         
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class SynContext extends ParserRuleContext {
		public ArrayList<Operator> feature;
		public FuncContext func;
		public TermContext term;
		public List<TermContext> term() {
			return getRuleContexts(TermContext.class);
		}
		public List<TerminalNode> WS() { return getTokens(QueryParser.WS); }
		public FuncContext func() {
			return getRuleContext(FuncContext.class,0);
		}
		public TerminalNode WS(int i) {
			return getToken(QueryParser.WS, i);
		}
		public TermContext term(int i) {
			return getRuleContext(TermContext.class,i);
		}
		public SynContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_syn; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof QueryListener ) ((QueryListener)listener).enterSyn(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof QueryListener ) ((QueryListener)listener).exitSyn(this);
		}
	}

	public final SynContext syn() throws RecognitionException {
		SynContext _localctx = new SynContext(_ctx, getState());
		enterRule(_localctx, 14, RULE_syn);
		 ((SynContext)_localctx).feature =  new ArrayList<Operator>(); 
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(133); ((SynContext)_localctx).func = func();
			 _localctx.feature.add( ((SynContext)_localctx).func.feature ); 
			setState(147);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,23,_ctx);
			while ( _alt!=2 && _alt!=ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(136);
					_la = _input.LA(1);
					if (_la==WS) {
						{
						setState(135); match(WS);
						}
					}

					setState(138); match(2);
					setState(140);
					_la = _input.LA(1);
					if (_la==WS) {
						{
						setState(139); match(WS);
						}
					}

					setState(142); ((SynContext)_localctx).term = term();
					 _localctx.feature.addAll( ((SynContext)_localctx).term.feature ); 
					}
					} 
				}
				setState(149);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,23,_ctx);
			}
			}
			 if (_localctx.feature.size() > 1) {
			               Operator o = new SynonymOperator( root, _localctx.feature ); 
			               ((SynContext)_localctx).feature =  new ArrayList<Operator>();
			               _localctx.feature.add(o);
			            }
			          
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class FuncContext extends ParserRuleContext {
		public Operator feature;
		public Token TERM;
		public Token FUNCTION;
		public TermContext term;
		public Token VARIABLE;
		public List<TermContext> term() {
			return getRuleContexts(TermContext.class);
		}
		public TerminalNode BRACKOPEN() { return getToken(QueryParser.BRACKOPEN, 0); }
		public List<TerminalNode> WS() { return getTokens(QueryParser.WS); }
		public TerminalNode BRACKCLOSE() { return getToken(QueryParser.BRACKCLOSE, 0); }
		public List<TerminalNode> VARIABLE() { return getTokens(QueryParser.VARIABLE); }
		public TerminalNode FUNCTION() { return getToken(QueryParser.FUNCTION, 0); }
		public TerminalNode VARIABLE(int i) {
			return getToken(QueryParser.VARIABLE, i);
		}
		public TerminalNode WS(int i) {
			return getToken(QueryParser.WS, i);
		}
		public TerminalNode TERM() { return getToken(QueryParser.TERM, 0); }
		public TermContext term(int i) {
			return getRuleContext(TermContext.class,i);
		}
		public FuncContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_func; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof QueryListener ) ((QueryListener)listener).enterFunc(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof QueryListener ) ((QueryListener)listener).exitFunc(this);
		}
	}

	public final FuncContext func() throws RecognitionException {
		FuncContext _localctx = new FuncContext(_ctx, getState());
		enterRule(_localctx, 16, RULE_func);
		int _la;
		try {
			setState(178);
			switch (_input.LA(1)) {
			case TERM:
				enterOuterAlt(_localctx, 1);
				{
				setState(150); ((FuncContext)_localctx).TERM = match(TERM);
				 ((FuncContext)_localctx).feature =  root.getTerm( (((FuncContext)_localctx).TERM!=null?((FuncContext)_localctx).TERM.getText():null) ); 
				}
				break;
			case FUNCTION:
				enterOuterAlt(_localctx, 2);
				{
				setState(152); ((FuncContext)_localctx).FUNCTION = match(FUNCTION);
				 ArrayList<String> varl = new ArrayList<String>(); 
				                ArrayList<String> vard = new ArrayList<String>(); 
				                String functionclass = (((FuncContext)_localctx).FUNCTION!=null?((FuncContext)_localctx).FUNCTION.getText():null); 
				                functionclass = functionclass.substring(0, functionclass.length()-1);
				              
				setState(154); match(BRACKOPEN);
				setState(156);
				_la = _input.LA(1);
				if (_la==WS) {
					{
					setState(155); match(WS);
					}
				}

				 ArrayList<Operator> list = new ArrayList<Operator>(); 
				setState(172); 
				_errHandler.sync(this);
				_la = _input.LA(1);
				do {
					{
					{
					setState(164);
					switch (_input.LA(1)) {
					case TERM:
					case FUNCTION:
					case BRACKOPEN:
					case BRACEOPEN:
					case BLOCKOPEN:
						{
						setState(159); ((FuncContext)_localctx).term = term();
						 list.addAll( ((FuncContext)_localctx).term.feature ); 
						}
						break;
					case VARIABLE:
						{
						setState(162); ((FuncContext)_localctx).VARIABLE = match(VARIABLE);
						 if ((((FuncContext)_localctx).VARIABLE!=null?((FuncContext)_localctx).VARIABLE.getText():null).indexOf(".") >= 0)
						                        vard.add( (((FuncContext)_localctx).VARIABLE!=null?((FuncContext)_localctx).VARIABLE.getText():null) );
						                     else 
						                        varl.add( (((FuncContext)_localctx).VARIABLE!=null?((FuncContext)_localctx).VARIABLE.getText():null) ); 
						}
						break;
					default:
						throw new NoViableAltException(this);
					}
					setState(169);
					_errHandler.sync(this);
					_la = _input.LA(1);
					while (_la==WS) {
						{
						{
						setState(166); match(WS);
						}
						}
						setState(171);
						_errHandler.sync(this);
						_la = _input.LA(1);
					}
					}
					}
					setState(174); 
					_errHandler.sync(this);
					_la = _input.LA(1);
				} while ( (((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << TERM) | (1L << VARIABLE) | (1L << FUNCTION) | (1L << BRACKOPEN) | (1L << BRACEOPEN) | (1L << BLOCKOPEN))) != 0) );
				setState(176); match(BRACKCLOSE);
				 
				               ((FuncContext)_localctx).feature =  root.construct( functionclass, list );
				               for (String v : varl)
				                  _localctx.feature.setGenericL( v );
				               for (String v : vard)
				                  _localctx.feature.setGenericD( v );
				     
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class WeightContext extends ParserRuleContext {
		public Double value;
		public Token WEIGHT;
		public TerminalNode WEIGHT() { return getToken(QueryParser.WEIGHT, 0); }
		public WeightContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_weight; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof QueryListener ) ((QueryListener)listener).enterWeight(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof QueryListener ) ((QueryListener)listener).exitWeight(this);
		}
	}

	public final WeightContext weight() throws RecognitionException {
		WeightContext _localctx = new WeightContext(_ctx, getState());
		enterRule(_localctx, 18, RULE_weight);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(180); ((WeightContext)_localctx).WEIGHT = match(WEIGHT);
			 ((WeightContext)_localctx).value =  Double.valueOf( (((WeightContext)_localctx).WEIGHT!=null?((WeightContext)_localctx).WEIGHT.getText():null).substring(1) ); 
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ChannelContext extends ParserRuleContext {
		public String value;
		public Token CHANNEL;
		public TerminalNode CHANNEL() { return getToken(QueryParser.CHANNEL, 0); }
		public ChannelContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_channel; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof QueryListener ) ((QueryListener)listener).enterChannel(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof QueryListener ) ((QueryListener)listener).exitChannel(this);
		}
	}

	public final ChannelContext channel() throws RecognitionException {
		ChannelContext _localctx = new ChannelContext(_ctx, getState());
		enterRule(_localctx, 20, RULE_channel);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(183); ((ChannelContext)_localctx).CHANNEL = match(CHANNEL);
			 ((ChannelContext)_localctx).value =  (((ChannelContext)_localctx).CHANNEL!=null?((ChannelContext)_localctx).CHANNEL.getText():null).substring(1); 
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static final String _serializedATN =
		"\3\u0430\ud6d1\u8206\uad2d\u4417\uaef1\u8d80\uaadd\3\22\u00bd\4\2\t\2"+
		"\4\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4\13"+
		"\t\13\4\f\t\f\3\2\3\2\3\2\6\2\34\n\2\r\2\16\2\35\3\3\3\3\3\3\5\3#\n\3"+
		"\3\3\3\3\3\3\5\3(\n\3\3\3\5\3+\n\3\3\3\3\3\3\3\5\3\60\n\3\3\4\3\4\3\4"+
		"\3\5\3\5\3\5\3\5\3\5\5\5:\n\5\3\5\3\5\3\5\3\5\3\5\5\5A\n\5\3\5\5\5D\n"+
		"\5\6\5F\n\5\r\5\16\5G\3\5\5\5K\n\5\3\6\3\6\3\6\3\6\3\6\5\6R\n\6\3\6\3"+
		"\6\3\6\3\6\3\6\5\6Y\n\6\3\6\5\6\\\n\6\6\6^\n\6\r\6\16\6_\3\6\5\6c\n\6"+
		"\3\7\3\7\3\7\3\7\3\7\5\7j\n\7\3\7\3\7\3\7\3\7\3\7\5\7q\n\7\3\7\5\7t\n"+
		"\7\6\7v\n\7\r\7\16\7w\3\7\5\7{\n\7\3\b\3\b\3\b\3\b\3\b\3\b\7\b\u0083\n"+
		"\b\f\b\16\b\u0086\13\b\3\t\3\t\3\t\5\t\u008b\n\t\3\t\3\t\5\t\u008f\n\t"+
		"\3\t\3\t\3\t\7\t\u0094\n\t\f\t\16\t\u0097\13\t\3\n\3\n\3\n\3\n\3\n\3\n"+
		"\5\n\u009f\n\n\3\n\3\n\3\n\3\n\3\n\3\n\5\n\u00a7\n\n\3\n\7\n\u00aa\n\n"+
		"\f\n\16\n\u00ad\13\n\6\n\u00af\n\n\r\n\16\n\u00b0\3\n\3\n\5\n\u00b5\n"+
		"\n\3\13\3\13\3\13\3\f\3\f\3\f\3\f\2\2\r\2\4\6\b\n\f\16\20\22\24\26\2\2"+
		"\u00ce\2\33\3\2\2\2\4\37\3\2\2\2\6\61\3\2\2\2\bJ\3\2\2\2\nb\3\2\2\2\f"+
		"z\3\2\2\2\16|\3\2\2\2\20\u0087\3\2\2\2\22\u00b4\3\2\2\2\24\u00b6\3\2\2"+
		"\2\26\u00b9\3\2\2\2\30\31\5\4\3\2\31\32\b\2\1\2\32\34\3\2\2\2\33\30\3"+
		"\2\2\2\34\35\3\2\2\2\35\33\3\2\2\2\35\36\3\2\2\2\36\3\3\2\2\2\37 \5\6"+
		"\4\2 \"\b\3\1\2!#\7\n\2\2\"!\3\2\2\2\"#\3\2\2\2#\'\3\2\2\2$%\5\24\13\2"+
		"%&\b\3\1\2&(\3\2\2\2\'$\3\2\2\2\'(\3\2\2\2(*\3\2\2\2)+\7\n\2\2*)\3\2\2"+
		"\2*+\3\2\2\2+/\3\2\2\2,-\5\26\f\2-.\b\3\1\2.\60\3\2\2\2/,\3\2\2\2/\60"+
		"\3\2\2\2\60\5\3\2\2\2\61\62\5\b\5\2\62\63\b\4\1\2\63\7\3\2\2\2\64\65\5"+
		"\n\6\2\65\66\b\5\1\2\66K\3\2\2\2\679\7\r\2\28:\7\n\2\298\3\2\2\29:\3\2"+
		"\2\2:E\3\2\2\2;<\5\6\4\2<=\b\5\1\2=A\3\2\2\2>?\7\6\2\2?A\b\5\1\2@;\3\2"+
		"\2\2@>\3\2\2\2AC\3\2\2\2BD\7\n\2\2CB\3\2\2\2CD\3\2\2\2DF\3\2\2\2E@\3\2"+
		"\2\2FG\3\2\2\2GE\3\2\2\2GH\3\2\2\2HI\3\2\2\2IK\7\16\2\2J\64\3\2\2\2J\67"+
		"\3\2\2\2K\t\3\2\2\2LM\5\f\7\2MN\b\6\1\2Nc\3\2\2\2OQ\7\21\2\2PR\7\n\2\2"+
		"QP\3\2\2\2QR\3\2\2\2R]\3\2\2\2ST\5\6\4\2TU\b\6\1\2UY\3\2\2\2VW\7\6\2\2"+
		"WY\b\6\1\2XS\3\2\2\2XV\3\2\2\2Y[\3\2\2\2Z\\\7\n\2\2[Z\3\2\2\2[\\\3\2\2"+
		"\2\\^\3\2\2\2]X\3\2\2\2^_\3\2\2\2_]\3\2\2\2_`\3\2\2\2`a\3\2\2\2ac\7\22"+
		"\2\2bL\3\2\2\2bO\3\2\2\2c\13\3\2\2\2de\5\16\b\2ef\b\7\1\2f{\3\2\2\2gi"+
		"\7\17\2\2hj\7\n\2\2ih\3\2\2\2ij\3\2\2\2ju\3\2\2\2kl\5\6\4\2lm\b\7\1\2"+
		"mq\3\2\2\2no\7\6\2\2oq\b\7\1\2pk\3\2\2\2pn\3\2\2\2qs\3\2\2\2rt\7\n\2\2"+
		"sr\3\2\2\2st\3\2\2\2tv\3\2\2\2up\3\2\2\2vw\3\2\2\2wu\3\2\2\2wx\3\2\2\2"+
		"xy\3\2\2\2y{\7\20\2\2zd\3\2\2\2zg\3\2\2\2{\r\3\2\2\2|}\5\20\t\2}\u0084"+
		"\b\b\1\2~\177\7\3\2\2\177\u0080\5\4\3\2\u0080\u0081\b\b\1\2\u0081\u0083"+
		"\3\2\2\2\u0082~\3\2\2\2\u0083\u0086\3\2\2\2\u0084\u0082\3\2\2\2\u0084"+
		"\u0085\3\2\2\2\u0085\17\3\2\2\2\u0086\u0084\3\2\2\2\u0087\u0088\5\22\n"+
		"\2\u0088\u0095\b\t\1\2\u0089\u008b\7\n\2\2\u008a\u0089\3\2\2\2\u008a\u008b"+
		"\3\2\2\2\u008b\u008c\3\2\2\2\u008c\u008e\7\4\2\2\u008d\u008f\7\n\2\2\u008e"+
		"\u008d\3\2\2\2\u008e\u008f\3\2\2\2\u008f\u0090\3\2\2\2\u0090\u0091\5\6"+
		"\4\2\u0091\u0092\b\t\1\2\u0092\u0094\3\2\2\2\u0093\u008a\3\2\2\2\u0094"+
		"\u0097\3\2\2\2\u0095\u0093\3\2\2\2\u0095\u0096\3\2\2\2\u0096\21\3\2\2"+
		"\2\u0097\u0095\3\2\2\2\u0098\u0099\7\5\2\2\u0099\u00b5\b\n\1\2\u009a\u009b"+
		"\7\b\2\2\u009b\u009c\b\n\1\2\u009c\u009e\7\r\2\2\u009d\u009f\7\n\2\2\u009e"+
		"\u009d\3\2\2\2\u009e\u009f\3\2\2\2\u009f\u00a0\3\2\2\2\u00a0\u00ae\b\n"+
		"\1\2\u00a1\u00a2\5\6\4\2\u00a2\u00a3\b\n\1\2\u00a3\u00a7\3\2\2\2\u00a4"+
		"\u00a5\7\6\2\2\u00a5\u00a7\b\n\1\2\u00a6\u00a1\3\2\2\2\u00a6\u00a4\3\2"+
		"\2\2\u00a7\u00ab\3\2\2\2\u00a8\u00aa\7\n\2\2\u00a9\u00a8\3\2\2\2\u00aa"+
		"\u00ad\3\2\2\2\u00ab\u00a9\3\2\2\2\u00ab\u00ac\3\2\2\2\u00ac\u00af\3\2"+
		"\2\2\u00ad\u00ab\3\2\2\2\u00ae\u00a6\3\2\2\2\u00af\u00b0\3\2\2\2\u00b0"+
		"\u00ae\3\2\2\2\u00b0\u00b1\3\2\2\2\u00b1\u00b2\3\2\2\2\u00b2\u00b3\7\16"+
		"\2\2\u00b3\u00b5\b\n\1\2\u00b4\u0098\3\2\2\2\u00b4\u009a\3\2\2\2\u00b5"+
		"\23\3\2\2\2\u00b6\u00b7\7\13\2\2\u00b7\u00b8\b\13\1\2\u00b8\25\3\2\2\2"+
		"\u00b9\u00ba\7\t\2\2\u00ba\u00bb\b\f\1\2\u00bb\27\3\2\2\2\37\35\"\'*/"+
		"9@CGJQX[_bipswz\u0084\u008a\u008e\u0095\u009e\u00a6\u00ab\u00b0\u00b4";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}