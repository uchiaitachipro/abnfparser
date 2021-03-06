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

package ca.gobits.bnf.tokenizer;

/**
 * Token holder for the result from the tokenizer.
 */
public class Token {

    /**
     * Type of tokens.
     */
    public enum TokenType {
        /** COMMENT. */
        COMMENT,
        /** QUOTED_STRING. */
        QUOTED_STRING,
        /** NUMBER. */
        NUMBER,
        /** WORD. */
        WORD,
        /** SYMBOL. */
        SYMBOL,
        /** WHITESPACE. */
        WHITESPACE,
        /** WHITESPACE_NEWLINE. */
        WHITESPACE_NEWLINE
    }

    /** unique token identifier. */
    private int id;

    /** string value of token. */
    private String stringValue;

    /** type of token. */
    private TokenType tokenType;

    /** next token in line. */
    private Token nextToken;

    /** previous token. */
    private Token previousToken;

    private String rawContent;

    /**
     * default constructor.
     */
    public Token() {
    }

    /**
     * constructor with string.
     * @param value -
     */
    public Token(final String value) {
        this.stringValue = value;
    }

    /**
     * Append value to token.
     * @param c -
     */
    public void appendValue(final char c) {
        this.stringValue = this.stringValue + String.valueOf(c);
    }

    /**
     * @return String
     */
    public String getStringValue() {
        return this.stringValue;
    }

    /**
     * @param value -
     */
    public void setStringValue(final String value) {
        this.stringValue = value;
    }

    /**
     * @return Token
     */
    public Token getNextToken() {
        return this.nextToken;
    }

    /**
     * @param token -
     */
    public void setNextToken(final Token token) {
        this.nextToken = token;
    }

    @Override
    public String toString() {
        return "TOKEN value: " + getStringValue() + " id: " + getId()
                + " type: " + getType();
    }

    /**
     * @return boolean
     */
    public boolean isSymbol() {
        return this.tokenType == TokenType.SYMBOL;
    }

    /**
     * @return boolean
     */
    public boolean isWord() {
        return this.tokenType == TokenType.WORD;
    }

    /**
     * @return boolean
     */
    public boolean isQuotedString() {
        return this.tokenType == TokenType.QUOTED_STRING;
    }

    /**
     * @return boolean
     */
    public boolean isNumber() {
        return this.tokenType == TokenType.NUMBER;
    }

    /**
     * @return boolean
     */
    public boolean isComment() {
        return this.tokenType == TokenType.COMMENT;
    }

    /**
     * @return boolean
     */
    public boolean isWhitespace() {
        return this.tokenType == TokenType.WHITESPACE;
    }

    /**
     * @return int
     */
    public int getId() {
        return this.id;
    }

    /**
     * @param identifier -
     */
    public void setId(final int identifier) {
        this.id = identifier;
    }

    /**
     * @return TokenType
     */
    public TokenType getType() {
        return this.tokenType;
    }

    /**
     * @param type -
     */
    public void setType(final TokenType type) {
        this.tokenType = type;
    }

    /**
     * @return Token
     */
    public Token getPreviousToken() {
        return this.previousToken;
    }

    /**
     * @param token -
     */
    public void setPreviousToken(final Token token) {
        this.previousToken = token;
    }

    public String getRawContent() {
        return rawContent;
    }

    public void setRawContent(String rawContent) {
        this.rawContent = rawContent;
    }

}
