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
                    "大",
                    "声大",
                    "大声点",
                    "大点声",
                    "声音大",
                    "声音再大一点",
                    "声音再大点",
                    "声音大点",
                    "声音大一点",
                    "再大一点",
                    "我听不见",
                    "听不见",
                    "小声",
                    "小声点",
                    "小声点",
                    "小点声",
                    "声音小",
                    "声音再小一点",
                    "声音再小点",
                    "声音小点",
                    "声音小一点",
                    "再小一点",
                    "太吵了",
                    "最大声",
                    "声音最大",
                    "音量最大",
                    "最大音量",
                    "最小声",
                    "声音最小",
                    "音量最小",
                    "最小音量",
                    "静音",
                    "不要说了",
                    "安静"
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
