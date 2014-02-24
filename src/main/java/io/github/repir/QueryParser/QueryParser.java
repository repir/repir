// Generated from Query.g4 by ANTLR 4.1

    package io.github.repir.QueryParser;
    import java.util.ArrayList;
    import io.github.repir.Repository.*;
    import io.github.repir.Retriever.*;
    import io.github.repir.Strategy.*;
    import io.github.repir.tools.Lib.Log;

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
		TERM=1, PHRASETERM=2, VARIABLE=3, ALTTERM=4, FUNCTION=5, CHANNEL=6, WS=7, 
		WEIGHT=8, FLOAT=9, BRACKOPEN=10, BRACKCLOSE=11, BRACEOPEN=12, BRACECLOSE=13, 
		BLOCKOPEN=14, BLOCKCLOSE=15;
	public static final String[] tokenNames = {
		"<INVALID>", "TERM", "PHRASETERM", "VARIABLE", "ALTTERM", "FUNCTION", 
		"CHANNEL", "WS", "WEIGHT", "FLOAT", "'('", "')'", "'{'", "'}'", "'['", 
		"']'"
	};
	public static final int
		RULE_prog = 0, RULE_endterm = 1, RULE_phrase = 2, RULE_phrase2 = 3, RULE_syn = 4, 
		RULE_syn2 = 5, RULE_set = 6, RULE_function = 7, RULE_term = 8, RULE_weight = 9, 
		RULE_channel = 10;
	public static final String[] ruleNames = {
		"prog", "endterm", "phrase", "phrase2", "syn", "syn2", "set", "function", 
		"term", "weight", "channel"
	};

	@Override
	public String getGrammarFileName() { return "Query.g4"; }

	@Override
	public String[] getTokenNames() { return tokenNames; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

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
			} while ( (((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << TERM) | (1L << PHRASETERM) | (1L << ALTTERM) | (1L << FUNCTION) | (1L << BRACKOPEN) | (1L << BRACEOPEN) | (1L << BLOCKOPEN))) != 0) );
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
		public GraphNode feature;
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
		int _la;
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
			_la = _input.LA(1);
			if (_la==WEIGHT) {
				{
				setState(34); ((EndtermContext)_localctx).weight = weight();
				 _localctx.feature.setweight( ((EndtermContext)_localctx).weight.value );
				      
				}
			}

			setState(40);
			_la = _input.LA(1);
			if (_la==WS) {
				{
				setState(39); match(WS);
				}
			}

			setState(45);
			_la = _input.LA(1);
			if (_la==CHANNEL) {
				{
				setState(42); ((EndtermContext)_localctx).channel = channel();
				 _localctx.feature.setchannel( ((EndtermContext)_localctx).channel.value );
				        log.info("aap");
				      
				}
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

	public static class PhraseContext extends ParserRuleContext {
		public GraphNode feature;
		public TermContext term;
		public Token VARIABLE;
		public List<TermContext> term() {
			return getRuleContexts(TermContext.class);
		}
		public TerminalNode BRACKOPEN() { return getToken(QueryParser.BRACKOPEN, 0); }
		public List<TerminalNode> WS() { return getTokens(QueryParser.WS); }
		public TerminalNode BRACKCLOSE() { return getToken(QueryParser.BRACKCLOSE, 0); }
		public List<TerminalNode> VARIABLE() { return getTokens(QueryParser.VARIABLE); }
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
		enterRule(_localctx, 4, RULE_phrase);
		 ((PhraseContext)_localctx).feature =  new FeatureProximityOrdered( root ); 
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			{
			setState(47); match(BRACKOPEN);
			{
			setState(49);
			_la = _input.LA(1);
			if (_la==WS) {
				{
				setState(48); match(WS);
				}
			}

			}
			setState(61); 
			_errHandler.sync(this);
			_la = _input.LA(1);
			do {
				{
				{
				setState(56);
				switch (_input.LA(1)) {
				case TERM:
				case PHRASETERM:
				case ALTTERM:
				case FUNCTION:
				case BRACKOPEN:
				case BRACEOPEN:
				case BLOCKOPEN:
					{
					setState(51); ((PhraseContext)_localctx).term = term();
					 ((FeatureProximity)_localctx.feature).add( ((PhraseContext)_localctx).term.feature ); 
					}
					break;
				case VARIABLE:
					{
					setState(54); ((PhraseContext)_localctx).VARIABLE = match(VARIABLE);
					 if ((((PhraseContext)_localctx).VARIABLE!=null?((PhraseContext)_localctx).VARIABLE.getText():null).indexOf(".") >= 0)
					                        _localctx.feature.setGenericD( (((PhraseContext)_localctx).VARIABLE!=null?((PhraseContext)_localctx).VARIABLE.getText():null) );
					                     else 
					                        _localctx.feature.setGenericL( (((PhraseContext)_localctx).VARIABLE!=null?((PhraseContext)_localctx).VARIABLE.getText():null) ); 
					}
					break;
				default:
					throw new NoViableAltException(this);
				}
				setState(59);
				_la = _input.LA(1);
				if (_la==WS) {
					{
					setState(58); match(WS);
					}
				}

				}
				}
				setState(63); 
				_errHandler.sync(this);
				_la = _input.LA(1);
			} while ( (((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << TERM) | (1L << PHRASETERM) | (1L << VARIABLE) | (1L << ALTTERM) | (1L << FUNCTION) | (1L << BRACKOPEN) | (1L << BRACEOPEN) | (1L << BLOCKOPEN))) != 0) );
			setState(65); match(BRACKCLOSE);
			}
			}
			 if (_localctx.feature.containedfeatures.size() == 1)
			                ((PhraseContext)_localctx).feature =  _localctx.feature.containedfeatures.get(0);
			          
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
		public FeatureProximity feature;
		public Token PHRASETERM;
		public Token TERM;
		public TerminalNode PHRASETERM(int i) {
			return getToken(QueryParser.PHRASETERM, i);
		}
		public List<TerminalNode> PHRASETERM() { return getTokens(QueryParser.PHRASETERM); }
		public TerminalNode TERM() { return getToken(QueryParser.TERM, 0); }
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
		enterRule(_localctx, 6, RULE_phrase2);
		 ((Phrase2Context)_localctx).feature =  new FeatureProximityOrdered( root ); 
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(69); 
			_errHandler.sync(this);
			_la = _input.LA(1);
			do {
				{
				{
				setState(67); ((Phrase2Context)_localctx).PHRASETERM = match(PHRASETERM);
				 _localctx.feature.add( root.getTerm( (((Phrase2Context)_localctx).PHRASETERM!=null?((Phrase2Context)_localctx).PHRASETERM.getText():null).substring(0, (((Phrase2Context)_localctx).PHRASETERM!=null?((Phrase2Context)_localctx).PHRASETERM.getText():null).length()-1 )));  
				}
				}
				setState(71); 
				_errHandler.sync(this);
				_la = _input.LA(1);
			} while ( _la==PHRASETERM );
			setState(73); ((Phrase2Context)_localctx).TERM = match(TERM);
			 _localctx.feature.add( root.getTerm( (((Phrase2Context)_localctx).TERM!=null?((Phrase2Context)_localctx).TERM.getText():null) ) ); 
			}
			 
			             _localctx.feature.setspan( new Long( _localctx.feature.containedfeatures.size() ));
			          
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
		public FeatureSynonym feature;
		public Token ALTTERM;
		public Token TERM;
		public List<TerminalNode> WS() { return getTokens(QueryParser.WS); }
		public TerminalNode WS(int i) {
			return getToken(QueryParser.WS, i);
		}
		public TerminalNode TERM() { return getToken(QueryParser.TERM, 0); }
		public List<TerminalNode> ALTTERM() { return getTokens(QueryParser.ALTTERM); }
		public TerminalNode ALTTERM(int i) {
			return getToken(QueryParser.ALTTERM, i);
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
		enterRule(_localctx, 8, RULE_syn);
		 ((SynContext)_localctx).feature =  new FeatureSynonym( root ); 
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(81); 
			_errHandler.sync(this);
			_la = _input.LA(1);
			do {
				{
				{
				setState(76); ((SynContext)_localctx).ALTTERM = match(ALTTERM);
				 _localctx.feature.add( root.getTerm( (((SynContext)_localctx).ALTTERM!=null?((SynContext)_localctx).ALTTERM.getText():null).substring(0, (((SynContext)_localctx).ALTTERM!=null?((SynContext)_localctx).ALTTERM.getText():null).length() - 1 ) ) ); 
				          
				setState(79);
				_la = _input.LA(1);
				if (_la==WS) {
					{
					setState(78); match(WS);
					}
				}

				}
				}
				setState(83); 
				_errHandler.sync(this);
				_la = _input.LA(1);
			} while ( _la==ALTTERM );
			setState(85); ((SynContext)_localctx).TERM = match(TERM);
			 _localctx.feature.add( root.getTerm( (((SynContext)_localctx).TERM!=null?((SynContext)_localctx).TERM.getText():null) ) ); 
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
		public FeatureSynonym feature;
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
		enterRule(_localctx, 10, RULE_syn2);
		 ((Syn2Context)_localctx).feature =  new FeatureSynonym( root ); 
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(88); match(BLOCKOPEN);
			setState(90);
			_la = _input.LA(1);
			if (_la==WS) {
				{
				setState(89); match(WS);
				}
			}

			setState(102); 
			_errHandler.sync(this);
			_la = _input.LA(1);
			do {
				{
				{
				setState(97);
				switch (_input.LA(1)) {
				case TERM:
				case PHRASETERM:
				case ALTTERM:
				case FUNCTION:
				case BRACKOPEN:
				case BRACEOPEN:
				case BLOCKOPEN:
					{
					setState(92); ((Syn2Context)_localctx).term = term();
					 _localctx.feature.add( ((Syn2Context)_localctx).term.feature );  
					}
					break;
				case VARIABLE:
					{
					setState(95); ((Syn2Context)_localctx).VARIABLE = match(VARIABLE);
					 if ((((Syn2Context)_localctx).VARIABLE!=null?((Syn2Context)_localctx).VARIABLE.getText():null).indexOf(".") >= 0)
					                        _localctx.feature.setGenericD( (((Syn2Context)_localctx).VARIABLE!=null?((Syn2Context)_localctx).VARIABLE.getText():null) );
					                     else 
					                        _localctx.feature.setGenericL( (((Syn2Context)_localctx).VARIABLE!=null?((Syn2Context)_localctx).VARIABLE.getText():null) ); 
					}
					break;
				default:
					throw new NoViableAltException(this);
				}
				setState(100);
				_la = _input.LA(1);
				if (_la==WS) {
					{
					setState(99); match(WS);
					}
				}

				}
				}
				setState(104); 
				_errHandler.sync(this);
				_la = _input.LA(1);
			} while ( (((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << TERM) | (1L << PHRASETERM) | (1L << VARIABLE) | (1L << ALTTERM) | (1L << FUNCTION) | (1L << BRACKOPEN) | (1L << BRACEOPEN) | (1L << BLOCKOPEN))) != 0) );
			setState(106); match(BLOCKCLOSE);
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
		public FeatureProximity feature;
		public TermContext term;
		public Token VARIABLE;
		public List<TermContext> term() {
			return getRuleContexts(TermContext.class);
		}
		public List<TerminalNode> WS() { return getTokens(QueryParser.WS); }
		public List<TerminalNode> VARIABLE() { return getTokens(QueryParser.VARIABLE); }
		public TerminalNode BRACEOPEN() { return getToken(QueryParser.BRACEOPEN, 0); }
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
		enterRule(_localctx, 12, RULE_set);
		 ((SetContext)_localctx).feature =  new FeatureProximityUnordered( root ); 
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(108); match(BRACEOPEN);
			{
			setState(110);
			_la = _input.LA(1);
			if (_la==WS) {
				{
				setState(109); match(WS);
				}
			}

			}
			setState(122); 
			_errHandler.sync(this);
			_la = _input.LA(1);
			do {
				{
				{
				setState(117);
				switch (_input.LA(1)) {
				case TERM:
				case PHRASETERM:
				case ALTTERM:
				case FUNCTION:
				case BRACKOPEN:
				case BRACEOPEN:
				case BLOCKOPEN:
					{
					setState(112); ((SetContext)_localctx).term = term();
					 _localctx.feature.add( ((SetContext)_localctx).term.feature ); 
					}
					break;
				case VARIABLE:
					{
					setState(115); ((SetContext)_localctx).VARIABLE = match(VARIABLE);
					 if ((((SetContext)_localctx).VARIABLE!=null?((SetContext)_localctx).VARIABLE.getText():null).indexOf(".") >= 0)
					                        _localctx.feature.setGenericD( (((SetContext)_localctx).VARIABLE!=null?((SetContext)_localctx).VARIABLE.getText():null) );
					                     else 
					                        _localctx.feature.setGenericL( (((SetContext)_localctx).VARIABLE!=null?((SetContext)_localctx).VARIABLE.getText():null) ); 
					}
					break;
				default:
					throw new NoViableAltException(this);
				}
				setState(120);
				_la = _input.LA(1);
				if (_la==WS) {
					{
					setState(119); match(WS);
					}
				}

				}
				}
				setState(124); 
				_errHandler.sync(this);
				_la = _input.LA(1);
			} while ( (((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << TERM) | (1L << PHRASETERM) | (1L << VARIABLE) | (1L << ALTTERM) | (1L << FUNCTION) | (1L << BRACKOPEN) | (1L << BRACEOPEN) | (1L << BLOCKOPEN))) != 0) );
			setState(126); match(BRACECLOSE);
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

	public static class FunctionContext extends ParserRuleContext {
		public GraphNode feature;
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
		public TermContext term(int i) {
			return getRuleContext(TermContext.class,i);
		}
		public FunctionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_function; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof QueryListener ) ((QueryListener)listener).enterFunction(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof QueryListener ) ((QueryListener)listener).exitFunction(this);
		}
	}

	public final FunctionContext function() throws RecognitionException {
		FunctionContext _localctx = new FunctionContext(_ctx, getState());
		enterRule(_localctx, 14, RULE_function);
		 ArrayList<GraphNode> terms = new ArrayList<GraphNode>(); 
		           ArrayList<String> varl = new ArrayList<String>(); 
		           ArrayList<String> vard = new ArrayList<String>(); 
		           String functionclass;
		         
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(128); ((FunctionContext)_localctx).FUNCTION = match(FUNCTION);
			 functionclass = (((FunctionContext)_localctx).FUNCTION!=null?((FunctionContext)_localctx).FUNCTION.getText():null); 
			                functionclass = functionclass.substring(0, functionclass.length()-1);
			              
			setState(130); match(BRACKOPEN);
			setState(132);
			_la = _input.LA(1);
			if (_la==WS) {
				{
				setState(131); match(WS);
				}
			}

			setState(147); 
			_errHandler.sync(this);
			_la = _input.LA(1);
			do {
				{
				{
				setState(139);
				switch (_input.LA(1)) {
				case TERM:
				case PHRASETERM:
				case ALTTERM:
				case FUNCTION:
				case BRACKOPEN:
				case BRACEOPEN:
				case BLOCKOPEN:
					{
					setState(134); ((FunctionContext)_localctx).term = term();
					 terms.add( ((FunctionContext)_localctx).term.feature ); 
					}
					break;
				case VARIABLE:
					{
					setState(137); ((FunctionContext)_localctx).VARIABLE = match(VARIABLE);
					 if ((((FunctionContext)_localctx).VARIABLE!=null?((FunctionContext)_localctx).VARIABLE.getText():null).indexOf(".") >= 0)
					                        vard.add( (((FunctionContext)_localctx).VARIABLE!=null?((FunctionContext)_localctx).VARIABLE.getText():null) );
					                     else 
					                        varl.add( (((FunctionContext)_localctx).VARIABLE!=null?((FunctionContext)_localctx).VARIABLE.getText():null) ); 
					}
					break;
				default:
					throw new NoViableAltException(this);
				}
				setState(144);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==WS) {
					{
					{
					setState(141); match(WS);
					}
					}
					setState(146);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				}
				}
				setState(149); 
				_errHandler.sync(this);
				_la = _input.LA(1);
			} while ( (((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << TERM) | (1L << PHRASETERM) | (1L << VARIABLE) | (1L << ALTTERM) | (1L << FUNCTION) | (1L << BRACKOPEN) | (1L << BRACEOPEN) | (1L << BLOCKOPEN))) != 0) );
			setState(151); match(BRACKCLOSE);
			}
			 ((FunctionContext)_localctx).feature =  root.construct( functionclass, terms );
			            for (String v : varl)
			               _localctx.feature.setGenericL( v );
			            for (String v : vard)
			               _localctx.feature.setGenericD( v );
			          
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
		public GraphNode feature;
		public Token TERM;
		public PhraseContext phrase;
		public FunctionContext function;
		public Phrase2Context phrase2;
		public SynContext syn;
		public Syn2Context syn2;
		public SetContext set;
		public FunctionContext function() {
			return getRuleContext(FunctionContext.class,0);
		}
		public Syn2Context syn2() {
			return getRuleContext(Syn2Context.class,0);
		}
		public Phrase2Context phrase2() {
			return getRuleContext(Phrase2Context.class,0);
		}
		public PhraseContext phrase() {
			return getRuleContext(PhraseContext.class,0);
		}
		public SynContext syn() {
			return getRuleContext(SynContext.class,0);
		}
		public TerminalNode TERM() { return getToken(QueryParser.TERM, 0); }
		public SetContext set() {
			return getRuleContext(SetContext.class,0);
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
		enterRule(_localctx, 16, RULE_term);
		try {
			setState(173);
			switch (_input.LA(1)) {
			case TERM:
				enterOuterAlt(_localctx, 1);
				{
				setState(153); ((TermContext)_localctx).TERM = match(TERM);
				 ((TermContext)_localctx).feature =  root.getTerm( (((TermContext)_localctx).TERM!=null?((TermContext)_localctx).TERM.getText():null) ); 
				}
				break;
			case BRACKOPEN:
				enterOuterAlt(_localctx, 2);
				{
				setState(155); ((TermContext)_localctx).phrase = phrase();
				 ((TermContext)_localctx).feature =  ((TermContext)_localctx).phrase.feature; 
				}
				break;
			case FUNCTION:
				enterOuterAlt(_localctx, 3);
				{
				setState(158); ((TermContext)_localctx).function = function();
				 ((TermContext)_localctx).feature =  ((TermContext)_localctx).function.feature; 
				}
				break;
			case PHRASETERM:
				enterOuterAlt(_localctx, 4);
				{
				setState(161); ((TermContext)_localctx).phrase2 = phrase2();
				 ((TermContext)_localctx).feature =  ((TermContext)_localctx).phrase2.feature; 
				}
				break;
			case ALTTERM:
				enterOuterAlt(_localctx, 5);
				{
				setState(164); ((TermContext)_localctx).syn = syn();
				 ((TermContext)_localctx).feature =  ((TermContext)_localctx).syn.feature; 
				}
				break;
			case BLOCKOPEN:
				enterOuterAlt(_localctx, 6);
				{
				setState(167); ((TermContext)_localctx).syn2 = syn2();
				 ((TermContext)_localctx).feature =  ((TermContext)_localctx).syn2.feature; 
				}
				break;
			case BRACEOPEN:
				enterOuterAlt(_localctx, 7);
				{
				setState(170); ((TermContext)_localctx).set = set();
				 ((TermContext)_localctx).feature =  ((TermContext)_localctx).set.feature; 
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
			setState(175); ((WeightContext)_localctx).WEIGHT = match(WEIGHT);
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
			setState(178); ((ChannelContext)_localctx).CHANNEL = match(CHANNEL);
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
		"\3\uacf5\uee8c\u4f5d\u8b0d\u4a45\u78bd\u1b2f\u3378\3\21\u00b8\4\2\t\2"+
		"\4\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4\13"+
		"\t\13\4\f\t\f\3\2\3\2\3\2\6\2\34\n\2\r\2\16\2\35\3\3\3\3\3\3\5\3#\n\3"+
		"\3\3\3\3\3\3\5\3(\n\3\3\3\5\3+\n\3\3\3\3\3\3\3\5\3\60\n\3\3\4\3\4\5\4"+
		"\64\n\4\3\4\3\4\3\4\3\4\3\4\5\4;\n\4\3\4\5\4>\n\4\6\4@\n\4\r\4\16\4A\3"+
		"\4\3\4\3\5\3\5\6\5H\n\5\r\5\16\5I\3\5\3\5\3\5\3\6\3\6\3\6\5\6R\n\6\6\6"+
		"T\n\6\r\6\16\6U\3\6\3\6\3\6\3\7\3\7\5\7]\n\7\3\7\3\7\3\7\3\7\3\7\5\7d"+
		"\n\7\3\7\5\7g\n\7\6\7i\n\7\r\7\16\7j\3\7\3\7\3\b\3\b\5\bq\n\b\3\b\3\b"+
		"\3\b\3\b\3\b\5\bx\n\b\3\b\5\b{\n\b\6\b}\n\b\r\b\16\b~\3\b\3\b\3\t\3\t"+
		"\3\t\3\t\5\t\u0087\n\t\3\t\3\t\3\t\3\t\3\t\5\t\u008e\n\t\3\t\7\t\u0091"+
		"\n\t\f\t\16\t\u0094\13\t\6\t\u0096\n\t\r\t\16\t\u0097\3\t\3\t\3\n\3\n"+
		"\3\n\3\n\3\n\3\n\3\n\3\n\3\n\3\n\3\n\3\n\3\n\3\n\3\n\3\n\3\n\3\n\3\n\3"+
		"\n\5\n\u00b0\n\n\3\13\3\13\3\13\3\f\3\f\3\f\3\f\2\r\2\4\6\b\n\f\16\20"+
		"\22\24\26\2\2\u00ca\2\33\3\2\2\2\4\37\3\2\2\2\6\61\3\2\2\2\bG\3\2\2\2"+
		"\nS\3\2\2\2\fZ\3\2\2\2\16n\3\2\2\2\20\u0082\3\2\2\2\22\u00af\3\2\2\2\24"+
		"\u00b1\3\2\2\2\26\u00b4\3\2\2\2\30\31\5\4\3\2\31\32\b\2\1\2\32\34\3\2"+
		"\2\2\33\30\3\2\2\2\34\35\3\2\2\2\35\33\3\2\2\2\35\36\3\2\2\2\36\3\3\2"+
		"\2\2\37 \5\22\n\2 \"\b\3\1\2!#\7\t\2\2\"!\3\2\2\2\"#\3\2\2\2#\'\3\2\2"+
		"\2$%\5\24\13\2%&\b\3\1\2&(\3\2\2\2\'$\3\2\2\2\'(\3\2\2\2(*\3\2\2\2)+\7"+
		"\t\2\2*)\3\2\2\2*+\3\2\2\2+/\3\2\2\2,-\5\26\f\2-.\b\3\1\2.\60\3\2\2\2"+
		"/,\3\2\2\2/\60\3\2\2\2\60\5\3\2\2\2\61\63\7\f\2\2\62\64\7\t\2\2\63\62"+
		"\3\2\2\2\63\64\3\2\2\2\64?\3\2\2\2\65\66\5\22\n\2\66\67\b\4\1\2\67;\3"+
		"\2\2\289\7\5\2\29;\b\4\1\2:\65\3\2\2\2:8\3\2\2\2;=\3\2\2\2<>\7\t\2\2="+
		"<\3\2\2\2=>\3\2\2\2>@\3\2\2\2?:\3\2\2\2@A\3\2\2\2A?\3\2\2\2AB\3\2\2\2"+
		"BC\3\2\2\2CD\7\r\2\2D\7\3\2\2\2EF\7\4\2\2FH\b\5\1\2GE\3\2\2\2HI\3\2\2"+
		"\2IG\3\2\2\2IJ\3\2\2\2JK\3\2\2\2KL\7\3\2\2LM\b\5\1\2M\t\3\2\2\2NO\7\6"+
		"\2\2OQ\b\6\1\2PR\7\t\2\2QP\3\2\2\2QR\3\2\2\2RT\3\2\2\2SN\3\2\2\2TU\3\2"+
		"\2\2US\3\2\2\2UV\3\2\2\2VW\3\2\2\2WX\7\3\2\2XY\b\6\1\2Y\13\3\2\2\2Z\\"+
		"\7\20\2\2[]\7\t\2\2\\[\3\2\2\2\\]\3\2\2\2]h\3\2\2\2^_\5\22\n\2_`\b\7\1"+
		"\2`d\3\2\2\2ab\7\5\2\2bd\b\7\1\2c^\3\2\2\2ca\3\2\2\2df\3\2\2\2eg\7\t\2"+
		"\2fe\3\2\2\2fg\3\2\2\2gi\3\2\2\2hc\3\2\2\2ij\3\2\2\2jh\3\2\2\2jk\3\2\2"+
		"\2kl\3\2\2\2lm\7\21\2\2m\r\3\2\2\2np\7\16\2\2oq\7\t\2\2po\3\2\2\2pq\3"+
		"\2\2\2q|\3\2\2\2rs\5\22\n\2st\b\b\1\2tx\3\2\2\2uv\7\5\2\2vx\b\b\1\2wr"+
		"\3\2\2\2wu\3\2\2\2xz\3\2\2\2y{\7\t\2\2zy\3\2\2\2z{\3\2\2\2{}\3\2\2\2|"+
		"w\3\2\2\2}~\3\2\2\2~|\3\2\2\2~\177\3\2\2\2\177\u0080\3\2\2\2\u0080\u0081"+
		"\7\17\2\2\u0081\17\3\2\2\2\u0082\u0083\7\7\2\2\u0083\u0084\b\t\1\2\u0084"+
		"\u0086\7\f\2\2\u0085\u0087\7\t\2\2\u0086\u0085\3\2\2\2\u0086\u0087\3\2"+
		"\2\2\u0087\u0095\3\2\2\2\u0088\u0089\5\22\n\2\u0089\u008a\b\t\1\2\u008a"+
		"\u008e\3\2\2\2\u008b\u008c\7\5\2\2\u008c\u008e\b\t\1\2\u008d\u0088\3\2"+
		"\2\2\u008d\u008b\3\2\2\2\u008e\u0092\3\2\2\2\u008f\u0091\7\t\2\2\u0090"+
		"\u008f\3\2\2\2\u0091\u0094\3\2\2\2\u0092\u0090\3\2\2\2\u0092\u0093\3\2"+
		"\2\2\u0093\u0096\3\2\2\2\u0094\u0092\3\2\2\2\u0095\u008d\3\2\2\2\u0096"+
		"\u0097\3\2\2\2\u0097\u0095\3\2\2\2\u0097\u0098\3\2\2\2\u0098\u0099\3\2"+
		"\2\2\u0099\u009a\7\r\2\2\u009a\21\3\2\2\2\u009b\u009c\7\3\2\2\u009c\u00b0"+
		"\b\n\1\2\u009d\u009e\5\6\4\2\u009e\u009f\b\n\1\2\u009f\u00b0\3\2\2\2\u00a0"+
		"\u00a1\5\20\t\2\u00a1\u00a2\b\n\1\2\u00a2\u00b0\3\2\2\2\u00a3\u00a4\5"+
		"\b\5\2\u00a4\u00a5\b\n\1\2\u00a5\u00b0\3\2\2\2\u00a6\u00a7\5\n\6\2\u00a7"+
		"\u00a8\b\n\1\2\u00a8\u00b0\3\2\2\2\u00a9\u00aa\5\f\7\2\u00aa\u00ab\b\n"+
		"\1\2\u00ab\u00b0\3\2\2\2\u00ac\u00ad\5\16\b\2\u00ad\u00ae\b\n\1\2\u00ae"+
		"\u00b0\3\2\2\2\u00af\u009b\3\2\2\2\u00af\u009d\3\2\2\2\u00af\u00a0\3\2"+
		"\2\2\u00af\u00a3\3\2\2\2\u00af\u00a6\3\2\2\2\u00af\u00a9\3\2\2\2\u00af"+
		"\u00ac\3\2\2\2\u00b0\23\3\2\2\2\u00b1\u00b2\7\n\2\2\u00b2\u00b3\b\13\1"+
		"\2\u00b3\25\3\2\2\2\u00b4\u00b5\7\b\2\2\u00b5\u00b6\b\f\1\2\u00b6\27\3"+
		"\2\2\2\33\35\"\'*/\63:=AIQU\\cfjpwz~\u0086\u008d\u0092\u0097\u00af";
	public static final ATN _ATN =
		ATNSimulator.deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}