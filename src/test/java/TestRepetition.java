import ca.gobits.bnf.MatchFactory;
import ca.gobits.bnf.io.IOUtils;
import ca.gobits.bnf.parser.*;
import ca.gobits.bnf.tokenizer.ABNFTokenizerFactoryImpl;
import ca.gobits.bnf.tokenizer.Token;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

public class TestRepetition {

//    support_repetition.rule


    public static void main(String[] args) {

        String key = "support_repetition";

        try (InputStream in = TestGroupGrammer.class
                .getClassLoader()
                .getResourceAsStream("support_repetition.rule")) {

            String rule = IOUtils.toString(in);
            MatchFactory.getInstance().setRule(key, rule);

//            String[] testGroup = new String[]{"aab", "b", "aaaaaaab", "c"};
//        String[] testGroup = new String[]{"acab","b","ab","cb","aaaaaaaaaa","ccc"};
//        String[] testGroup = {"bbbabbcbcc"};
//        String[] testGroup = {"abcab"};
//        String[] testGroup = {"aabcabba","aabcabcabba","aabba","aabc","aabcabc"};
//        String[] testGroup = new String[] {"aacababaccb"};
//        String[] testGroup = new String[] {"ababcabba"};
//        String[] testGroup = new String[]{"acccabbcbcbca"};
        String[] testGroup = new String[] {
                // true
                "aaabbbbbbbababab",
                // false
                "aabbbbbbab",
                // false
                "aaabbbbb",
                // true
                "aaabbbbbbb",
                "aaaa","bbbbb","abab"};
//        String[] testGroup = new String[] {
//                "bbbbb",
//                "bbbbbb",
//                "bbbb",
//                "abbbbb",
//                "aabbbbb",
//                "bbbbbc","bbbbbcc","bbbbbccc"
//        };

            for (int i = 0; i < testGroup.length; i++) {
                IParseResult result = MatchFactory.getInstance().useABNFToMatch(key, testGroup[i]);
                System.out.println("result : " + result.isSuccess() + " top : " + result.getTop());
                System.out.println(result.getMatchWords());
            }

        } catch (IOException e) {
            e.printStackTrace();
        }


    }

}
