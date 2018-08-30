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

import java.io.InputStream;
import java.util.*;

import ca.gobits.bnf.parser.SymbolMetaData.Repetition;

/**
 * BNF Sequence Factory implementation.
 */
public class BNFSequenceFactoryImpl implements ISequenceFactory {


    @Override
    public Map<String, List<BNFSequence>> json() {

        Map<String, String> prop = getGrammarJSON();

        return buildMap(prop);
    }

    @Override
    public Map<String, List<BNFSequence>> map(InputStream stream) {
        PropertyParser parser = new PropertyParser();
        try {
            Map<String, String> sentences = parser.parse(stream);
            return buildMap(sentences);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public Map<String, List<BNFSequence>> map(String rule) {
        PropertyParser parser = new PropertyParser();
        Map<String, String> sentences = parser.parse(rule);
        return buildMap(sentences);
    }

    /**
     * @param prop - grammar property map
     * @return Map<String ,   BNFSequences>
     */
    private Map<String, List<BNFSequence>> buildMap(final Map<String, String> prop) {

        Map<String, List<BNFSequence>> result = new HashMap<String, List<BNFSequence>>();

        for (Map.Entry<String, String> e : prop.entrySet()) {

            String name = e.getKey().toString();

            String value = e.getValue().toString();

            List<String> sequenceNames = createSequenceNames(value);

            List<BNFSequence> sequences = createBNFSequences(sequenceNames);
            result.put(name, sequences);
        }

        return result;
    }

    /**
     * @param sequenceNames -
     * @return BNFSequences
     */
    private List<BNFSequence> createBNFSequences(final List<String> sequenceNames) {
        List<BNFSequence> list = createBNFSequenceList(sequenceNames);
        return list;
    }

    /**
     * @param sequenceNames -
     * @return List<BNFSequence>
     */
    private List<BNFSequence> createBNFSequenceList(final List<String> sequenceNames) {

        List<BNFSequence> list = new ArrayList<BNFSequence>(
                sequenceNames.size());

        for (String s : sequenceNames) {
            BNFSequence sequence = createSequence(s);
            list.add(sequence);
        }

        return list;
    }

    /**
     * @param s -
     * @return BNFSequence
     */
    private BNFSequence createSequence(final String s) {

        List<SymbolMetaData> symbols = createSymbols(s);
        return new BNFSequence(symbols);
    }

    /**
     * @param s -
     * @return List<SymbolMetaData>
     */
    private List<SymbolMetaData> createSymbols(final String s) {

        String[] split = s.trim().split(" ");

        List<SymbolMetaData> symbols = new ArrayList<SymbolMetaData>(split.length);

        for (String ss : split) {
            SymbolMetaData symbol = createSymbol(ss);
            symbols.add(symbol);
        }

        return symbols;
    }

    /**
     * @param s -
     * @return SymbolMetaData
     */
    private SymbolMetaData createSymbol(final String s) {

        String ss = s;
        Repetition repetition = Repetition.NONE;

        if (ss.endsWith("*")) {
            ss = ss.substring(0, ss.length() - 1);
            repetition = Repetition.ZERO_OR_MORE;
        }

        return new SymbolMetaData(ss, repetition);
    }

    /**
     * @param value -
     * @return List<String>
     */
    private List<String> createSequenceNames(final String value) {

        String[] values = value.split("[|]");
        List<String> list = new ArrayList<>(values.length);

        for (String s : values) {

            if (s.endsWith(";")) {
                s = s.substring(0, s.length() - 1);
            }

            list.add(s.trim());
        }

        sortSequenceNames(list);

        return list;
    }

    /**
     * @param list -
     */
    private void sortSequenceNames(final List<String> list) {
        Collections.sort(list, new Comparator<String>() {
            @Override
            public int compare(final String o1, final String o2) {
                if (o1.equals("Empty")) {
                    return 1;
                } else if (o2.equals("Empty")) {
                    return -1;
                }
                return 0;
            }
        });
    }

    /**
     * Load JSON grammar.
     *
     * @return Map<String   ,       String>
     */
    private Map<String, String> getGrammarJSON() {
        PropertyParser parser = new PropertyParser();
        InputStream is = getClass().getResourceAsStream("/json.bnf");
        try {
            return parser.parse(is);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}