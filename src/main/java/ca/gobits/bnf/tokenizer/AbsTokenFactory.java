package ca.gobits.bnf.tokenizer;

public abstract class AbsTokenFactory implements TokenizerFactory {

    /**
     * CHARACTER_SYMBOL29.
     */
    protected static final int CHARACTER_SYMBOL29 = 0xFFFF;
    /**
     * CHARACTER_SYMBOL28.
     */
    protected static final int CHARACTER_SYMBOL28 = 0xFF00;
    /**
     * CHARACTER_SYMBOL27.
     */
    protected static final int CHARACTER_SYMBOL27 = 0xFE6F;
    /**
     * CHARACTER_SYMBOL26.
     */
    protected static final int CHARACTER_SYMBOL26 = 0xFE30;
    /**
     * CHARACTER_SYMBOL25.
     */
    protected static final int CHARACTER_SYMBOL25 = 0x4DFF;
    /**
     * CHARACTER_SYMBOL24.
     */
    protected static final int CHARACTER_SYMBOL24 = 0x4DC0;
    /**
     * CHARACTER_SYMBOL23.
     */
    protected static final int CHARACTER_SYMBOL23 = 0x33FF;
    /**
     * CHARACTER_SYMBOL22.
     */
    protected static final int CHARACTER_SYMBOL22 = 0x3200;
    /**
     * CHARACTER_SYMBOL21.
     */
    protected static final int CHARACTER_SYMBOL21 = 0x303F;
    /**
     * CHARACTER_SYMBOL20.
     */
    protected static final int CHARACTER_SYMBOL20 = 0x3000;
    /**
     * CHARACTER_SYMBOL19.
     */
    protected static final int CHARACTER_SYMBOL19 = 0x2E7F;
    /**
     * CHARACTER_SYMBOL18.
     */
    protected static final int CHARACTER_SYMBOL18 = 0x2E00;
    /**
     * CHARACTER_SYMBOL17.
     */
    protected static final int CHARACTER_SYMBOL17 = 0x2BFF;
    /**
     * CHARACTER_SYMBOL16.
     */
    protected static final int CHARACTER_SYMBOL16 = 0x2000;
    /**
     * CHARACTER_SYMBOL15.
     */
    protected static final int CHARACTER_SYMBOL15 = 0x19FF;
    /**
     * CHARACTER_SYMBOL14.
     */
    protected static final int CHARACTER_SYMBOL14 = 0x19E0;
    /**
     * CHARACTER_SYMBOL13.
     */
    protected static final int CHARACTER_SYMBOL13 = 0xFF;
    /**
     * CHARACTER_SYMBOL12.
     */
    protected static final int CHARACTER_SYMBOL12 = 0xC0;
    /**
     * CHARACTER_SYMBOL11.
     */
    protected static final int CHARACTER_SYMBOL11 = 191;
    /**
     * CHARACTER_SYMBOL10.
     */
    protected static final int CHARACTER_SYMBOL10 = 123;
    /**
     * CHARACTER_SYMBOL9.
     */
    protected static final int CHARACTER_SYMBOL9 = 96;
    /**
     * CHARACTER_SYMBOL8.
     */
    protected static final int CHARACTER_SYMBOL8 = 91;
    /**
     * CHARACTER_BACKWARD_SLASH.
     */
    protected static final int CHARACTER_BACKWARD_SLASH = 92;
    /**
     * CHARACTER_SYMBOL7.
     */
    protected static final int CHARACTER_SYMBOL7 = 63;
    /**
     * CHARACTER_SYMBOL6.
     */
    protected static final int CHARACTER_SYMBOL6 = 58;
    /**
     * CHARACTER_SYMBOL_COMMA.
     */
    protected static final int CHARACTER_SYMBOL_COMMA = 44;
    /**
     * CHARACTER_SYMBOL_STAR.
     */
    protected static final int CHARACTER_SYMBOL_STAR = 42;
    /**
     * CHARACTER_SYMBOL5.
     */
    protected static final int CHARACTER_SYMBOL5 = 41;
    /**
     * CHARACTER_SYMBOL4.
     */
    protected static final int CHARACTER_SYMBOL4 = 40;
    /**
     * CHARACTER_SYMBOL3.
     */
    protected static final int CHARACTER_SYMBOL3 = 38;
    /**
     * CHARACTER_SYMBOL2.
     */
    protected static final int CHARACTER_SYMBOL2 = 36;
    /**
     * CHARACTER_SYMBOL1.
     */
    protected static final int CHARACTER_SYMBOL1 = 33;
    /**
     * CHARACTER_SPACE.
     */
    protected static final int CHARACTER_SPACE = 32;
    /**
     * CHARACTER_NON_PRINTABLE_END.
     */
    protected static final int CHARACTER_NON_PRINTABLE_END = 31;
    /**
     * CHARACTER_NON_PRINTABLE_START.
     */
    protected static final int CHARACTER_NON_PRINTABLE_START = 0;
    /**
     * CHARACTER_CARRIAGE_RETURN.
     */
    protected static final int CHARACTER_CARRIAGE_RETURN = 13;
    /**
     * CHARACTER_NEWLINE.
     */
    protected static final int CHARACTER_NEWLINE = 10;


