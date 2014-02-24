// Generated from Query.g4 by ANTLR 4.1

    package io.github.repir.QueryParser;
    import java.util.ArrayList;
    import io.github.repir.Repository.*;
    import io.github.repir.Retriever.*;
    import io.github.repir.Strategy.*;
    import io.github.repir.tools.Lib.Log;

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

	/**
	 * Enter a parse tree produced by {@link QueryParser#function}.
	 * @param ctx the parse tree
	 */
	void enterFunction(@NotNull QueryParser.FunctionContext ctx);
	/**
	 * Exit a parse tree produced by {@link QueryParser#function}.
	 * @param ctx the parse tree
	 */
	void exitFunction(@NotNull QueryParser.FunctionContext ctx);
}