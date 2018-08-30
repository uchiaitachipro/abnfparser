import ca.gobits.bnf.MatchFactory;
import ca.gobits.bnf.io.IOUtils;
import ca.gobits.bnf.parser.IParseResult;

import java.io.IOException;
import java.io.InputStream;

public class TestOptional {

    public static void main(String[] args){

        String key = "support_optional";

        try(InputStream in = TestGroupGrammer.class
                .getClassLoader()
                .getResourceAsStream("support_optional.rule")){

            String rule = IOUtils.toString(in);

            MatchFactory.getInstance().setRule(key,rule);
//            String[] testGroup = new String[] {
//                    "水可载舟", "水可覆舟", "亦可赛艇","水可舟","水很舟"};
//            String[] testGroup = new String[] {
//                    "水可载舟"};
            String[] testGroup = new String[] {
                    "前几年是不是是不是过的很好",
                    "假疫苗制造者过的很好水可载舟是不是是不是"
            };
            for (int i = 0 ; i < testGroup.length; i++){
                IParseResult result = MatchFactory.getInstance().useABNFToMatch(key,testGroup[i]);
                System.out.println("result : " + result.isSuccess() + " top : " + result.getTop());
                System.out.println(result.getMatchWords());
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
