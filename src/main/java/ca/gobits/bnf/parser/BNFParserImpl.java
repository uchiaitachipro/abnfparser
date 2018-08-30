//
// Copyright 2013 Mike Friesen
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//

package ca.gobits.bnf.parser;

import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import ca.gobits.bnf.parser.ParserContext.ParserRepetition;
import ca.gobits.bnf.parser.ParserContext.ParserState;
import ca.gobits.bnf.parser.SymbolMetaData.Repetition;
import ca.gobits.bnf.tokenizer.Token;
import ca.gobits.bnf.tokenizer.TokenizerFactory;
import ca.gobits.bnf.tokenizer.BNFTokenizerFactoryImpl;

/**
 * BNF IParser implementation.
 */
public class BNFParserImpl implements IParser<Object> {

    /**
     * Number Pattern.
     */
    private final Pattern numberPattern = Pattern.compile("^[\\d\\-\\.]+$");

    /**
     * Holder for BNFSequences map.
     */
    private final Map<String, List<BNFSequence>> sequenceMap;

    /**
     * BNF processing stack.
     */
    private final Stack<ParserContext> stack = new Stack<ParserContext>();

    /**
     * IParser Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(IParser.class.getName());

    /**
     * constructor.
     *
     * @param map -
     */
    public BNFParserImpl(final Map<String, List<BNFSequence>> map) {
        this.sequenceMap = map;
    }

    @Override
    public IParseResult parse(final String string) {
        TokenizerFactory tokenizer = new BNFTokenizerFactoryImpl();
        Token token = tokenizer.tokens(string);
        return parse(token,string);
    }

    @Override
    public Object getTag() {
        return null;
    }

    @Override
    public void setTag(Object obj) {

    }

    @Override
    public IParseResult parse(final Token token,String rawContent) {
        List<BNFSequence> sd = this.sequenceMap.get("@start");
        addParserState(sd, token, ParserContext.ParserRepetition.NONE, SymbolMetaData.Repetition.NONE);

        return parseSequences(token);
    }

    /**
     * Main loop for parsing.
     *
     * @param startToken -
     * @return IParseResultImpl
     */
    private IParseResultImpl parseSequences(final Token startToken) {

        boolean success = false;
        Token maxMatchToken = startToken;
        Token errorToken = null;

        IParseResultImpl result = new IParseResultImpl();
        result.setTop(startToken);

        while (!this.stack.isEmpty()) {

            ParserContext holder = this.stack.peek();

            if (holder.getState() == ParserState.EMPTY) {

                this.stack.pop();
                Token token = this.stack.peek().getCurrentToken();
                if (!isEmpty(token)) {
                    rewindToNextSymbol();
                } else {
                    success = true;
                    errorToken = null;
                    rewindToNextSequence();
                }

            } else if (holder.getState() == ParserState.NO_MATCH_WITH_ZERO_REPETITION) {

                processNoMatchWithZeroRepetition();

            } else if (holder.getState() == ParserState.MATCH_WITH_ZERO_REPETITION) {

                processMatchWithZeroRepetition();

            } else if (holder.getState() == ParserState.NO_MATCH_WITH_ZERO_REPETITION_LOOKING_FOR_FIRST_MATCH) {

                maxMatchToken = processNoMatchWithZeroRepetitionLookingForFirstMatch();
                errorToken = null;
                success = true;

            } else if (holder.getState() == ParserState.MATCH) {

                maxMatchToken = processMatch();
                errorToken = null;
                success = true;

            } else if (holder.getState() == ParserState.NO_MATCH) {

                Token eToken = processNoMatch();
                errorToken = updateErrorToken(errorToken, eToken);
                success = false;

            } else {
                processStack();
            }
        }

        updateResult(result, maxMatchToken, errorToken, success);

        return result;
    }

