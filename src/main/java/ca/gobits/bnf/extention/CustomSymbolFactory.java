package ca.gobits.bnf.extention;

import ca.gobits.bnf.parser.SymbolMetaData;

import java.util.*;

public class CustomSymbolFactory {

    private Map<Integer,List<ISymbolProcessor>> prioritySymbols
            = new LinkedHashMap<>(4);


    private static volatile  CustomSymbolFactory instance;


    private CustomSymbolFactory(){
        registerDefaultSymbols();
    }

    public static CustomSymbolFactory getInstance(){
        if (instance == null){
            synchronized (CustomSymbolFactory.class){
                if (instance == null){
                    instance = new CustomSymbolFactory();
                }
            }
        }
        return instance;
    }

    public void registerSymbolProcessor(int priority,ISymbolProcessor symbol){

        if (!prioritySymbols.containsKey(priority)){
            List<ISymbolProcessor> symbols = new LinkedList<>();
            symbols.add(symbol);
            prioritySymbols.put(priority,symbols);
            return;
        }
        prioritySymbols.get(priority).add(symbol);
    }

    public void unRegisterSymbolProcessor(int priority,ISymbolProcessor symbol){

        if (!prioritySymbols.containsKey(priority)){
            return;
        }

        if (prioritySymbols.get(priority).contains(symbol)){
            prioritySymbols.remove(symbol);
        }
    }



    private void registerDefaultSymbols(){
        registerSymbolProcessor(
                optionalSymbolProcessor.getSymbolPriority(),
                optionalSymbolProcessor);
        registerSymbolProcessor(
                lookNextIsMatchSymbolProcessor.getSymbolPriority(),
                lookNextIsMatchSymbolProcessor);
        registerSymbolProcessor(placeHolderSymbolProcessor.getSymbolPriority(),
                placeHolderSymbolProcessor);
    }

    public List<ISymbolProcessor> getSymbolProcessorByPriority(int priority){
        return prioritySymbols.get(priority);
    }

    public List<String> needRemoveSymbolsInDynamicalRule(){

        List<String> symbolList = new LinkedList<>();

        for (List<ISymbolProcessor> item : prioritySymbols.values()){

            if (item.size() < 1){
                continue;
            }

            for(ISymbolProcessor processor : item){

                String[] symbols = processor.getSymbols();
                if (symbols == null || symbols.length < 1){
                    continue;
                }

                for (int i = 0 ; i < symbols.length ; i++){
                    symbolList.add(symbols[i]);
                }

            }

        }

        return symbolList;
    }



    private static final String OPTIONAL_SYMBOL_PREFIX = "[";
    private static final String OPTIONAL_SYMBOL_SUFFIX = "]";
    private final ISymbolProcessor optionalSymbolProcessor = new ISymbolProcessor() {
        @Override
        public void preHandleSymbol(Map<String, String> sentences) {

            for (Map.Entry<String, String> pair : sentences.entrySet()) {

                String key = pair.getKey();
                String value = pair.getValue();

                if (!((value.contains(OPTIONAL_SYMBOL_PREFIX)
                        && value.contains(OPTIONAL_SYMBOL_SUFFIX)))){
                    continue;
                }

                Stack<Character> stack = new Stack<>();
                for (int i = 0; i < value.length(); i++){
                    char c = value.charAt(i);
                    if (c == '['){
                        stack.push(c);
                    }

                    if (c == ']'){
                        stack.pop();
                    }
                }

                if (stack.size() > 0){
                    throw new IllegalArgumentException("Please check optional gramar. e.g. [rule]");
                }

                value = value.replaceAll("\\[","*1(")
                        .replaceAll(OPTIONAL_SYMBOL_SUFFIX,")");

                sentences.put(key,value);

            }


        }

        @Override
        public String handleSymbol(String symbol, SymbolMetaData metaData) {
            return symbol;
        }


        @Override
        public int getSymbolPriority() {
            return ISymbolProcessor.PRIORITY_THIRD;
        }

        @Override
        public String[] getSymbols() {
            return new String[]{
//                    "\\[",
//                    OPTIONAL_SYMBOL_SUFFIX
            };
        }
    };


    private static final String LOOK_NEXT_IF_MATCH = "@care";
    private final ISymbolProcessor lookNextIsMatchSymbolProcessor = new ISymbolProcessor() {
        @Override
        public void preHandleSymbol(Map<String, String> sentences) {

        }

        @Override
        public String handleSymbol(String symbol, SymbolMetaData metaData) {

            if (!symbol.startsWith(LOOK_NEXT_IF_MATCH)){
                return symbol;
            }

            String string = symbol.replace("@care","");

            if (string.startsWith(OPTIONAL_SYMBOL_PREFIX) || string.startsWith("*")){
                throw new IllegalArgumentException("@care can't modify rule such as repetition or optional");
            }

            metaData.setGoNextIfNoMatch(true);
            metaData.setName(string);

            return string;
        }

        @Override
        public int getSymbolPriority() {
            return ISymbolProcessor.PRIORIRY_SECOND;
        }

        @Override
        public String[] getSymbols() {
            return new String[]{LOOK_NEXT_IF_MATCH};
        }
    };

    private static final String PLACE_HOLDER_SYMBOL_PREFIX = "<";
    private static final String PLACE_HOLDER_SYMBOL_SUFFIX = ">";
    private static final String PLACE_HOLDER_SYMBOL_NAME = "#placeholder#";
    private static final ISymbolProcessor placeHolderSymbolProcessor = new ISymbolProcessor() {
        @Override
        public void preHandleSymbol(Map<String, String> sentences) {

        }

        @Override
        public String handleSymbol(String symbol, SymbolMetaData metaData) {

            String result = symbol;
            if (symbol.contains(PLACE_HOLDER_SYMBOL_PREFIX)
                    && symbol.contains(PLACE_HOLDER_SYMBOL_SUFFIX)){

                int startIndex = symbol.indexOf(PLACE_HOLDER_SYMBOL_PREFIX);
                int endIndex = symbol.lastIndexOf(PLACE_HOLDER_SYMBOL_SUFFIX);

                if (startIndex == -1  || endIndex == -1){
                    return result;
                }

                if (startIndex == (symbol.length() - 1) || endIndex == 0){
                    throw new IllegalArgumentException("Error <> grammar ,please check rule.");
                }

                String label = symbol.substring(startIndex + 1,endIndex);

                if (label.contains(PLACE_HOLDER_SYMBOL_SUFFIX)
                        || label.contains(PLACE_HOLDER_SYMBOL_PREFIX)){
                    throw new IllegalArgumentException("Error <> grammar , it can't nest <>");
                }

                metaData.setPlaceholder(true);
                metaData.setPlaceholderLabel(label);
                metaData.setName(PLACE_HOLDER_SYMBOL_NAME);
                return PLACE_HOLDER_SYMBOL_NAME;
            }

            return result;
        }

        @Override
        public int getSymbolPriority() {
            return ISymbolProcessor.PRORITY_FORTH;
        }

        @Override
        public String[] getSymbols() {
            return new String[]{
                    PLACE_HOLDER_SYMBOL_PREFIX,
                    PLACE_HOLDER_SYMBOL_SUFFIX
            };
        }
    };

}