    /**
     * @param c        -
     * @param lastType -
     * @return BNFTokenizerType
     */
    protected BNFTokenizerType getType(final int c, final BNFTokenizerType lastType) {
        if (c == CHARACTER_NEWLINE || c == CHARACTER_CARRIAGE_RETURN) {
            return BNFTokenizerType.WHITESPACE_NEWLINE;
        } else if (c >= CHARACTER_NON_PRINTABLE_START && c <= CHARACTER_NON_PRINTABLE_END) {
            return BNFTokenizerType.WHITESPACE_OTHER;
        } else if (c == CHARACTER_SPACE) {
            return BNFTokenizerType.WHITESPACE;
        } else if (c == CHARACTER_SYMBOL1) {
            return BNFTokenizerType.SYMBOL;
        } else if (c == '"') {
            // From: 34 to: 34 From:0x22 to:0x22
            return lastType == BNFTokenizerType.SYMBOL_BACKWARD_SLASH
                    ? BNFTokenizerType.QUOTE_DOUBLE_ESCAPED
                    : BNFTokenizerType.QUOTE_DOUBLE;
        } else if (c == '#') {
            // From: 35 to: 35 From:0x23 to:0x23
            return BNFTokenizerType.SYMBOL_HASH;
        } else if (c >= CHARACTER_SYMBOL2 && c <= CHARACTER_SYMBOL3) {
            return BNFTokenizerType.SYMBOL;
        } else if (c == '\'') {
            // From: 39 to: 39 From:0x27 to:0x27
            return lastType == BNFTokenizerType.SYMBOL_BACKWARD_SLASH
                    ? BNFTokenizerType.QUOTE_SINGLE_ESCAPED
                    : BNFTokenizerType.QUOTE_SINGLE;
        } else if (c >= CHARACTER_SYMBOL4 && c <= CHARACTER_SYMBOL5) {
            return BNFTokenizerType.SYMBOL;
        } else if (c == CHARACTER_SYMBOL_STAR) {
            return BNFTokenizerType.SYMBOL_STAR;
        } else if (c == '+') {
            // From: 43 to: 43 From:0x2B to:0x2B
            return BNFTokenizerType.SYMBOL;
        } else if (c == CHARACTER_SYMBOL_COMMA) {
            return BNFTokenizerType.SYMBOL;
        } else if (c == '-') {
            // From: 45 to: 45 From:0x2D to:0x2D
            return BNFTokenizerType.NUMBER;
        } else if (c == '.') {
            // From: 46 to: 46 From:0x2E to:0x2E
            return BNFTokenizerType.NUMBER;
        } else if (c == '/') {
            // From: 47 to: 47 From:0x2F to:0x2F
            return BNFTokenizerType.SYMBOL_FORWARD_SLASH;
        } else if (c >= '0' && c <= '9') {
            // From: 48 to: 57 From:0x30 to:0x39
            return BNFTokenizerType.NUMBER;
        } else if (c >= CHARACTER_SYMBOL6 && c <= CHARACTER_SYMBOL7) {
            return BNFTokenizerType.SYMBOL;
        } else if (c == '@') {
            // From: 64 to: 64 From:0x40 to:0x40
            return BNFTokenizerType.SYMBOL_AT;
        } else if (c >= 'A' && c <= 'Z') {
            // From: 65 to: 90 From:0x41 to:0x5A
            return BNFTokenizerType.LETTER;
        } else if (c == CHARACTER_BACKWARD_SLASH) {
            // /
            return BNFTokenizerType.SYMBOL_BACKWARD_SLASH;
        } else if (c >= CHARACTER_SYMBOL8 && c <= CHARACTER_SYMBOL9) {
            return BNFTokenizerType.SYMBOL;
        } else if (c >= 'a' && c <= 'z') { // From: 97 to:122 From:0x61 to:0x7A
            return BNFTokenizerType.LETTER;
        } else if (c >= CHARACTER_SYMBOL10 && c <= CHARACTER_SYMBOL11) {
            return BNFTokenizerType.SYMBOL;
        } else if (c >= CHARACTER_SYMBOL12 && c <= CHARACTER_SYMBOL13) {
            // From:192 to:255 From:0xC0 to:0xFF
            return BNFTokenizerType.LETTER;
        } else if (c >= CHARACTER_SYMBOL14 && c <= CHARACTER_SYMBOL15) {
            // khmer symbols
            return BNFTokenizerType.SYMBOL;
        } else if (c >= CHARACTER_SYMBOL16 && c <= CHARACTER_SYMBOL17) {
            // various symbols
            return BNFTokenizerType.SYMBOL;
        } else if (c >= CHARACTER_SYMBOL18 && c <= CHARACTER_SYMBOL19) {
            // supplemental punctuation
            return BNFTokenizerType.SYMBOL;
        } else if (c >= CHARACTER_SYMBOL20 && c <= CHARACTER_SYMBOL21) {
            // cjk symbols & punctuation
            return BNFTokenizerType.SYMBOL;
        } else if (c >= CHARACTER_SYMBOL22 && c <= CHARACTER_SYMBOL23) {
            // enclosed cjk letters and months, cjk compatibility
            return BNFTokenizerType.SYMBOL;
        } else if (c >= CHARACTER_SYMBOL24 && c <= CHARACTER_SYMBOL25) {
            // yijing hexagram symbols
            return BNFTokenizerType.SYMBOL;
        } else if (c >= CHARACTER_SYMBOL26 && c <= CHARACTER_SYMBOL27) {
            // cjk compatibility forms, small form variants
            return BNFTokenizerType.SYMBOL;
        } else if (c >= CHARACTER_SYMBOL28 && c <= CHARACTER_SYMBOL29) {
            // hiragana & katakana halfwitdh & fullwidth forms, Specials
            return BNFTokenizerType.SYMBOL;
        } else {
            return BNFTokenizerType.LETTER;
        }
    }