    /**
     * Update BNFParserResult.
     *
     * @param result        -
     * @param maxMatchToken -
     * @param errorToken    -
     * @param success       -
     */
    private void updateResult(
            final IParseResultImpl result,
            final Token maxMatchToken,
            final Token errorToken,
            final boolean success) {

        boolean succ = success;
        Token errToken = errorToken;

        if (maxMatchToken != null && maxMatchToken.getNextToken() != null) {

            if (errorToken == null) {
                errToken = maxMatchToken.getNextToken();
            }

            succ = false;
        }

        result.setError(errToken);
        result.setMaxMatchToken(maxMatchToken);
        result.setSuccess(succ);
    }

    /**
     * Returns The Token with the largest ID.
     *
     * @param token1 -
     * @param token2 -
     * @return Token
     */
    private Token updateErrorToken(final Token token1, final Token token2) {
        return token1 != null && token1.getId() > token2.getId() ? token1 : token2;
    }

    /**
     * Rewind stack to the next sequence.
     *
     * @return Token
     */
    private Token processNoMatch() {

        debugPrintIndents();
        LOGGER.finer("-> no match, rewinding to next sequence");

        this.stack.pop();

        Token token = this.stack.peek().getCurrentToken();

        rewindToNextSequence();

        if (!this.stack.isEmpty()) {
            ParserContext holder = this.stack.peek();
            holder.resetToken();
        }

        return token;
    }

    /**
     * @return Token
     */
    private Token processMatchWithZeroRepetition() {
        this.stack.pop();

        Token token = this.stack.peek().getCurrentToken();

        debugPrintIndents();
        LOGGER.finer("-> matched token " + token.getStringValue() + " rewind to start of repetition");

        rewindToOutsideOfRepetition();

        if (!this.stack.isEmpty()) {
            ParserContext holder = this.stack.peek();
            holder.advanceToken(token.getNextToken());
        }

        return token;
    }

    /**
     * @return Token
     */
    private Token processNoMatchWithZeroRepetitionLookingForFirstMatch() {

        this.stack.pop();

        Token token = this.stack.peek().getCurrentToken();

        debugPrintIndents();
        LOGGER.finer("-> no match Zero Or More Looking for First Match token "
                + debug(token) + " rewind outside of Repetition");

        rewindToOutsideOfRepetition();
        rewindToNextSymbol();

        if (!this.stack.isEmpty()) {
            ParserContext holder = this.stack.peek();
            holder.advanceToken(token);
        }

        return token;
    }

    /**
     * Rewind stack to next symbol.
     *
     * @return Token
     */
    private Token processMatch() {

        this.stack.pop();

        Token token = this.stack.peek().getCurrentToken();

        debugPrintIndents();
        LOGGER.finer("-> matched token " + token.getStringValue() + " rewind to next symbol");

        rewindToNextSymbolOrRepetition();

        if (!this.stack.isEmpty()) {
            ParserContext holder = this.stack.peek();

            token = token.getNextToken();
            holder.advanceToken(token);
        }

        return token;
    }

    /**
     * processNoMatchWithZeroRepetition.
     */
    private void processNoMatchWithZeroRepetition() {

        debugPrintIndents();
        LOGGER.finer("-> " + ParserState.NO_MATCH_WITH_ZERO_REPETITION + ", rewind to next symbol");

        this.stack.pop();

        Token token = this.stack.peek().getCurrentToken();

        rewindToNextSymbol();

        if (!this.stack.isEmpty()) {
            ParserContext holder = this.stack.peek();
            holder.advanceToken(token);
        }
    }

    /**
     * rewindToOutsideOfRepetition.
     */
    private void rewindToOutsideOfRepetition() {

        while (!this.stack.isEmpty()) {
            ParserContext holder = this.stack.peek();

            if (holder.getParserRepetition() != ParserContext.ParserRepetition.NONE) {
                this.stack.pop();
            } else {
                break;
            }
        }
    }

