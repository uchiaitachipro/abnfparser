import ca.gobits.bnf.parser.*;
import ca.gobits.bnf.tokenizer.ABNFTokenizerFactoryImpl;
import ca.gobits.bnf.tokenizer.Token;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

public class TestGroupGrammer {

    public static void main(String[] args) {

        InputStream in = TestGroupGrammer.class
                .getClassLoader()
                .getResourceAsStream("support_group.rule");
        ISequenceFactory factory = new ABNFSequenceFactoryImpl();
        Map<String, List<BNFSequence>> map = factory.map(in);
        IParser parser = new ABNFParserImpl(map);

        ABNFTokenizerFactoryImpl tokenizerFactory = new ABNFTokenizerFactoryImpl();


        String[] testGroup = new String[]{
                // true
                "aababa",
                // true
                "abbaba",
                // false
                "aabab",
                // false
                "aaaaa",
                // false
                "",
                // false
                null};

        for (int i = 0; i < testGroup.length; i++) {

            Token token = tokenizerFactory.tokens(testGroup[i]);
            IParseResult result = parser.parse(token, testGroup[i]);
            System.out.println("result : " + result.isSuccess() + " top : " + result.getTop());
            System.out.println(result.getMatchWords());

        }

    }

}
