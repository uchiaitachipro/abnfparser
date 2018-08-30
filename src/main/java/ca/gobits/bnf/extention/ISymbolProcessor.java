package ca.gobits.bnf.extention;

import ca.gobits.bnf.parser.SymbolMetaData;

import java.util.Map;

public interface ISymbolProcessor {

    int PRIORIRY_SECOND = 2;
    int PRIORITY_THIRD = 3;
    int PRORITY_FORTH = 4;
    int PRIORITY_FIFTH = 5;
    int PRIORITY_SIXTH = 6;

    void preHandleSymbol(Map<String ,String> sentences);

    String handleSymbol(String symbol,SymbolMetaData metaData);

    int getSymbolPriority();

    String[] getSymbols();
}
