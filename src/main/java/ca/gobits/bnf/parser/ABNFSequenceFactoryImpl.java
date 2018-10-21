package ca.gobits.bnf.parser;

import ca.gobits.bnf.extention.CustomSymbolFactory;
import ca.gobits.bnf.extention.ISymbolProcessor;

import java.io.InputStream;
import java.util.*;

public class ABNFSequenceFactoryImpl implements ISequenceFactory {

    private static String REPETITIONMETAREGEX = "_@\\d+_\\d+@_";


    private static final String REPETITION_SYMBOL_PREFIX = "_@";
    private static final String REPRETITION_SYMBOL_SUFFIX = "@_";

    @Override
    public Map<String, List<BNFSequence>> json() {
        return null;
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
        return buildMap(parser.parse(rule));
    }

    /**
     * @param prop - grammar property map
     * @return Map<String ,   BNFSequences>
     */
    private Map<String, List<BNFSequence>> buildMap(final Map<String, String> prop) {

        preHandlePrioritySymbols(prop,ISymbolProcessor.PRIORITY_THIRD);
        preHandleRepetitionSymbol(prop);
        expandGroupInItem(prop);

        Map<String, List<BNFSequence>> result = new HashMap<String, List<BNFSequence>>();

        for (Map.Entry<String, String> e : prop.entrySet()) {

            String name = e.getKey().toString();

            String value = e.getValue().toString();

            List<String> sequenceNames = createSequenceNames(value);

            List<BNFSequence> sequences = createBNFSequences(sequenceNames,name);
            result.put(name, sequences);
        }

        preHandlePrioritySymbols(prop,ISymbolProcessor.PRORITY_FORTH);
        preHandlePrioritySymbols(prop,ISymbolProcessor.PRIORITY_FIFTH);
        preHandlePrioritySymbols(prop,ISymbolProcessor.PRIORITY_SIXTH);

        return result;
    }

    /**
     * @param sequenceNames -
     * @return BNFSequences
     */
    private List<BNFSequence> createBNFSequences(final List<String> sequenceNames,String parentSymBol) {
        List<BNFSequence> list = createBNFSequenceList(sequenceNames,parentSymBol);
        return list;
    }

    /**
     * @param sequenceNames -
     * @return List<BNFSequence>
     */
    private List<BNFSequence> createBNFSequenceList(final List<String> sequenceNames,String parentSymbol) {

        List<BNFSequence> list = new ArrayList<>(sequenceNames.size());

        for (String s : sequenceNames) {
            BNFSequence sequence = createSequence(s,parentSymbol);
            list.add(sequence);
        }

        return list;
    }

    /**
     * @param s -
     * @return BNFSequence
     */
    private BNFSequence createSequence(final String s,String parentSymbol) {

        List<SymbolMetaData> symbols = createSymbols(s,parentSymbol);
        return new BNFSequence(symbols);
    }

    /**
     * @param s -
     * @return List<SymbolMetaData>
     */
    private List<SymbolMetaData> createSymbols(final String s,final String parentSymbol) {

        String[] split = s.trim().split(" ");

        List<SymbolMetaData> symbols = new ArrayList<SymbolMetaData>(split.length);

        for (String ss : split) {

            // split出的字符串可能为空需要检查
            if (isEmpty(ss)){
                continue;
            }

            SymbolMetaData symbol = createSymbol(ss);
            symbol.setParentSymbol(parentSymbol);
            symbols.add(symbol);
        }

        return symbols;
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

            if (s.contains(";")){
                s = s.substring(0,s.indexOf(";"));
            }

            list.add(s.trim());
        }

