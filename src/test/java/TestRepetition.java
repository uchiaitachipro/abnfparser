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

            String[] testGroup = new String[]{
                    // true
                    "aab",
                    // true
                    "b",
                    // true
                    "aaaaaaab",
                    // false
                    "c"
            };

//        String[] testGroup = new String[]{
//                // true
//                "acab",
//                // true
//                "b",
//                // true
//                "ab",
//                // true
//                "cb",
//                // false
//                "aaaaaaaaaa",
//                // false
//                "ccc"
//        };

//        String[] testGroup = {
//                // true
//                "bbbabbcbcc"
//        };

//        String[] testGroup = {
//                // true
//                "abcab"
//        };

//        String[] testGroup = {
//                // true
//                "aabcabba",
//                // true
//                "aabcabcabba",
//                // true
//                "aabba",
//                // false
//                "aabc",
//                // false
//                "aabcabc"
//        };

//        String[] testGroup = new String[] {
//                // true
//                "aacababaccb"
//        };

//            String[] testGroup = new String[]{
//                    // true
//                    "ababcabba"
//            };

//        String[] testGroup = new String[]{
//                // true
//                "acccabbcbcbca"
//        };

//            String[] testGroup = new String[]{
//                    // true
//                    "aaabbbbbbbababab",
//                    // false
//                    "aabbbbbbab",
//                    // false
//                    "aaabbbbb",
//                    // true
//                    "aaabbbbbbb",
//                    // false
//                    "aaaa",
//                    // false
//                    "bbbbb",
//                    // false
//                    "abab",
//            };

//            String[] testGroup = new String[]{
//                    // true
//                    "bbbbb",
//                    // false
//                    "bbbbbb",
//                    // false
//                    "bbbb",
//                    // true
//                    "abbbbb",
//                    // false
//                    "aabbbbb",
//                    // true
//                    "bbbbbc",
//                    // true
//                    "bbbbbcc",
//                    // false
//                    "bbbbbccc"
//            };

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