    /**
     * Rewinds to next incomplete sequence or to ZERO_OR_MORE repetition which
     * ever one is first.
     */
    private void rewindToNextSymbolOrRepetition() {
        while (!this.stack.isEmpty()) {
            ParserContext holder = this.stack.peek();

            if (holder.getRepetition() == SymbolMetaData.Repetition.ZERO_OR_MORE && holder.isComplete()) {
                holder.reset();
                if (holder.getRepetition() != SymbolMetaData.Repetition.NONE) {
                    holder.setParserRepetition(ParserRepetition.ZERO_OR_MORE_LOOKING_FOR_FIRST_MATCH);
                }
                break;
            } else if (holder.hasAndConditions() && !holder.isComplete()) {
                if (holder.getParserRepetition() == ParserRepetition.ZERO_OR_MORE_LOOKING_FOR_FIRST_MATCH) {
                    holder.setParserRepetition(ParserRepetition.NONE);
                }

                break;
            }

            this.stack.pop();
        }
    }

    /**
     * Rewinds to next incomplete sequence or to ZERO_OR_MORE repetition which
     * ever one is first.
     */
    private void rewindToNextSymbol() {
        while (!this.stack.isEmpty()) {
            ParserContext holder = this.stack.peek();

            if (holder.hasAndConditions() && !holder.isComplete()) {
                break;
            }

            this.stack.pop();
        }
    }

    /**
     * rewindToNextSequence.
     */
    private void rewindToNextSequence() {

        while (!this.stack.isEmpty()) {
            ParserContext holder = this.stack.peek();
            if (holder.hasOrConditions()) {
                break;
            }

            this.stack.pop();
        }
    }

    /**
     * processStack.
     */
    private void processStack() {

        ParserContext holder = this.stack.peek();

        if (holder.isComplete()) {
            this.stack.pop();
        } else {

            Token currentToken = holder.getCurrentToken();

            if (holder.hasOrConditions()) {

                BNFSequence sequence = holder.getNextOrCondition();
                addParserState(sequence, currentToken, holder.getParserRepetition(), Repetition.NONE);

            } else if (holder.hasAndConditions()) {

                SymbolMetaData symbol = holder.getNextAndConditionSymbol();
                List<BNFSequence> sd = this.sequenceMap.get(symbol.getName());

                ParserRepetition repetition = getParserRepetition(holder, symbol);

                if (sd != null) {

                    addParserState(sd, currentToken, repetition, symbol.getRepetition());

                } else {

                    ParserState state = getParserState(symbol, currentToken, repetition);
                    addParserState(state);
                }
            }
        }
    }

    /**
     * Gets the IParser State.
     *
     * @param symbol     -
     * @param token      -
     * @param repetition -
     * @return ParserState
     */
    private ParserState getParserState(
            final SymbolMetaData symbol,
            final Token token,
            final ParserRepetition repetition) {

        ParserState state = ParserState.NO_MATCH;

        String symbolName = symbol.getName();

        if (symbolName.equals("Empty")) {

            state = ParserState.EMPTY;

        } else if (isMatch(symbolName, token)) {

            state = ParserState.MATCH;

        } else if (repetition == ParserContext.ParserRepetition.ZERO_OR_MORE_LOOKING_FOR_FIRST_MATCH) {

            state = ParserState.NO_MATCH_WITH_ZERO_REPETITION_LOOKING_FOR_FIRST_MATCH;

        } else if (repetition == ParserRepetition.ZERO_OR_MORE) {

            state = ParserState.NO_MATCH_WITH_ZERO_REPETITION;
        }

        return state;
    }

    /**
     * @param symbolName -
     * @param token      -
     * @return boolean
     */
    private boolean isMatch(final String symbolName, final Token token) {

        boolean match = false;

        if (token != null) {
            String s = isQuotedString(symbolName) ? symbolName.substring(1, symbolName.length() - 1) : symbolName;
            match = s.equals(token.getStringValue()) || isQuotedString(symbolName, token)
                    || isNumber(symbolName, token);
        }

        return match;
    }

    /**
     * @param value -
     * @return boolean
     */
    private boolean isQuotedString(final String value) {
        return (value.startsWith("\"") && value.endsWith("\"")) || value.startsWith("'") && value.endsWith("'");
    }