        return list;
    }

    private void expandGroupInItem(Map<String, String> prop) {

        Map<String, String> copyOfProp = new HashMap<>(prop);

        for (Map.Entry<String, String> pair : copyOfProp.entrySet()) {
            String key = pair.getKey();
            String value = pair.getValue();
            while (value.contains("(") && value.contains(")")) {
                int firstStartIndex = -1;
                int firstEndIndex = -1;

                Stack<Character> stack = new Stack<>();
                for (int j = 0; j < value.length(); j++) {

                    char c = value.charAt(j);
                    firstEndIndex = j;
                    if (c == ')') {
                        break;
                    }
                    stack.push(c);
                }
                firstStartIndex = firstEndIndex != -1 ? firstEndIndex - 1 : firstEndIndex;
                while (!stack.empty()) {
                    if (stack.pop() == '(') {
                        break;
                    }
                    firstStartIndex--;
                }


                if (firstStartIndex == -1 && firstEndIndex == -1) {
                    break;
                }

                if ((firstStartIndex == -1 && firstEndIndex != -1)
                        || (firstStartIndex != -1 && firstEndIndex == -1)) {
                    throw new IllegalArgumentException("sympol ( and ) must be a pair ,recheck it!");
                }

                int leftSubStringIndex = firstStartIndex == 0 ? -1 : firstStartIndex;
                int rightSubStringIndex = firstEndIndex == value.length() - 1 ? -1 : firstEndIndex;

                StringBuilder sb = new StringBuilder();

                if (leftSubStringIndex != -1) {
                    sb.append(value.substring(0, firstStartIndex));
                }

                String subGroup = value.substring(
                        firstStartIndex + 1,
                        firstEndIndex);

                String appendKey = "rule-" + subGroup
                        .replaceAll(REPETITIONMETAREGEX,"")
                        .replaceAll("[|]", "or")
                        .replaceAll("\\*", "")
                        .replaceAll(" ", "")
                        .replaceAll("<","lr")
                        .replaceAll(">","rl");


//                String appendKey = UUID.randomUUID().toString();

                for (String symbol : CustomSymbolFactory.getInstance().needRemoveSymbolsInDynamicalRule()){
                    appendKey.replaceAll(symbol,"");
                }

                sb.append(appendKey);

                if (rightSubStringIndex != -1) {
                    sb.append(value.substring(firstEndIndex + 1, value.length()));
                }

                String newValue = sb.toString();
                if (!newValue.equals(value)) {

                    value = newValue;
                    prop.put(appendKey, subGroup);
                    prop.put(key, newValue);
                }
            }
        }
    }

    private void preHandlePrioritySymbols(Map<String,String> prop, int priority){
        List<ISymbolProcessor> secondPrioritySymbols =  CustomSymbolFactory
                .getInstance()
                .getSymbolProcessorByPriority(priority);

        if (secondPrioritySymbols != null){
            for (ISymbolProcessor processor : secondPrioritySymbols){
                processor.preHandleSymbol(prop);
            }
        }
    }


    private void preHandleRepetitionSymbol(Map<String, String> prop) {
        for (Map.Entry<String, String> pair : prop.entrySet()) {

            String key = pair.getKey();
            String value = pair.getValue();


            while (value.contains("*")) {

                int repetitionSymbolIndex = value.indexOf("*");
                String left = extractNumberFromString(value,repetitionSymbolIndex,true);
                String right = extractNumberFromString(value,repetitionSymbolIndex,false);
                StringBuilder sb = new StringBuilder();
                String replaceString = sb
                        .append(REPETITION_SYMBOL_PREFIX)
                        .append(isEmpty(left) ? 0 : left)
                        .append("_")
                        .append(isEmpty(right) ? Integer.MAX_VALUE : right)
                        .append(REPRETITION_SYMBOL_SUFFIX)
                        .toString();
                int leftIndex = repetitionSymbolIndex - (isEmpty(left)
                        ? 0
                        : left.length());

                int rightIndex = repetitionSymbolIndex + (isEmpty(right)
                        ? 0
                        : right.length());

                value = replaceString(value, replaceString,leftIndex,rightIndex + 1);
            }

            prop.put(key,value);

        }
    }

    private String replaceString(String sourceString,String replaceString,int startIndex,int endIndex){
        StringBuilder sb = new StringBuilder();

        if (startIndex > 0){
            sb.append(sourceString.substring(0,startIndex));
        }
        sb.append(replaceString);
        if (endIndex < sourceString.length()){
            sb.append(sourceString.substring(endIndex,sourceString.length()));
        }
        return sb.toString();
    }

    private boolean isEmpty(String string){
        return  string == null || string.equals("");
    }

    private String extractNumberFromString(String sourceString,int startIndex, boolean left){

        int numberStartIndex = -1;
        int nextIndex = left ? startIndex - 1 :startIndex + 1;
        int count = sourceString.length();
        while (nextIndex >= 0 && nextIndex < count){

            char charactor = sourceString.charAt(nextIndex);

            if (charactor < '0' || charactor > '9'){
               break;
            }
            numberStartIndex = nextIndex;
            nextIndex = left ? nextIndex - 1 :nextIndex + 1;

        }

        if (numberStartIndex == -1){
            return "";
        }
        int s = left ? numberStartIndex : startIndex + 1;
        int e = left ? startIndex : (
                numberStartIndex + 1 > sourceString.length()
                        ? sourceString.length()
                        : numberStartIndex + 1);
        return sourceString.substring(s,e);
    }

    /**
     * @param s -
     * @return SymbolMetaData
     */
    private SymbolMetaData createSymbol(final String s) {

        String ss = s;
        SymbolMetaData.Repetition repetition = SymbolMetaData.Repetition.NONE;
        SymbolMetaData result = new SymbolMetaData(ss,repetition);

        handlePrioritySymbol(s,result,ISymbolProcessor.PRIORIRY_SECOND);
        ss = result.getName();

        if (ss.contains(REPETITION_SYMBOL_PREFIX) && ss.contains(REPRETITION_SYMBOL_SUFFIX)){

            int index = ss.lastIndexOf(REPRETITION_SYMBOL_SUFFIX);
            ss = ss.substring(index+2,ss.length());
            String metaData = s.substring(2,index);
            String[] repetitions = metaData.split("_");
            int minRepetitionValue = Integer.valueOf(repetitions[0]);
            int maxRepetitionValue = Integer.valueOf(repetitions[1]);
            result.setName(ss);
            result.setMinRepetitionTimes(minRepetitionValue);
            result.setMaxRepetitionTimes(maxRepetitionValue);
            result.setRepetition(SymbolMetaData.Repetition.ZERO_OR_MORE);
        }

        handlePrioritySymbol(s,result,ISymbolProcessor.PRIORITY_THIRD);
        handlePrioritySymbol(s,result,ISymbolProcessor.PRORITY_FORTH);
        handlePrioritySymbol(s,result,ISymbolProcessor.PRIORITY_FIFTH);
        handlePrioritySymbol(s,result,ISymbolProcessor.PRIORITY_SIXTH);

        return result;
    }

    private void handlePrioritySymbol(String symbol,SymbolMetaData metaData,int priority){
        List<ISymbolProcessor> prioritySymbols =  CustomSymbolFactory
                .getInstance()
                .getSymbolProcessorByPriority(priority);
        String ss = symbol;
        if (prioritySymbols != null){
            for (ISymbolProcessor processor : prioritySymbols){
                 processor.handleSymbol(ss,metaData);
            }
        }
    }


//    /**
//     * 对或排序中的字符串排序，使字符数量多的始终优先比较。
//     * 待匹配       aabcdba
//     * rule        = r3 (r5 | r6) $ r4
//     * r5          = ab;
//     * r6          = abc;
//     *
//     * 或中用错误的字符串匹配，导致整体不匹配
//     */
//    private void sortSequenceNames(final List<String> list) {
//        Collections.sort(list, new Comparator<String>() {
//            @Override
//            public int compare(final String o1, final String o2) {
//                return o2.length() - o1.length();
//            }
//        });
//    }

}
