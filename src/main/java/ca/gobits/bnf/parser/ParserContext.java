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

import ca.gobits.bnf.parser.SymbolMetaData.Repetition;
import ca.gobits.bnf.tokenizer.Token;

/**
 * ParserContext holds the states of the parser.
 */
public class ParserContext {

    /** ParserRepetition. */
    public enum ParserRepetition {
        /** NONE. */
        NONE,
        /** ZERO OR MORE. */
        ZERO_OR_MORE,
        /** ZERO OR MORE LOOKING FOR FIRST MATCH. */
        ZERO_OR_MORE_LOOKING_FOR_FIRST_MATCH
    }

    /** IParser State. */
    public enum ParserState {
        /** NONE. */
        NONE,

        /** MATCH. */
        MATCH,

        /** MATCH_IN_SEQUENCE_PARTICALLY. */
        MATCH_IN_SEQUENCE_PARTICALLY,

        /** MATCH_WITH_ONE_OR_MORE_REPETITION_PARTIALLY */
        MATCH_WITH_ONE_OR_MORE_REPETITION_PARTIALLY,

        /** MATCH_REPETITION_FINISHED_LOOKING_FOR_NEXT */
        MATCH_REPETITION_FINISHED_LOOKING_FOR_NEXT,

        /** NO_MATCH_WITH_ZERO_REPETITION_LOOKING_FOR_FIRST_MATCH. */
        NO_MATCH_WITH_ZERO_REPETITION_LOOKING_FOR_FIRST_MATCH,

        /** NO_MATCH. */
        NO_MATCH,

        /** MATCH_WITH_ZERO_REPETITION. */
        MATCH_WITH_ZERO_REPETITION,

        /** NO_MATCH_WITH_ZERO_REPETITION. */
        NO_MATCH_WITH_ZERO_REPETITION,

        /** EMPTY. */
        EMPTY
    }

    /** position in andConditions or symbols. */
    private int currentPosition = -1;

    /**
     * next symbol start location to partialMatch leftover strings
     */
    private int offset;

    /** current state. */
    private ParserState state;

    /** original token. */
    private Token originalToken;

    /** current token. */
    private Token currentToken;

    /** List<BNFSequence>. */
    private List<BNFSequence> orConditions;

    /** BNFSequence. */
    private BNFSequence andConditions;

    /** Repetition. */
    private SymbolMetaData.Repetition repetition;

    /** ParserRepetition. */
    private ParserRepetition parserRepetition;

    private int minRepetition = 0;

    private int maxRepetition = Integer.MAX_VALUE;

    private int currentRepetition = 0;

    private boolean allowAskForUserWhenNoMatch = false;

    private boolean goNextIfNoMatch = false;

    private int recentRepetitionMatchCount = 0;

    /**
     * default constructor.
     */
    private ParserContext() {
        setState(ParserState.NONE);
        this.parserRepetition = ParserRepetition.NONE;
    }

    /**
     * constructor.
     * @param parserState -
     */
    public ParserContext(final ParserState parserState) {
        this();
        setState(parserState);
    }

    /**
     * constructor.
     * @param seqs -
     * @param token
     *            -
     */
    public ParserContext(final List<BNFSequence> seqs, final Token token) {
        this(token);
        this.orConditions = seqs;
    }

    /**
     * constructor.
     * @param token -
     */
    private ParserContext(final Token token) {
        this();
        this.originalToken = token;
        this.currentToken = this.originalToken;
    }

    /**
     * constructor.
     * @param seq -
     * @param token -
     */
    public ParserContext(final BNFSequence seq, final Token token) {
        this(token);
        this.andConditions = seq;
    }

    /**
     * constructor.
     * @param sd -
     * @param token -
     * @param parserRep -
     * @param rep -
     */
    public ParserContext(
            final List<BNFSequence> sd,
            final Token token,
            final ParserRepetition parserRep,
            final Repetition rep) {
        this(sd, token);
        this.parserRepetition = parserRep;
        this.repetition = rep;
    }

    public ParserContext(
            final BNFSequence seq,
            final Token token,
            final ParserRepetition parserRep,
            final SymbolMetaData.Repetition rep,
            final int offset) {
        this(seq, token,parserRep,rep);
        this.offset = offset;
    }

    public ParserContext(
            final BNFSequence seq,
            final Token token,
            final ParserRepetition parserRep,
            final SymbolMetaData.Repetition rep,
            final int offset,
            final int minRepetition,
            final int maxRepetition) {
        this(seq, token,parserRep,rep,offset);
        this.minRepetition = minRepetition;
        this.maxRepetition = maxRepetition;
    }

    /**
     * constructor.
     *
     * @param seq
     *            -
     * @param token
     *            -
     * @param parserRep
     *            -
     * @param rep
     *            -
     */
    public ParserContext(final BNFSequence seq, final Token token,
                         final ParserRepetition parserRep, final Repetition rep) {
        this(seq, token);
        this.parserRepetition = parserRep;
        this.repetition = rep;
    }

