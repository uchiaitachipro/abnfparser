import ca.gobits.bnf.MatchFactory;
import ca.gobits.bnf.io.IOUtils;
import ca.gobits.bnf.parser.IParseResult;

import java.io.IOException;
import java.io.InputStream;

public class TestOptional {

    public static void main(String[] args) {

        String key = "support_optional";

        try (InputStream in = TestGroupGrammer.class
                .getClassLoader()
                .getResourceAsStream("support_optional.rule")) {

            String rule = IOUtils.toString(in);

            MatchFactory.getInstance().setRule(key, rule);
//            String[] testGroup = new String[]{
//                    // true
//                    "水可载舟",
//                    // true
//                    "水可覆舟",
//                    // true
//                    "水可舟",
//                    // false
//                    "亦可赛艇",
//                    // false
//                    "水很舟"
//            };

//            String[] testGroup = new String[]{
//                    // true
//                    "水可载舟",
//                    // true
//                    "水可载Naive舟",
//                    // true
//                    "水可覆舟",
//                    // false
//                    "水可载覆舟"
//            };

            String[] testGroup = new String[]{
                    "acd",// true
                    "abd",// true
                    "d" , // true
                    "ad", // false
                    "b", // false

            };

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
