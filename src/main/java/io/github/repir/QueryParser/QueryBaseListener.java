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


import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.misc.NotNull;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.TerminalNode;

/**
 * This class provides an empty implementation of {@link QueryListener},
 * which can be extended to create a listener which only needs to handle a subset
 * of the available methods.
 */
public class QueryBaseListener implements QueryListener {
	/**
	 * {@inheritDoc}
	 * <p/>
	 * The default implementation does nothing.
	 */
	@Override public void enterProg(@NotNull QueryParser.ProgContext ctx) { }
	/**
	 * {@inheritDoc}
	 * <p/>
	 * The default implementation does nothing.
	 */
	@Override public void exitProg(@NotNull QueryParser.ProgContext ctx) { }

	/**
	 * {@inheritDoc}
	 * <p/>
	 * The default implementation does nothing.
	 */
	@Override public void enterWeight(@NotNull QueryParser.WeightContext ctx) { }
	/**
	 * {@inheritDoc}
	 * <p/>
	 * The default implementation does nothing.
	 */
	@Override public void exitWeight(@NotNull QueryParser.WeightContext ctx) { }

	/**
	 * {@inheritDoc}
	 * <p/>
	 * The default implementation does nothing.
	 */
	@Override public void enterSyn(@NotNull QueryParser.SynContext ctx) { }
	/**
	 * {@inheritDoc}
	 * <p/>
	 * The default implementation does nothing.
	 */
	@Override public void exitSyn(@NotNull QueryParser.SynContext ctx) { }

	/**
	 * {@inheritDoc}
	 * <p/>
	 * The default implementation does nothing.
	 */
	@Override public void enterEndterm(@NotNull QueryParser.EndtermContext ctx) { }
	/**
	 * {@inheritDoc}
	 * <p/>
	 * The default implementation does nothing.
	 */
	@Override public void exitEndterm(@NotNull QueryParser.EndtermContext ctx) { }

	/**
	 * {@inheritDoc}
	 * <p/>
	 * The default implementation does nothing.
	 */
	@Override public void enterTerm(@NotNull QueryParser.TermContext ctx) { }
	/**
	 * {@inheritDoc}
	 * <p/>
	 * The default implementation does nothing.
	 */
	@Override public void exitTerm(@NotNull QueryParser.TermContext ctx) { }

	/**
	 * {@inheritDoc}
	 * <p/>
	 * The default implementation does nothing.
	 */
	@Override public void enterSet(@NotNull QueryParser.SetContext ctx) { }
	/**
	 * {@inheritDoc}
	 * <p/>
	 * The default implementation does nothing.
	 */
	@Override public void exitSet(@NotNull QueryParser.SetContext ctx) { }

	/**
	 * {@inheritDoc}
	 * <p/>
	 * The default implementation does nothing.
	 */
	@Override public void enterPhrase(@NotNull QueryParser.PhraseContext ctx) { }
	/**
	 * {@inheritDoc}
	 * <p/>
	 * The default implementation does nothing.
	 */
	@Override public void exitPhrase(@NotNull QueryParser.PhraseContext ctx) { }

	/**
	 * {@inheritDoc}
	 * <p/>
	 * The default implementation does nothing.
	 */
	@Override public void enterSyn2(@NotNull QueryParser.Syn2Context ctx) { }
	/**
	 * {@inheritDoc}
	 * <p/>
	 * The default implementation does nothing.
	 */
	@Override public void exitSyn2(@NotNull QueryParser.Syn2Context ctx) { }

	/**
	 * {@inheritDoc}
	 * <p/>
	 * The default implementation does nothing.
	 */
	@Override public void enterChannel(@NotNull QueryParser.ChannelContext ctx) { }
	/**
	 * {@inheritDoc}
	 * <p/>
	 * The default implementation does nothing.
	 */
	@Override public void exitChannel(@NotNull QueryParser.ChannelContext ctx) { }

	/**
	 * {@inheritDoc}
	 * <p/>
	 * The default implementation does nothing.
	 */
	@Override public void enterPhrase2(@NotNull QueryParser.Phrase2Context ctx) { }
	/**
	 * {@inheritDoc}
	 * <p/>
	 * The default implementation does nothing.
	 */
	@Override public void exitPhrase2(@NotNull QueryParser.Phrase2Context ctx) { }

	/**
	 * {@inheritDoc}
	 * <p/>
	 * The default implementation does nothing.
	 */
	@Override public void enterFunction(@NotNull QueryParser.FunctionContext ctx) { }
	/**
	 * {@inheritDoc}
	 * <p/>
	 * The default implementation does nothing.
	 */
	@Override public void exitFunction(@NotNull QueryParser.FunctionContext ctx) { }

	/**
	 * {@inheritDoc}
	 * <p/>
	 * The default implementation does nothing.
	 */
	@Override public void enterEveryRule(@NotNull ParserRuleContext ctx) { }
	/**
	 * {@inheritDoc}
	 * <p/>
	 * The default implementation does nothing.
	 */
	@Override public void exitEveryRule(@NotNull ParserRuleContext ctx) { }
	/**
	 * {@inheritDoc}
	 * <p/>
	 * The default implementation does nothing.
	 */
	@Override public void visitTerminal(@NotNull TerminalNode node) { }
	/**
	 * {@inheritDoc}
	 * <p/>
	 * The default implementation does nothing.
	 */
	@Override public void visitErrorNode(@NotNull ErrorNode node) { }
}