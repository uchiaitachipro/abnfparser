package ca.gobits.bnf.parser;

import ca.gobits.bnf.extention.PlaceHolderRegister;
import ca.gobits.bnf.tokenizer.BNFTokenizerFactoryImpl;
import ca.gobits.bnf.tokenizer.MultiEntranceRecorder;
import ca.gobits.bnf.tokenizer.Token;
import ca.gobits.bnf.tokenizer.TokenizerFactory;

import java.util.*;
import java.util.logging.Logger;
import java.util.regex.Pattern;

public class ABNFParserImpl<T> implements IParser<T> {

    private static final String WILDCARD_SYMBOL = "$";

    private static final String PLACE_HOLDER = "#placeholder#";

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
    private Stack<ParserContext> stack = new Stack<ParserContext>();

    /**
     * IParser Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(IParser.class.getName());

    /**
     * constructor.
     *
     * @param map -
     */

    private String parsingString = null;

    private List<ResultNode> matchWords = new LinkedList<>();

    private T tag;

    private IParseResult wildcardResult = null;

    public ABNFParserImpl(final Map<String, List<BNFSequence>> map) {
        this.sequenceMap = map;
    }

    @Override
    public T getTag() {
        return tag;
    }

    @Override
    public void setTag(T tag) {
        this.tag = tag;
    }

    @Override
    public IParseResult parse(final String string) {
        TokenizerFactory tokenizer = new BNFTokenizerFactoryImpl();
        Token token = tokenizer.tokens(string);
        return parse(token, string);
    }

    @Override
    public IParseResult parse(final Token token, String rawContent) {

        if (token == null) {
            IParseResultImpl result = new IParseResultImpl();
            result.setTop(new Token());
            return result;
        }

        parsingString = rawContent;

        List<BNFSequence> sd = this.sequenceMap.get("@start");

        // 允许多入口
        Iterable<String> starter = MultiEntranceRecorder.getInstance().getEntrances();
        if (sd == null && starter != null) {
            for (String rule : starter) {
                List<BNFSequence> sentence = this.sequenceMap.get(rule);
                if (sentence == null) {
                    continue;
                }
                this.parsingString = rawContent;
                this.stack = new Stack<>();
                addParserState(
                        sentence,
                        token,
                        ParserContext.ParserRepetition.NONE,
                        SymbolMetaData.Repetition.NONE,
                        0,
                        0,
                        0,
                        false);
                IParseResultImpl result = (IParseResultImpl) parseSequences(token);

                if (result != null && result.isSuccess()) {
                    tag = (T) MultiEntranceRecorder.getInstance().getOptionalInfo(rule);
                    this.matchWords.clear();
                    this.wildcardResult = null;
                    return result;
                }
                this.wildcardResult = null;
                this.matchWords.clear();
            }

        } else {
            addParserState(
                    sd,
                    token,
                    ParserContext.ParserRepetition.NONE,
                    SymbolMetaData.Repetition.NONE,
                    0,
                    0,
                    0,
                    false);

            return parseSequences(token);
        }

        return null;
    }

    /**
     * Main loop for parsing.
     *
     * @param startToken -
     * @return IParseResultImpl
     */
    private IParseResult parseSequences(final Token startToken) {

        boolean success = false;

        IParseResultImpl result = new IParseResultImpl();
        result.setTop(startToken);

        while (!this.stack.isEmpty()) {
            ParserContext holder = this.stack.peek();

            if (holder.getState() == ParserContext.ParserState.NO_MATCH_WITH_ZERO_REPETITION) {

                processNoMatchWithZeroRepetition();

            } else if (holder.getState() == ParserContext.ParserState.MATCH_WITH_ZERO_REPETITION) {

                processMatchWithZeroRepetition();

            } else if (holder.getState() == ParserContext.ParserState
                    .NO_MATCH_WITH_ZERO_REPETITION_LOOKING_FOR_FIRST_MATCH) {

                processNoMatchWithZeroRepetitionLookingForFirstMatch();

            } else if (holder.getState() == ParserContext.ParserState.MATCH) {

                // 通配符匹配成功直接返回结果
                if (wildcardResult != null && wildcardResult.isSuccess()) {
                    this.stack.pop();
                    return wildcardResult;
                }

                processMatch();
                success = true;

            } else if (holder.getState() == ParserContext.ParserState
                    .MATCH_WITH_ONE_OR_MORE_REPETITION_PARTIALLY) {

                processMatchOneOrMoreInSequence();

            } else if (holder.getState() == ParserContext.ParserState.NO_MATCH) {

                processNoMatch();
                success = false;

            } else if (holder.getState() == ParserContext.ParserState
                    .MATCH_IN_SEQUENCE_PARTICALLY) {
                processMatchPartiallyInSequence();
            } else if (holder.getState() == ParserContext.ParserState
                    .MATCH_REPETITION_FINISHED_LOOKING_FOR_NEXT) {
                processAllRepetitionForNext();

                // 处理所有已经匹配结束的符号
                if (!this.stack.isEmpty()) {
                    int offset = this.stack.peek().getOffset();
                    while (!this.stack.isEmpty()) {
                        ParserContext context = this.stack.peek();
                        if (!context.isComplete()) {
                            break;
                        }
                        this.stack.pop();
                    }

                    if (this.stack.isEmpty()) {
                        success = true;
                        break;
                    } else {
                        this.stack.peek().setOffset(offset);
                    }

                }

                // 如果Repetition处于最后并且完全匹配则
                if (this.stack.size() == 1) {
                    ParserContext finalContext = this.stack.peek();
                    if (finalContext.isComplete() && finalContext.getOffset() == parsingString.length()) {
                        this.stack.clear();
                        if (finalContext.getCurrentRepetition() >= finalContext.getMaxRepetition()) {
                            success = true;
                        } else {
                            success = false;
                        }
                        break;
                    }
                }


            } else {
                processStack();
            }
        }

        updateResult(result, success);
        parsingString = null;
        return result;
    }