    /**
     * @param symbolName -
     * @param token      -
     * @return boolean
     */
    private boolean isQuotedString(final String symbolName, final Token token) {
        String value = token.getStringValue();
        return symbolName.equals("QuotedString") && isQuotedString(value);
    }

    /**
     * @param symbolName -
     * @param token      -
     * @return boolean
     */
    private boolean isNumber(final String symbolName, final Token token) {

        boolean match = false;

        if (token != null && symbolName.equals("Number")) {
            String value = token.getStringValue();
            match = this.numberPattern.matcher(value).matches();
        }

        return match;
    }

    /**
     * @param state -
     */
    private void addParserState(final ParserState state) {
        this.stack.push(new ParserContext(state));
    }

    /**
     * @param sequences        -
     * @param token            -
     * @param parserRepetition -
     * @param repetition       -
     */
    private void addParserState(final List<BNFSequence> sequences, final Token token,
                                final ParserRepetition parserRepetition, final SymbolMetaData.Repetition repetition) {

        if (sequences.size() == 1) {
            addParserState(sequences.get(0), token, parserRepetition, repetition);
        } else {
            debug(sequences, token, parserRepetition);
            this.stack.push(new ParserContext(sequences, token, parserRepetition, repetition));
        }
    }

    /**
     * @param sequence         -
     * @param token            -
     * @param parserRepetition -
     * @param repetition       -
     */
    private void addParserState(
            final BNFSequence sequence,
            final Token token,
            final ParserRepetition parserRepetition,
            final SymbolMetaData.Repetition repetition) {
        debug(sequence, token, parserRepetition);
        this.stack.push(new ParserContext(sequence, token, parserRepetition, repetition));
    }

    /**
     * @param holder -
     * @param symbol -
     * @return ParserRepetition
     */
    private ParserContext.ParserRepetition getParserRepetition(final ParserContext holder, final SymbolMetaData symbol) {

        SymbolMetaData.Repetition symbolRepetition = symbol.getRepetition();
        ParserContext.ParserRepetition holderRepetition = holder.getParserRepetition();

        if (symbolRepetition != SymbolMetaData.Repetition.NONE && holderRepetition == ParserRepetition.NONE) {
            holderRepetition = ParserContext.ParserRepetition.ZERO_OR_MORE_LOOKING_FOR_FIRST_MATCH;
        } else if (symbolRepetition != Repetition.NONE && holderRepetition != ParserContext.ParserRepetition.NONE) {
            holderRepetition = ParserContext.ParserRepetition.ZERO_OR_MORE;
        }

        return holderRepetition;
    }

    /**
     * @param currentToken -
     * @return boolean
     */
    private boolean isEmpty(final Token currentToken) {
        return currentToken == null
                || currentToken.getStringValue() == null
                || currentToken.getStringValue().length() == 0;
    }

    /**
     * debug.
     */
    private void debugPrintIndents() {
        int size = this.stack.size() - 1;
        for (int i = 0; i < size; i++) {
            LOGGER.finer(" ");
        }
    }

    /**
     * debug.
     *
     * @param token -
     * @return String
     */
    private String debug(final Token token) {
        return token != null ? token.getStringValue() : null;
    }

    /**
     * debug.
     *
     * @param sequence   -
     * @param token      -
     * @param repetition -
     */
    private void debug(final BNFSequence sequence, final Token token, final ParserRepetition repetition) {
        debugPrintIndents();
        LOGGER.finer("-> procesing pipe line " + sequence + " for token "
                + debug(token) + " with repetition " + repetition);
    }

    /**
     * debug.
     *
     * @param sd         -
     * @param token      -
     * @param repetition -
     */
    private void debug(final List<BNFSequence> sd, final Token token, final ParserContext.ParserRepetition repetition) {
        debugPrintIndents();
        LOGGER.finer("-> adding pipe lines " + sd
                + " for token " + debug(token) + " with repetition "
                + repetition);
    }
}