    /**
     * @param type -
     * @return boolean
     */
    protected boolean isQuote(final BNFTokenizerType type) {
        return type == BNFTokenizerType.QUOTE_DOUBLE || type == BNFTokenizerType.QUOTE_SINGLE;
    }

    /**
     * @param type -
     * @return boolean
     */
    protected boolean isSymbol(final BNFTokenizerType type) {
        return type == BNFTokenizerType.SYMBOL
                || type == BNFTokenizerType.SYMBOL_HASH
                || type == BNFTokenizerType.SYMBOL_AT
                || type == BNFTokenizerType.SYMBOL_STAR
                || type == BNFTokenizerType.SYMBOL_FORWARD_SLASH
                || type == BNFTokenizerType.SYMBOL_BACKWARD_SLASH;
    }

    /**
     * @param type -
     * @return boolean
     */
    protected boolean isWhitespace(final BNFTokenizerType type) {
        return type == BNFTokenizerType.WHITESPACE
                || type == BNFTokenizerType.WHITESPACE_OTHER
                || type == BNFTokenizerType.WHITESPACE_NEWLINE;
    }

    /**
     * @param type -
     * @return boolean
     */
    protected boolean isComment(final BNFTokenizerType type) {
        return type == BNFTokenizerType.COMMENT_MULTI_LINE
                || type == BNFTokenizerType.COMMENT_SINGLE_LINE;
    }

    /**
     * @param type -
     * @return boolean
     */
    protected boolean isNumber(final BNFTokenizerType type) {
        return type == BNFTokenizerType.NUMBER;
    }

    /**
     * @param type -
     * @return boolean
     */
    protected boolean isLetter(final BNFTokenizerType type) {
        return type == BNFTokenizerType.LETTER;
    }


    /**
     * @param last - Token
     * @return boolean
     */
    protected boolean isWord(final Token last) {
        return last != null && last.isWord();
    }


    /**
     * Whether to include the text while parsing.
     * @param type - BNFTokenizerType
     * @param params - BNFTokenizerParams
     * @return boolean
     */
    protected boolean includeText(final BNFTokenizerType type, final BNFTokenizerParams params) {
        return (params.isIncludeWhitespace() && type == BNFTokenizerType.WHITESPACE)
                || (params.isIncludeWhitespaceOther() && type == BNFTokenizerType.WHITESPACE_OTHER)
                || (params.isIncludeWhitespaceNewlines() && type == BNFTokenizerType.WHITESPACE_NEWLINE)
                || !isWhitespace(type);
    }


}