    /**
     * Update BNFParserResult.
     *
     * @param result  -
     * @param success -
     */
    private void updateResult(
            final IParseResultImpl result,
            final boolean success) {
        result.setMatchResult(new LinkedList<>(matchWords));
        result.setSuccess(success);

        matchWords.clear();
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
    private void processNoMatch() {

        debugPrintIndents();
        LOGGER.finer("-> no match, rewinding to next sequence");

        this.stack.pop();

        while (!this.stack.isEmpty()) {
            ParserContext holder = this.stack.peek();
            if (holder.hasOrConditions() && !holder.isComplete()) {
                break;
            }

            // 如果当前字母没有匹配上，则匹配下一个字符
            // 如果当前token匹配结束，next不为空则匹配下一个token
            if (holder.isGoNextIfNoMatch()) {

                holder.setOffset(holder.getOffset() + 1);
                holder.reset();
                break;
            }

            ParserContext popContext = this.stack.pop();
            if (popContext.getRepetition() != SymbolMetaData.Repetition.NONE) {
                checkRepetitionTimesLessThanMinTimes(popContext);
            }
        }
    }

    /**
     * @return Token
     */
    private void processMatchWithZeroRepetition() {
        this.stack.pop();
        debugPrintIndents();
        rewindToOutsideOfRepetition();
    }

    /**
     * @return Token
     */
    private void processNoMatchWithZeroRepetitionLookingForFirstMatch() {

        this.stack.pop();

        while (!this.stack.isEmpty()) {
            ParserContext holder = this.stack.peek();

            if (holder.getRepetition() != SymbolMetaData.Repetition.NONE
                    && holder.hasOrConditions()
                    && !holder.isComplete()) {
                break;
            }

            // Repetition匹配了部分，需要回退到通配符匹配的起点
            if (holder.getRepetition() != SymbolMetaData.Repetition.NONE
                    && holder.hasAndConditions()
                    && holder.isComplete()) {


                this.stack.pop();
                this.stack.peek().setOffset((holder.getOffset())
                        - (holder.getCurrentAndConditionMatchIndex() - 1));

                // 检查Repetition次数是否少于最小次数
                checkRepetitionTimesLessThanMinTimes(holder);
                break;
            }

            if (holder.getParserRepetition() != ParserContext.ParserRepetition.NONE) {
                this.stack.pop();

                if (holder.getRepetition() != SymbolMetaData.Repetition.NONE) {
                    // 重新修正已匹配成功的字符，需要除去Repetition不完全匹配的offset
                    // 让下一个规则去重新匹配
                    this.stack.peek().setOffset(holder.getOffset());
                }

            } else {
                break;
            }
        }

        while (!this.stack.isEmpty()) {
            ParserContext holder = this.stack.peek();

            if (holder.hasAndConditions() && !holder.isComplete()) {
                break;
            }

            if (holder.getRepetition() != SymbolMetaData.Repetition.NONE
                    && !holder.isComplete()) {
                break;
            }

            this.stack.pop();

            // 当Repetition不匹配时，需要将已匹配的字符串传递给下一个规则
            if (!this.stack.isEmpty() && holder.hasAndConditions() && holder.isComplete()) {
                this.stack.peek().setOffset(holder.getOffset());
            }
        }
    }

    /**
     * Rewind stack to next symbol.
     *
     * @return Token
     */
    private void processMatch() {

        this.stack.pop();


        debugPrintIndents();

        rewindToNextSymbolOrRepetition();

        if (!this.stack.isEmpty()) {
            ParserContext holder = this.stack.peek();

            // 再次检查@care修饰的关键词在匹配到最后之后，是否还有必要的条件待匹配，排除可以重复0-n的
            if (holder.isGoNextIfNoMatch()
                    && holder.hasAndConditions()) {

                while (!holder.isComplete()) {
                    SymbolMetaData metaData = holder
                            .getAndConditions()
                            .getSymbols()
                            .get(holder.getCurrentPosition() + 1);

                    if (metaData.getMaxRepetitionTimes() > 0 && metaData.getMinRepetitionTimes() == 0) {
                        holder.getNextAndConditionSymbol();
                    } else {
                        break;
                    }
                }

                while (!this.stack.isEmpty()) {
                    if (!this.stack.peek().isComplete()) {
                        break;
                    }
                    this.stack.pop();
                }
            }
        }

    }

    private void processAllRepetitionForNext() {
        this.stack.pop();

        while (!this.stack.isEmpty()) {
            ParserContext holder = this.stack.peek();

            // Repetition匹配了部分，需要回退到通配符匹配的起点
            if (holder.getRepetition() != SymbolMetaData.Repetition.NONE
                    && holder.hasAndConditions()
                    && !holder.isComplete()) {
                this.stack.pop();
                this.stack.peek().setOffset(holder.getOffset() - (holder.getCurrentAndConditionMatchIndex() - 1));
                break;
            }

            // Repetition完全匹配，并且字符串已经匹配结束，需要退出Repetition。结束匹配
            if (holder.getParserRepetition() != ParserContext.ParserRepetition.NONE) {
                this.stack.pop();
                if (holder.getRepetition() != SymbolMetaData.Repetition.NONE) {
                    holder.setCurrentRepetition(holder.getCurrentRepetition() + 1);
                    checkRepetitionTimesMoreThanMaxTimes(holder);
                    this.stack.peek().setOffset(holder.getOffset());
                    checkRepetitionTimesLessThanMinTimes(holder);
                }

            } else {
                break;
            }

        }

    }

    private void processMatchOneOrMoreInSequence() {

        this.stack.pop();
        ParserContext state = this.stack.peek();
        int offset = state.getOffset();

        // 加速确定数量通配符的匹配，直接将offset移至下一个规则
        boolean isWildcardSymbol = state.isComplete()
                && state.getAndConditions().getSymbols().get(0).getName().equals(WILDCARD_SYMBOL);
        if (isWildcardSymbol
                && state.getMinRepetition() == state.getMaxRepetition()
                && state.getMinRepetition() > 0) {
            offset += (state.getMinRepetition() - 1);
            state.setCurrentRepetition(state.getMinRepetition() - 1);
        }

        while (!this.stack.isEmpty()) {
            ParserContext holder = this.stack.peek();

            if (holder.isComplete()
                    && holder.getAndConditions() != null
                    && holder.getAndConditions().getSymbols().get(0).getName().equals(WILDCARD_SYMBOL)
                    && (holder.getCurrentRepetition() == holder.getMaxRepetition() - 1)) {
                this.stack.pop();
                ParserContext parent = this.stack.peek();

                // 如果通配符有父符号则其也要推出堆
                if (parent.isComplete() && parent.getAndConditions().getSymbols()
                        .get(parent.getCurrentPosition()).getName().equals(
                                holder.getAndConditions().getSymbols().get(0).getParentSymbol())) {
                    this.stack.pop();
                }

                break;
            }

            if (holder.getRepetition() != SymbolMetaData.Repetition.NONE) {

                int maxRepetitionCount = holder.getMaxRepetition() - 1;
                maxRepetitionCount = maxRepetitionCount >= 0 ? maxRepetitionCount : 0;
                if (holder.getCurrentRepetition() < maxRepetitionCount) {
                    break;
                }
            }

            // 如果 and 条件并没有匹配结束，继续匹配 and 条件,
            if (holder.hasAndConditions() && !holder.isComplete()) {
                break;
            }

            this.stack.pop();

        }

        if (!this.stack.isEmpty()) {
            ParserContext holder = this.stack.peek();

            // 统计 形如(r1 r2)* 的重复次数
            if (holder.getRepetition() != SymbolMetaData.Repetition.NONE
                    && holder.hasAndConditions()
                    && holder.isComplete()) {

                // 只获取重复0-1次
                if (holder.getMaxRepetition() <= 1) {
                    recordMatchWords(holder);
                }


                holder.setCurrentRepetition(holder.getCurrentRepetition() + 1);
                checkRepetitionTimesMoreThanMaxTimes(holder);
            }

            // 统计形如 (r1 | r2)* 的重复次数
            if (holder.getRepetition() != SymbolMetaData.Repetition.NONE
                    && holder.hasOrConditions()) {

//                // 只获取重复0-1次
//                if (holder.getMaxRepetition() <= 1) {
//                    recordMatchWords(state);
//                }

                holder.setCurrentRepetition(holder.getCurrentRepetition() + 1);
                checkRepetitionTimesMoreThanMaxTimes(holder);
            }

            if (holder.getParserRepetition() != ParserContext.ParserRepetition.NONE
                    && (holder.getCurrentRepetition() < (holder.getMaxRepetition() - 1))
                    && holder.isComplete()) {
                holder.reset();
            }

            holder.setOffset(offset);
        }
    }

    private int countAllSymbolLength(List<BNFSequence> data) {

        int length = 0;

        if (data == null) {
            return length;
        }

        for (BNFSequence s : data) {

            if (s == null) {
                continue;
            }

            for (SymbolMetaData metaData : s.getSymbols()) {

                List<BNFSequence> childSequnce = this.sequenceMap.get(metaData.getName());

                if (childSequnce == null) {
                    length += metaData.getName().length();
                } else {
                    length += countAllSymbolLength(childSequnce);
                }

            }
        }


        return length;
    }

    private boolean isLeafNode(ParserContext context) {

        if (context == null || !context.isComplete()) {
            return false;
        }

        if (context.getAndConditions() == null) {
            return false;
        }

        List<SymbolMetaData> symbols = context.getAndConditions().getSymbols();
        if (symbols.size() == 1) {
            return this.sequenceMap.get(symbols.get(0).getName()) == null;
        }
        return false;
    }

    private boolean isPlaceHolder(ParserContext context) {
        if (context == null) {
            return false;
        }

        if (context.getAndConditions() == null) {
            return false;
        }

        List<SymbolMetaData> symbolMetaData = context.getAndConditions().getSymbols();
        if (symbolMetaData.size() == 1) {
            SymbolMetaData data = symbolMetaData.get(0);
            return data.isPlaceholder();
        }

        return false;
    }

    private String getPlaceHolderValue(ParserContext context) {
        SymbolMetaData symbol = context.getAndConditions().getSymbols().get(0);
        return PlaceHolderRegister.getInstance().getPlaceholderRealValue(symbol.getPlaceholderLabel());
    }

    private void checkRepetitionTimesMoreThanMaxTimes(ParserContext context) {

        if (context.getCurrentRepetition() <= context.getMaxRepetition()) {
            return;
        }

        this.stack.pop();
        this.stack.add(new ParserContext(ParserContext.ParserState.NO_MATCH));

    }

    private void checkRepetitionTimesLessThanMinTimes(ParserContext context) {

        if (context.getCurrentRepetition() >= context.getMinRepetition()) {
            return;
        }

        this.stack.add(new ParserContext(ParserContext.ParserState.NO_MATCH));

    }

    private void processMatchPartiallyInSequence() {
        this.stack.pop();
        ParserContext state = this.stack.peek();

        // 统计匹配的叶子节点以及其对应的父节点的符号
        recordMatchWords(state);

        int offset = state.getOffset();
        while (!this.stack.isEmpty()) {
            ParserContext holder = this.stack.peek();

            if (holder.hasAndConditions() && !holder.isComplete()) {
                break;
            }

            ParserContext popContext = this.stack.pop();
            if (popContext.getRepetition() != SymbolMetaData.Repetition.NONE) {
                checkRepetitionTimesLessThanMinTimes(popContext);
            }
        }

        if (!this.stack.isEmpty()) {
            ParserContext holder = this.stack.peek();
            holder.setOffset(offset);
        }
    }

    private void recordMatchWords(ParserContext context) {

        int currentPosition = context.getCurrentPosition();

        // 统计占位符内容,他的ResultNode中的key不再是表达式，而是对应的label
        if (context.hasAndConditions()) {
            SymbolMetaData metaData = context.getAndConditions()
                    .getSymbols()
                    .get(currentPosition);
            if (metaData.getName().equals(PLACE_HOLDER)) {

                String value = PlaceHolderRegister.getInstance().getPlaceholderRealValue(
                        metaData.getPlaceholderLabel());

                if (value == null) {
                    return;
                }

                matchWords.add(
                        new ResultNode(
                                metaData.getPlaceholderLabel(),
                                value,
                                context.getOffset() - value.length()));
                return;
            }
        }

        if (!isLeafNode(context)) {
            return;
        }

        if (context.isComplete() && context.hasAndConditions()) {
            List<BNFSequence> sequences = new ArrayList<>();
            sequences.add(context.getAndConditions());
            int count = countAllSymbolLength(sequences);
            int startIndex = context.getOffset() - count;
            int endIndex = context.getOffset();

            if (startIndex >= 0 && endIndex < parsingString.length()) {

                String symbol = getSymbolTypeOrUUID(context.getAndConditions());
                ResultNode node = new ResultNode(
                        symbol,
                        parsingString.substring(startIndex, endIndex),
                        startIndex);
                matchWords.add(node);
            }
        }
    }

    /**
     * processNoMatchWithZeroRepetition.
     */
    private void processNoMatchWithZeroRepetition() {

//        debugPrintIndents();
//        LOGGER.finer("-> " + ParserContext.ParserState.NO_MATCH_WITH_ZERO_REPETITION
//                + ", rewind to next symbol");

        this.stack.pop();

//        Token token = this.stack.peek().getCurrentToken();

        rewindToNextSymbol();

//        if (!this.stack.isEmpty()) {
//            ParserContext holder = this.stack.peek();
//            holder.advanceToken(token);
//        }
    }

    /**
     * rewindToOutsideOfRepetition.
     */
    private void rewindToOutsideOfRepetition() {

        while (!this.stack.isEmpty()) {
            ParserContext holder = this.stack.peek();

            if (holder.getParserRepetition() != ParserContext.ParserRepetition.NONE) {
                this.stack.pop();
                checkRepetitionTimesLessThanMinTimes(holder);
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
                    holder.setParserRepetition(
                            ParserContext.ParserRepetition.ZERO_OR_MORE_LOOKING_FOR_FIRST_MATCH);
                }
                break;
            } else if (holder.hasAndConditions() && !holder.isComplete()) {
                if (holder.getParserRepetition() == ParserContext.ParserRepetition
                        .ZERO_OR_MORE_LOOKING_FOR_FIRST_MATCH) {
                    holder.setParserRepetition(
                            ParserContext.ParserRepetition.NONE);
                }

                break;
            }

            recordMatchWordsInEnd(holder);
            if (parsingString.length() == 1 && holder.isComplete()) {
                this.stack.clear();
                break;
            }

            ParserContext popContext = this.stack.pop();
            if (!this.stack.isEmpty()) {
                this.stack.peek().setOffset(popContext.getOffset());
            }
        }
    }

