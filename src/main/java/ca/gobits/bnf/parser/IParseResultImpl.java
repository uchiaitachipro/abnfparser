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

import ca.gobits.bnf.tokenizer.Token;

import java.util.LinkedHashMap;
import java.util.List;

/**
 * BNF IParser Result implementation.
 *
 */
public class IParseResultImpl implements IParseResult {

    /** Top token. */
    private Token top;

    /** Top error token. */
    private Token error;

    /** Max token was successfully validated. */
    private Token maxToken;

    /** Was IParser successful. */
    private boolean success;

    private List<ResultNode> data;

    /**
     * default constructor.
     */
    public IParseResultImpl() {
    }

    @Override
    /**
     * @return boolean
     */
    public boolean isSuccess() {
        return this.success;
    }

    @Override
    public List<ResultNode> getMatchWords() {
        return data;
    }

    @Override
    public void setMatchWords(String type) {
        StringBuilder sb = new StringBuilder();

        if (data.size() <= 0){
            return;
        }
        int startIndex = data.get(0).getStartIndex();
        for(ResultNode word : data){
            sb.append(word.getValue());
        }
        data.clear();
        data.add(new ResultNode(type,sb.toString(),startIndex));
    }

    /**
     * @param status -
     */
    public void setSuccess(final boolean status) {
        this.success = status;
    }

    @Override
    /**
     * @return Token
     */
    public Token getTop() {
        return this.top;
    }

    /**
     * @param token -
     */
    public void setTop(final Token token) {
        this.top = token;
    }

    @Override
    /**
     * @return Token
     */
    public Token getError() {
        return this.error;
    }

    /**
     * @param token -
     */
    public void setError(final Token token) {
        this.error = token;
    }

    /**
     * @param token -
     */
    public void setMaxMatchToken(final Token token) {
        if (this.maxToken == null
                || (token != null && token.getId() > this.maxToken.getId())) {
            this.maxToken = token;
        }
    }

    public void setMatchResult(List<ResultNode> data){
        this.data = data;
    }

    /**
     * complete.
     */
    public void complete() {

        if (!isSuccess()) {
            setError(this.maxToken);
        }
    }
}
