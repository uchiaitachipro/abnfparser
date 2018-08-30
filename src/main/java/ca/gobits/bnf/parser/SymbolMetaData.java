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

/**
 * SymbolMetaData - holder class for name or repetition.
 */
public class SymbolMetaData {

    /** BNF Repetition. */
    public enum Repetition {
        NONE,
        ZERO_OR_MORE,
    }


    /** name of symbol. */
    private String name;

    private String parentSymbol;

    private boolean isGoNextIfNoMatch = false;

    /** repetition of symbol. */
    private Repetition repetition;

    private int minRepetitionTimes;

    private int maxRepetitionTimes;

    private boolean isGroup;

    private boolean isPlaceholder;

    private String placeholderLabel;

    /**
     * default constructor.
     */
    public SymbolMetaData() {
        this.repetition = Repetition.NONE;
    }

    /**
     * constructor with name.
     * @param original - name of symbol
     */
    public SymbolMetaData(final String original) {
        this();
        this.name = original;
    }

    /**
     * constructor.
     * @param original - name of symbol
     * @param rep - repetition of symbol
     */
    public SymbolMetaData(final String original, final Repetition rep) {
        this(original);
        this.repetition = rep;
    }

    /**
     * @return String - name of symbol
     */
    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return Repetition - repetition of symbol
     */
    public Repetition getRepetition() {
        return this.repetition;
    }

    public void setRepetition(Repetition repetition){
        this.repetition = repetition;
    }


    public int getMinRepetitionTimes() {
        return minRepetitionTimes;
    }

    public void setMinRepetitionTimes(int minRepetitionTimes) {
        this.minRepetitionTimes = minRepetitionTimes;
    }

    public int getMaxRepetitionTimes() {
        return maxRepetitionTimes;
    }

    public void setMaxRepetitionTimes(int maxRepetitionTimes) {
        this.maxRepetitionTimes = maxRepetitionTimes;
    }

    public String getParentSymbol() {
        return parentSymbol;
    }

    public void setParentSymbol(String parentSymbol) {
        this.parentSymbol = parentSymbol;
    }

    public boolean isGoNextIfNoMatch() {
        return isGoNextIfNoMatch;
    }

    public void setGoNextIfNoMatch(boolean goNextIfNoMatch) {
        isGoNextIfNoMatch = goNextIfNoMatch;
    }

    public boolean isGroup() {
        return isGroup;
    }

    public void setGroup(boolean group) {
        isGroup = group;
    }

    public boolean isPlaceholder() {
        return isPlaceholder;
    }

    public void setPlaceholder(boolean placeholder) {
        isPlaceholder = placeholder;
    }

    public String getPlaceholderLabel() {
        return placeholderLabel;
    }

    public void setPlaceholderLabel(String placeholderLabel) {
        this.placeholderLabel = placeholderLabel;
    }


    @Override
    public String toString() {
        return this.name;
    }

    public SymbolMetaData copy(){
        SymbolMetaData result = new SymbolMetaData();
        result.name = this.name;
        result.parentSymbol = this.parentSymbol;
        result.isGoNextIfNoMatch = this.isGoNextIfNoMatch;
        result.repetition = this.repetition;
        result.minRepetitionTimes = this.minRepetitionTimes;
        result.maxRepetitionTimes = this.maxRepetitionTimes;
        result.isGroup = this.isGroup;
        result.isPlaceholder = this.isPlaceholder;
        result.placeholderLabel = this.placeholderLabel;
        return result;
    }

}