    private void recordMatchWordsInEnd(ParserContext holder) {
        if (isLeafNode(holder)) {

            if (holder.getOffset() == parsingString.length()
                    && holder.isComplete()) {

                if (isPlaceHolder(holder)) {
                    String value = getPlaceHolderValue(holder);
                    String label = holder.getAndConditions()
                            .getSymbols()
                            .get(0)
                            .getPlaceholderLabel();
                    matchWords.add(
                            new ResultNode(label, value, holder.getOffset() - value.length()));
                } else {

                    List<BNFSequence> sequences = new ArrayList<>();
                    sequences.add(holder.getAndConditions());
                    int count = countAllSymbolLength(sequences);

                    int startIndex = holder.getOffset() - count;
                    int endIndex = holder.getOffset();

                    // 这里能取等的原因是offset 始终指向下一个字符串
                    if (startIndex >= 0 && endIndex <= parsingString.length()) {
                        matchWords.add(new ResultNode(
                                getSymbolTypeOrUUID(holder.getAndConditions()),
                                parsingString.substring(startIndex, endIndex), startIndex));
                    }
                }
            } else {
                if (parsingString.length() == 1 && holder.isComplete()) {
                    if (isPlaceHolder(holder)) {
                        String label = holder.getAndConditions()
                                .getSymbols()
                                .get(0)
                                .getPlaceholderLabel();
                        matchWords.add(new ResultNode(label, parsingString, 0));
                    } else {
                        matchWords.add(new ResultNode(
                                getSymbolTypeOrUUID(holder.getAndConditions()),
                                parsingString.substring(0, 1), 0));
                    }
                }
            }
        }
    }

