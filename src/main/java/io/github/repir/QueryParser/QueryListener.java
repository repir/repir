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

import org.antlr.v4.runtime.misc.NotNull;
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link QueryParser}.
 */
public interface QueryListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link QueryParser#prog}.
	 * @param ctx the parse tree
	 */
	void enterProg(@NotNull QueryParser.ProgContext ctx);
	/**
	 * Exit a parse tree produced by {@link QueryParser#prog}.
	 * @param ctx the parse tree
	 */
	void exitProg(@NotNull QueryParser.ProgContext ctx);

	/**
	 * Enter a parse tree produced by {@link QueryParser#weight}.
	 * @param ctx the parse tree
	 */
	void enterWeight(@NotNull QueryParser.WeightContext ctx);
	/**
	 * Exit a parse tree produced by {@link QueryParser#weight}.
	 * @param ctx the parse tree
	 */
	void exitWeight(@NotNull QueryParser.WeightContext ctx);

	/**
	 * Enter a parse tree produced by {@link QueryParser#syn}.
	 * @param ctx the parse tree
	 */
	void enterSyn(@NotNull QueryParser.SynContext ctx);
	/**
	 * Exit a parse tree produced by {@link QueryParser#syn}.
	 * @param ctx the parse tree
	 */
	void exitSyn(@NotNull QueryParser.SynContext ctx);

	/**
	 * Enter a parse tree produced by {@link QueryParser#endterm}.
	 * @param ctx the parse tree
	 */
	void enterEndterm(@NotNull QueryParser.EndtermContext ctx);
	/**
	 * Exit a parse tree produced by {@link QueryParser#endterm}.
	 * @param ctx the parse tree
	 */
	void exitEndterm(@NotNull QueryParser.EndtermContext ctx);

	/**
	 * Enter a parse tree produced by {@link QueryParser#term}.
	 * @param ctx the parse tree
	 */
	void enterTerm(@NotNull QueryParser.TermContext ctx);
	/**
	 * Exit a parse tree produced by {@link QueryParser#term}.
	 * @param ctx the parse tree
	 */
	void exitTerm(@NotNull QueryParser.TermContext ctx);

	/**
	 * Enter a parse tree produced by {@link QueryParser#set}.
	 * @param ctx the parse tree
	 */
	void enterSet(@NotNull QueryParser.SetContext ctx);
	/**
	 * Exit a parse tree produced by {@link QueryParser#set}.
	 * @param ctx the parse tree
	 */
	void exitSet(@NotNull QueryParser.SetContext ctx);

	/**
	 * Enter a parse tree produced by {@link QueryParser#phrase}.
	 * @param ctx the parse tree
	 */
	void enterPhrase(@NotNull QueryParser.PhraseContext ctx);
	/**
	 * Exit a parse tree produced by {@link QueryParser#phrase}.
	 * @param ctx the parse tree
	 */
	void exitPhrase(@NotNull QueryParser.PhraseContext ctx);

	/**
	 * Enter a parse tree produced by {@link QueryParser#func}.
	 * @param ctx the parse tree
	 */
	void enterFunc(@NotNull QueryParser.FuncContext ctx);
	/**
	 * Exit a parse tree produced by {@link QueryParser#func}.
	 * @param ctx the parse tree
	 */
	void exitFunc(@NotNull QueryParser.FuncContext ctx);

	/**
	 * Enter a parse tree produced by {@link QueryParser#syn2}.
	 * @param ctx the parse tree
	 */
	void enterSyn2(@NotNull QueryParser.Syn2Context ctx);
	/**
	 * Exit a parse tree produced by {@link QueryParser#syn2}.
	 * @param ctx the parse tree
	 */
	void exitSyn2(@NotNull QueryParser.Syn2Context ctx);

	/**
	 * Enter a parse tree produced by {@link QueryParser#channel}.
	 * @param ctx the parse tree
	 */
	void enterChannel(@NotNull QueryParser.ChannelContext ctx);
	/**
	 * Exit a parse tree produced by {@link QueryParser#channel}.
	 * @param ctx the parse tree
	 */
	void exitChannel(@NotNull QueryParser.ChannelContext ctx);

	/**
	 * Enter a parse tree produced by {@link QueryParser#phrase2}.
	 * @param ctx the parse tree
	 */
	void enterPhrase2(@NotNull QueryParser.Phrase2Context ctx);
	/**
	 * Exit a parse tree produced by {@link QueryParser#phrase2}.
	 * @param ctx the parse tree
	 */
	void exitPhrase2(@NotNull QueryParser.Phrase2Context ctx);
}