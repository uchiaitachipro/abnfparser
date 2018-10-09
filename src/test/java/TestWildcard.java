import ca.gobits.bnf.MatchFactory;
import ca.gobits.bnf.io.IOUtils;
import ca.gobits.bnf.parser.IParseResult;

import java.io.InputStream;

public class TestWildcard {

    public static void main(String[] args) {
        String key = "support_wildcard";

        try (InputStream in = TestGroupGrammer.class
                .getClassLoader()
                .getResourceAsStream("support_wildcard.rule")) {

            String rule = IOUtils.toString(in);
            MatchFactory.getInstance().setRule(key, rule);

//            String[] testGroup = new String[]{
//                    "带我",
//                    "带值班室",
//                    "带我值班室",
//                    "带我去值班室",
//
//            };

//            String[] testGroup = new String[]{
//                    // true
//                    "ababaabababaab",
////                    // false
//                    "abababababababa",
////                    // true
//                    "abababaab",
////                    // true
//                    "aabbbababaab"
//
//            };

            String[] testGroup = new String[]{
//                    // true
//                    "ba",
//                    // true
//                    "babab",
                    // false
                    "baaba"
            };

//            String[] testGroup = new String[]{
//                    // true
//                    "a",
//                    // true
//                    "asdfdfdffdfddfdf",
//                    // false
//                    "uwhxdfdffdfddfdjff"
//            };

//            String[] testGroup = new String[] {"你好能带我去值班室吗"};

//            String[] testGroup = new String[]{
//                    //true
//                    "abvcba",
//                    //true
//                    "ahdba",
//                    //true
//                    "aab",
//                    //true
//                    "aba"
//            };

//            String[] testGroup = new String[]{
//                    //true
//                    "aabcdba"
//            };

            for (int i = 0; i < testGroup.length; i++) {
                IParseResult result = MatchFactory.getInstance().useABNFToMatch(key, testGroup[i]);
                System.out.println("result : " + result.isSuccess() + " top : " + result.getTop());
                System.out.println(result.getMatchWords());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