    /**
     * @param token
     *            -
     */
    public void advanceToken(final Token token) {
        this.currentToken = token;
    }

    /**
     * reset token value to original.
     */
    public void resetToken() {
        this.currentToken = this.originalToken;
    }

    public Token getOriginalToken() {
        return originalToken;
    }

    /**
     * @return boolean
     */
    public boolean hasOrConditions() {
        return this.orConditions != null;
    }

    /**
     * @return boolean
     */
    public boolean hasAndConditions() {
        return this.andConditions != null;
    }

    /**
     * @return Token
     */
    public Token getCurrentToken() {
        return this.currentToken;
    }

    /**
     * @return BNFSequences
     */
    public List<BNFSequence> getOrConditions() {
        return this.orConditions;
    }

    /**
     * @return boolean
     */
    public boolean isComplete() {
        return this.isCompleteSequence() || isCompleteSymbol();
    }

    /**
     * @return BNFSequence
     */
    public BNFSequence getAndConditions() {
        return this.andConditions;
    }

    /**
     * @return HolderState
     */
    public ParserState getState() {
        return this.state;
    }

    /**
     * @param parserState -
     */
    public void setState(final ParserState parserState) {
        this.state = parserState;
    }

    @Override
    public String toString() {
        if (this.orConditions != null) {
            return this.orConditions.toString();
        }

        if (this.andConditions != null) {
            return this.andConditions.toString();
        }

        return "status " + this.state;
    }

    /**
     * @return ParserRepetition
     */
    public ParserRepetition getParserRepetition() {
        return this.parserRepetition;
    }

    /**
     * @param rep -
     */
    public void setParserRepetition(final ParserRepetition rep) {
        this.parserRepetition = rep;
    }

    /**
     * @return BNFSequence
     */
    public BNFSequence getNextOrCondition() {

        BNFSequence seq = null;
        int i = this.currentPosition + 1;

        if (i < this.getOrConditions().size()) {
            seq = this.getOrConditions().get(i);
            this.currentPosition = i;
        }

        return seq;
    }

    /**
     * @return boolean
     */
    public boolean isCompleteSequence() {
        return this.orConditions != null
                && this.currentPosition >= this.getOrConditions().size() - 1;
    }

    /**
     * @return boolean
     */
    public SymbolMetaData getNextAndConditionSymbol() {

        SymbolMetaData symbol = null;
        int i = this.currentPosition + 1;

        if (i < this.andConditions.getSymbols().size()) {
            symbol = this.andConditions.getSymbols().get(i);
            this.currentPosition = i;
        }

        return symbol;
    }

    /**
     * @return boolean
     */
    public boolean isCompleteSymbol() {
        return this.andConditions != null
                && this.currentPosition >= this.andConditions.getSymbols().size() - 1;
    }

    /**
     * @return Repetition
     */
    public Repetition getRepetition() {
        return this.repetition;
    }

    public void setRepetition(Repetition repetition) {
        this.repetition = repetition;
    }

    /**
     * reset parser position.
     */
    public void reset() {
        this.currentPosition = -1;
    }


    public void setCurrentPosition(int position){
        this.currentPosition = position;
    }

    public int getCurrentPosition(){
        return this.currentPosition;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public int getMinRepetition() {
        return minRepetition;
    }

    public void setMinRepetition(int minRepetition) {
        this.minRepetition = minRepetition;
    }

    public int getMaxRepetition() {
        return maxRepetition;
    }

    public void setMaxRepetition(int maxRepetition) {
        this.maxRepetition = maxRepetition;
    }

    public int getCurrentRepetition() {
        return currentRepetition;
    }

    public void setCurrentRepetition(int currentRepetition) {
        this.currentRepetition = currentRepetition;
    }

    public int getRecentRepetitionMatchCount() {
        return recentRepetitionMatchCount;
    }

    public void setRecentRepetitionMatchCount(int recentRepetitionMatchCount) {
        this.recentRepetitionMatchCount = recentRepetitionMatchCount;
    }

    public boolean isGoNextIfNoMatch() {
        return goNextIfNoMatch;
    }

    public void setGoNextIfNoMatch(boolean goNextIfNoMatch) {
        this.goNextIfNoMatch = goNextIfNoMatch;
    }


    public ParserContext copy(){
        ParserContext context =  new ParserContext();
        context.currentPosition = this.currentPosition;
        context.offset = this.offset;
        context.state = this.state;
        context.originalToken = this.originalToken;
        context.currentToken = this.currentToken;
        context.orConditions = this.orConditions;
        context.andConditions = this.andConditions;
        context.repetition = this.repetition;
        context.parserRepetition = this.parserRepetition;
        context.minRepetition = this.minRepetition;
        context.maxRepetition = this.maxRepetition;
        context.currentRepetition = this.currentRepetition;
        context.allowAskForUserWhenNoMatch = this.allowAskForUserWhenNoMatch;
        context.goNextIfNoMatch = this.goNextIfNoMatch;
        return context;
    }

}