    private String getSymbolTypeOrUUID(BNFSequence sequence) {
        StringBuilder sb = new StringBuilder();

        if (sequence == null || sequence.getSymbols() == null) {
            return UUID.randomUUID().toString();
        }

        for (SymbolMetaData symbol : sequence.getSymbols()) {
            sb.append(symbol.getParentSymbol());
        }

        return sb.toString();
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

            ParserContext popContext = this.stack.pop();
            if (popContext.getRepetition() != SymbolMetaData.Repetition.NONE) {
                checkRepetitionTimesLessThanMinTimes(popContext);
            }
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

            ParserContext popContext = this.stack.pop();
            if (popContext.getRepetition() != SymbolMetaData.Repetition.NONE) {
                checkRepetitionTimesLessThanMinTimes(popContext);
            }
        }
    }

    /**
     * processStack.
     */
    private void processStack() {

        ParserContext holder = this.stack.peek();

        // 关键词若是到字符串末尾还是不匹配，需要结束匹配
        if (holder.isGoNextIfNoMatch() && holder.getOffset() >= parsingString.length()) {
            this.stack.clear();
            addParserState(
                    holder.getAndConditions(),
                    holder.getCurrentToken(),
                    ParserContext.ParserRepetition.NONE,
                    SymbolMetaData.Repetition.NONE,
                    holder.getOffset(),
                    0, 0, false);
            addParserState(ParserContext.ParserState.NO_MATCH);

            return;
        }

        if (holder.isComplete()) {
            ParserContext popContext = this.stack.peek();

            // 如果这个符号能重复多次，则先让它进到下一轮循环
            if (popContext.getCurrentRepetition() < popContext.getMaxRepetition()) {
                holder.reset();
                return;
            }
            this.stack.pop();
            if (popContext != null && !this.stack.isEmpty()) {
                this.stack.peek().setOffset(popContext.getOffset());
            }
        } else {

            Token currentToken = holder.getCurrentToken();

            if (holder.hasOrConditions()) {

                BNFSequence sequence = holder.getNextOrCondition();
                addParserState(
                        sequence,
                        currentToken,
                        holder.getParserRepetition(),
                        SymbolMetaData.Repetition.NONE,
                        holder.getOffset(), 0, 0, false);
            } else if (holder.hasAndConditions()) {

                SymbolMetaData symbol = holder.getNextAndConditionSymbol();
                List<BNFSequence> sd = this.sequenceMap.get(symbol.getName());

                ParserContext.ParserRepetition repetition = getParserRepetition(holder, symbol);


                if (handleUncertainWildcard(holder)) {
                    return;
                }

                if (sd != null) {
                    addParserState(
                            sd,
                            currentToken,
                            repetition,
                            symbol.getRepetition(),
                            holder.getOffset(),
                            symbol.getMinRepetitionTimes(),
                            symbol.getMaxRepetitionTimes(),
                            symbol.isGoNextIfNoMatch());

                } else {
                    ParserContext.ParserState state = getParserState(
                            holder,
                            symbol,
                            repetition);
                    addParserState(state);
                }

            }
        }
    }


    private boolean handleUncertainWildcard(ParserContext context) {

        if (context.getAndConditions() == null || context.getAndConditions().getSymbols() == null) {
            return false;
        }

        SymbolMetaData metaData = context.getAndConditions().getSymbols().get(0);

        // 如果通配符最少要求匹配n次，那么这个是不能跳过去比较下一个条件的
        if (!metaData.getName().equals(WILDCARD_SYMBOL)
                || context.getCurrentRepetition() < context.getMinRepetition()) {
            return false;
        }

        int needToCheckMatchCount = parsingString.length() - context.getOffset();

        // 检查通配符的数量是否多于待匹配字符的数量
        if (needToCheckMatchCount <= context.getMaxRepetition()) {
            IParseResultImpl result = new IParseResultImpl();
            result.setMatchResult(new LinkedList<>(matchWords));
            result.setError(null);
//            result.setMaxMatchToken(context.getCurrentToken());
            result.setSuccess(true);
            this.wildcardResult = result;
            this.stack.clear();
            addParserState(ParserContext.ParserState.MATCH);
            return true;
        }

        // 一般来说不会出现这种情况，若出现，则交给上层匹配
        if (context.getCurrentRepetition() >= context.getMaxRepetition()) {
            return false;
        }

        //让通配符出栈，重新开始匹配
        this.stack.pop();
        int backupSymbolIndex = this.stack.peek().getCurrentPosition();
        SymbolMetaData backupRepetitionSymbol = this.stack.peek().getAndConditions()
                .getSymbols().get(backupSymbolIndex).copy();
        Stack<ParserContext> backupStack = copyStackFully((Stack<ParserContext>) this.stack.clone());
        LinkedList<ResultNode> backupNode = new LinkedList<>(matchWords);
        String backupParingString = new String(this.parsingString);
        for (int i = context.getCurrentRepetition(); i <= context.getMaxRepetition(); i++) {

            if (i > 0) {
                SymbolMetaData data = this.stack.peek().getAndConditions()
                        .getSymbols().get(this.stack.peek().getCurrentPosition());
                data.setMinRepetitionTimes(i);
                data.setMaxRepetitionTimes(i);
                this.stack.peek().setCurrentPosition(this.stack.peek().getCurrentPosition() - 1);
            }


            IParseResult result = parseSequences(context.getCurrentToken());

            if (result != null && result.isSuccess()) {
                wildcardResult = result;
                addParserState(ParserContext.ParserState.MATCH);
                backupStack.peek().getAndConditions().getSymbols().set(backupSymbolIndex, backupRepetitionSymbol);
                return true;
            }

            this.stack = copyStackFully((Stack<ParserContext>) backupStack.clone());
            this.matchWords = new LinkedList<>(backupNode);
            this.parsingString = backupParingString;
        }
        backupStack.peek().getAndConditions().getSymbols().set(backupSymbolIndex, backupRepetitionSymbol);
        addParserState(ParserContext.ParserState.NO_MATCH);
        return true;
    }

    private Stack<ParserContext> copyStackFully(Stack<ParserContext> stack) {
        LinkedList<ParserContext> list = new LinkedList<>();

        while (!stack.isEmpty()) {
            ParserContext context = stack.pop().copy();
            list.addLast(context);
        }

        Collections.reverse(list);
        Stack<ParserContext> copyStack = new Stack<>();
        for (ParserContext context : list) {
            copyStack.push(context);
        }
        return copyStack;
    }


    /**
     * Gets the IParser State.
     *
     * @param holder     -
     * @param symbol     -
     * @param repetition -
     * @return ParserState
     */
    private ParserContext.ParserState getParserState(
            final ParserContext holder,
            final SymbolMetaData symbol,
            final ParserContext.ParserRepetition repetition) {

        ParserContext.ParserState state = ParserContext.ParserState.NO_MATCH;

        String symbolName = symbol.getName();

//        if (symbolName.equals("Empty")) {
//
//            state = ParserContext.ParserState.EMPTY;
//
//        }
//        else if (isMatch(symbolName, token)) {
//
//            state = ParserContext.ParserState.MATCH;
//
//        }
        if (repetition == ParserContext.ParserRepetition.ZERO_OR_MORE) {

            state = ParserContext.ParserState.NO_MATCH_WITH_ZERO_REPETITION;

        } else if (repetition == ParserContext.ParserRepetition
                .ZERO_OR_MORE_LOOKING_FOR_FIRST_MATCH) {

            boolean finded = false;

            if (isMatchPartiallyInSequence(holder, symbol)) {
                finded = true;
            }

            state = finded ? ParserContext.ParserState.MATCH_WITH_ONE_OR_MORE_REPETITION_PARTIALLY
                    : ParserContext.ParserState.NO_MATCH_WITH_ZERO_REPETITION_LOOKING_FOR_FIRST_MATCH;

            if (holder.getOffset() == parsingString.length()) {
                state = ParserContext.ParserState.MATCH_REPETITION_FINISHED_LOOKING_FOR_NEXT;
            }


        } else if (isMatchPartiallyInSequence(holder, symbol)) {

            state = holder.getOffset() == parsingString.length()
                    ? ParserContext.ParserState.MATCH
                    : ParserContext.ParserState.MATCH_IN_SEQUENCE_PARTICALLY;

        }

        return state;
    }


