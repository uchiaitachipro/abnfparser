package ca.gobits.bnf.extention;

import ca.gobits.bnf.parser.BNFSequence;
import ca.gobits.bnf.parser.ParserContext;
import ca.gobits.bnf.parser.SymbolMetaData;
import ca.gobits.bnf.tokenizer.Token;

import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class ParserContextPool {

    private static final int MIN_CAPACITY = 10;

    private volatile static ParserContextPool instance;

    private ConcurrentLinkedQueue<ParserContext> pool = new ConcurrentLinkedQueue<>();
    private AtomicInteger count = new AtomicInteger(0);

    private ParserContextPool() {
    }

    public static ParserContextPool getInstance() {
        if (instance == null) {
            synchronized (ParserContextPool.class) {
                if (instance == null) {
                    instance = new ParserContextPool();
                }
            }
        }
        return instance;
    }

    public ParserContext allocate(final BNFSequence sequence,
                                  final Token token,
                                  final ParserContext.ParserRepetition parserRepetition,
                                  final SymbolMetaData.Repetition repetition,
                                  final int offset,
                                  final int minRepetition,
                                  final int maxRepetition,
                                  final boolean goNextIfNoMatch) {
        ParserContext context = pool.poll();
        if (context == null || count.get() < MIN_CAPACITY) {
            count.addAndGet(1);
            return new ParserContext(
                    sequence,
                    token,
                    parserRepetition,
                    repetition,
                    offset,
                    minRepetition,
                    maxRepetition);
        }
        count.decrementAndGet();
        context.resetAll();
        context.setOriginalToken(token);
        context.setCurrentToken(token);
        context.setAndConditions(sequence);
        context.setParserRepetition(parserRepetition);
        context.setRepetition(repetition);
        context.setOffset(offset);
        context.setMinRepetition(minRepetition);
        context.setMaxRepetition(maxRepetition);
        context.setGoNextIfNoMatch(goNextIfNoMatch);
        return context;
    }

    public ParserContext allocate(List<BNFSequence> sd,
                         Token token,
                         ParserContext.ParserRepetition parserRepetition,
                         SymbolMetaData.Repetition rep){
        ParserContext context = pool.poll();
        if (context == null || count.get() < MIN_CAPACITY){
            count.addAndGet(1);
            return new ParserContext(sd,token,parserRepetition,rep);
        }
        count.decrementAndGet();
        context.resetAll();
        context.setOriginalToken(token);
        context.setCurrentToken(token);
        context.setOrConditions(sd);
        context.setParserRepetition(parserRepetition);
        context.setRepetition(rep);
        context.setState(ParserContext.ParserState.NONE);
        return context;
    }

    public ParserContext allocate(ParserContext.ParserState state) {
        ParserContext context = pool.poll();
        if (context == null || count.get() < MIN_CAPACITY) {
            count.addAndGet(1);
            return new ParserContext(state);
        }
        count.decrementAndGet();
        context.resetAll();
        context.setState(state);
        context.setParserRepetition(ParserContext.ParserRepetition.NONE);
        return context;
    }

    public void clear() {
        pool.clear();
    }

    public void recycle(ParserContext context) {
        count.addAndGet(1);
        pool.add(context);
    }

}
