package ca.gobits.bnf.tokenizer;

import java.util.Stack;

public class ABNFTokenizerFactoryImpl extends AbsTokenFactory {


    /**
     * @param text - string
     * @return Token - token;
     */
    @Override
    public Token tokens(final String text) {

        if (text == null){
            return null;
        }

        return tokens(text, new BNFTokenizerParams());
    }

    /**
     * @param text - string
     * @param params - BNFTokenizerParams
     * @return Token - token;
     */
    @Override
    public Token tokens(final String text, final BNFTokenizerParams params) {

        if (text == null){
            return null;
        }

        Stack<Token> stack = new Stack<Token>();
        BNFFastForward ff = new BNFFastForward();

        BNFTokenizerType lastType = BNFTokenizerType.NONE;

        int len = text.length();

        for (int i = 0; i < len; i++) {

            char c = text.charAt(i);
            BNFTokenizerType type = getType(c, lastType);

            if (ff.isActive()) {

                ff.appendIfActive(c);

                boolean isFastForwardComplete = ff.isComplete(type, lastType, i, len);

                if (isFastForwardComplete) {

                    finishFastForward(stack, ff);
                    ff.complete();
                }

            } else {

                calculateFastForward(ff, type, stack, lastType);

                if (ff.isActive()) {

                    ff.appendIfActive(c);

                } else if (includeText(type, params)) {

                    if (isAppendable(lastType, type)) {

                        stack.peek().appendValue(c);

                    } else {
                        addBNFToken(stack, type, c);
                    }
                }
            }

            lastType = type;
        }

        return !stack.isEmpty() ? stack.firstElement() : new Token("");
    }


    /**
     * @param ff -
     * @param type -
     * @param stack -
     * @param lastType -
     */
    private void calculateFastForward(
            final BNFFastForward ff,
            final BNFTokenizerType type,
            final Stack<Token> stack,
            final BNFTokenizerType lastType) {

        Token last = !stack.isEmpty() ? stack.peek() : null;
        ff.setStart(BNFTokenizerType.NONE);

        // single line comment
        if (lastType == BNFTokenizerType.SYMBOL_FORWARD_SLASH
                && type == BNFTokenizerType.SYMBOL_FORWARD_SLASH) {

            ff.setStart(BNFTokenizerType.COMMENT_SINGLE_LINE);
            ff.setEnd(new BNFTokenizerType[] {BNFTokenizerType.WHITESPACE_NEWLINE});

            Token token = stack.pop();
            ff.appendIfActive(token.getStringValue());

            // multi line comment
        } else if (lastType == BNFTokenizerType.SYMBOL_FORWARD_SLASH
                && type == BNFTokenizerType.SYMBOL_STAR) {

            ff.setStart(BNFTokenizerType.COMMENT_MULTI_LINE);
            ff.setEnd(new BNFTokenizerType[] {BNFTokenizerType.SYMBOL_FORWARD_SLASH, BNFTokenizerType.SYMBOL_STAR});

            Token token = stack.pop();
            ff.appendIfActive(token.getStringValue());

        } else if (type == BNFTokenizerType.QUOTE_DOUBLE) {

            ff.setStart(BNFTokenizerType.QUOTE_DOUBLE);
            ff.setEnd(BNFTokenizerType.QUOTE_DOUBLE);

        } else if (type == BNFTokenizerType.QUOTE_SINGLE && !isWord(last)) {

            ff.setStart(BNFTokenizerType.QUOTE_SINGLE);
            ff.setEnd(BNFTokenizerType.QUOTE_SINGLE);
        }
    }


    /**
     * @param stack -
     * @param ff -
     */
    private void finishFastForward(final Stack<Token> stack, final BNFFastForward ff) {

        if (isComment(ff.getStart())) {

            setNextToken(stack, null);

        } else {

            addBNFToken(stack, ff.getStart(), ff.getString());
        }
    }

    /**
     * @param stack -
     * @param type -
     * @param c -
     */
    private void addBNFToken(
            final Stack<Token> stack,
            final BNFTokenizerType type,
            final char c) {
        addBNFToken(stack, type, String.valueOf(c));
    }

    /**
     * @param stack -
     * @param type -
     * @param c -
     */
    private void addBNFToken(
            final Stack<Token> stack,
            final BNFTokenizerType type,
            final String c) {

        Token token = createBNFToken(c, type);

        if (!stack.isEmpty()) {
            Token peek = stack.peek();
            peek.setNextToken(token);
            token.setId(peek.getId() + 1);
            token.setPreviousToken(peek);
        } else {
            token.setId(1);
        }

        stack.push(token);
    }

    /**
     * @param stack -
     * @param nextToken -
     */
    private void setNextToken(final Stack<Token> stack, final Token nextToken) {
        if (!stack.isEmpty()) {
            stack.peek().setNextToken(nextToken);
        }
    }

    /**
     * @param lastType -
     * @param current -
     * @return boolean
     */
    private boolean isAppendable(
            final BNFTokenizerType lastType, final BNFTokenizerType current) {
        return lastType == current
                && (current == BNFTokenizerType.LETTER
                || current == BNFTokenizerType.NUMBER);
    }

    /**
     * @param value -
     * @param type -
     * @return Token
     */
    private Token createBNFToken(final String value, final BNFTokenizerType type) {

        Token token = new Token();
        token.setStringValue(value);

        if (isComment(type)) {
            token.setType(Token.TokenType.COMMENT);
        } else if (isNumber(type)) {
            token.setType(Token.TokenType.NUMBER);
        } else if (isLetter(type)) {
            token.setType(Token.TokenType.WORD);
        } else if (isSymbol(type)) {
            token.setType(Token.TokenType.SYMBOL);
        } else if (type == BNFTokenizerType.WHITESPACE_NEWLINE) {
            token.setType(Token.TokenType.WHITESPACE_NEWLINE);
        } else if (isWhitespace(type)) {
            token.setType(Token.TokenType.WHITESPACE);
        } else if (isQuote(type)) {
            token.setType(Token.TokenType.QUOTED_STRING);
        }
        return token;
    }
}