//    /**
//     * @param symbolName -
//     * @param token      -
//     * @return boolean
//     */
//    private boolean isMatch(final String symbolName, final Token token) {
//
//        boolean match = false;
//
//        if (token != null) {
//            String s = isQuotedString(symbolName)
//                    ? symbolName.substring(1, symbolName.length() - 1)
//                    : symbolName;
//            match = s.equals(token.getStringValue()) || isQuotedString(symbolName, token)
//                    || isNumber(symbolName, token);
//        }
//
//        return match;
//    }

    private boolean isMatchPartiallyInSequence(
            final ParserContext holder,
            SymbolMetaData symbol) {

        boolean match = false;

//        if (token != null) {
        String symbolName = symbol.getName();

        if (symbolName.equals(PLACE_HOLDER)) {

            PlaceHolderRegister.ICallback callback = PlaceHolderRegister
                    .getInstance()
                    .getPlaceholderCallback(symbol.getPlaceholderLabel());

            if (callback == null) {
                return false;
            }

            Collection<String> names = callback.getValue(symbol.getPlaceholderLabel());

            if (names != null) {
                for (String name : names) {
                    if (parsingString.contains(name)) {
                        symbolName = name;
                        PlaceHolderRegister.getInstance().savePlaceholderValue(
                                symbol.getPlaceholderLabel(),
                                name);
                    }
                }
            }
        }

        String s = isQuotedString(symbolName)
                ? symbolName.substring(1, symbolName.length() - 1)
                : symbolName;

        if ((s.length() + holder.getOffset()) > parsingString.length()) {
            return false;
        }

        match = s.equals(parsingString.substring(
                holder.getOffset(),
                holder.getOffset() + s.length()));

        if (s.equals(WILDCARD_SYMBOL)) {
            match = true;
        }

        if (match) {
            holder.setOffset(holder.getOffset() + s.length());
        }
//        }

        return match;
    }

    /**
     * @param value -
     * @return boolean
     */
    private boolean isQuotedString(final String value) {
        return (value.startsWith("\"") && value.endsWith("\""))
                || value.startsWith("'") && value.endsWith("'");
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
    private void addParserState(final ParserContext.ParserState state) {
        this.stack.push(new ParserContext(state));
    }

    /**
     * @param sequences        -
     * @param token            -
     * @param parserRepetition -
     * @param repetition       -
     */
    private void addParserState(final List<BNFSequence> sequences,
                                final Token token,
                                final ParserContext.ParserRepetition parserRepetition,
                                final SymbolMetaData.Repetition repetition,
                                final int offset,
                                final int minRepetition,
                                final int maxRepetition,
                                boolean goNextIfNoMatch) {
        if (sequences.size() == 1) {
            addParserState(
                    sequences.get(0),
                    token,
                    parserRepetition,
                    repetition,
                    offset,
                    minRepetition,
                    maxRepetition,
                    goNextIfNoMatch);
        } else {
            debug(sequences, token, parserRepetition);
            ParserContext state = new ParserContext(
                    sequences,
                    token,
                    parserRepetition,
                    repetition);
            state.setOffset(offset);
            state.setMinRepetition(minRepetition);
            state.setMaxRepetition(maxRepetition);
            state.setGoNextIfNoMatch(goNextIfNoMatch);
            this.stack.push(state);
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
            final ParserContext.ParserRepetition parserRepetition,
            final SymbolMetaData.Repetition repetition,
            final int offset,
            final int minRepetition,
            final int maxRepetition,
            final boolean goNextIfNoMatch) {
        debug(sequence, token, parserRepetition);

        ParserContext context = new ParserContext(
                sequence,
                token,
                parserRepetition,
                repetition,
                offset,
                minRepetition,
                maxRepetition);
        context.setGoNextIfNoMatch(goNextIfNoMatch);

        this.stack.push(context);
    }


    /**
     * @param holder -
     * @param symbol -
     * @return ParserRepetition
     */
    private ParserContext.ParserRepetition getParserRepetition(
            final ParserContext holder,
            final SymbolMetaData symbol) {

        SymbolMetaData.Repetition symbolRepetition = symbol.getRepetition();
        ParserContext.ParserRepetition holderRepetition = holder.getParserRepetition();

        if (symbolRepetition != SymbolMetaData.Repetition.NONE
                && holderRepetition == ParserContext.ParserRepetition.NONE) {
            holderRepetition = ParserContext.ParserRepetition.ZERO_OR_MORE_LOOKING_FOR_FIRST_MATCH;
        } else if (symbolRepetition != SymbolMetaData.Repetition.NONE
                && holderRepetition != ParserContext.ParserRepetition.NONE) {
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
    private void debug(
            final BNFSequence sequence,
            final Token token,
            final ParserContext.ParserRepetition repetition) {
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
    private void debug(
            final List<BNFSequence> sd,
            final Token token,
            final ParserContext.ParserRepetition repetition) {
        debugPrintIndents();
        LOGGER.finer("-> adding pipe lines " + sd
                + " for token " + debug(token) + " with repetition "
                + repetition);
    }

}
