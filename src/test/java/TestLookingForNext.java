import ca.gobits.bnf.MatchFactory;
import ca.gobits.bnf.io.IOUtils;
import ca.gobits.bnf.parser.IParseResult;

import java.io.IOException;
import java.io.InputStream;

public class TestLookingForNext {

    public static void main(String[] args){
        String key = "support_look_for_next";

        try(InputStream in = TestGroupGrammer.class
                .getClassLoader()
                .getResourceAsStream("support_look_for_next.rule")){

            String rule = IOUtils.toString(in);

            MatchFactory.getInstance().setRule(key,rule);
            String[] testGroup = new String[] {
                    "乐乐带我去值班室",
                    "了了领我去值班室",
                    "可可请你去值班室",
                    "了了领我go值班室"
            };
            for (int i = 0 ; i < testGroup.length; i++){
                IParseResult result = MatchFactory.getInstance().useABNFToMatch(key,testGroup[i]);
                System.out.println("result : " + result.isSuccess() + " top : " + result.getTop());
                System.out.println(result.getMatchWords());
            }

            IParseResult result = MatchFactory.getInstance().executeRule(
                    "@care(带|领) @care(去) 值班室;go",
                    "乐乐带我去值班室",
                    false);
            System.out.println("result : " + result.isSuccess() + " top : " + result.getTop());
            System.out.println(result.getMatchWords());

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
