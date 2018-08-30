import ca.gobits.bnf.parser.*;
import ca.gobits.bnf.tokenizer.Token;
import ca.gobits.bnf.tokenizer.BNFTokenizerFactoryImpl;
import ca.gobits.bnf.tokenizer.TokenizerFactory;

import java.util.List;
import java.util.Map;

public class TestJsonBnf {

    public static void main(String[] args) {
//        String text = "The cow jumped over the moon!";
//        TokenizerFactory factory = new BNFTokenizerFactoryImpl();
//        Token token = factory.tokens(text);
//        while (token != null) {
//            System.out.println("TOKEN " + token.getStringValue());
//            token = token.getNextToken();
//        }

//        text = "sample key = sample value";
//        PropertyParser parser = new PropertyParser();
//        Map<String, String> keyValueMap = parser.parse(text);
//
//        for (Map.Entry<String,String> entry : keyValueMap.entrySet()){
//            System.out.println(entry.getKey() + " = " + entry.getValue());
//        }

// Create String Tokens
        String text = "{ \"key\":\"蛤\",\"array\":[\"虎\",\"2\"]}";
        TokenizerFactory tokenizerFactory = new BNFTokenizerFactoryImpl();
        Token token = tokenizerFactory.tokens(text);

// Create Backus-Naur Form State Definitions
        BNFSequenceFactoryImpl factory = new BNFSequenceFactoryImpl();
        Map<String, List<BNFSequence>> map = factory.json();

// Run Tokens through IParser
        BNFParserImpl parser = new BNFParserImpl(map);
        IParseResult result = parser.parse(token,text);

        System.out.println("IsSuccess: " + result.isSuccess());
        System.out.println("Top" + result.getTop());

    }

}
