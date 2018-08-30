import ca.gobits.bnf.MatchFactory;
import ca.gobits.bnf.io.IOUtils;
import ca.gobits.bnf.parser.IParseResult;

import java.io.InputStream;

public class TestMultiEntrance {


    public static void main(String[] args) {
        try (InputStream in = TestGroupGrammer.class
                .getClassLoader()
                .getResourceAsStream("support_multi_entrance.rule")) {

            String content = IOUtils.toString(in);
            MatchFactory.getInstance().setSceneRule("chat", content, null);

            String[] testGroup = new String[]{
                    "小声",
                    "小声点",
                    "小点声",
                    "声音小",
                    "声音再小一点",
                    "声音再小点",
                    "声音小点",
                    "声音小一点",
                    "再小一点儿",
                    "太吵了",
                    "小点儿声",
                    "小点声儿",
                    "音量调到最小",
                    "最小声",
                    "声音最小",
                    "音量最小",
                    "最小音量",
                    "最低音量",
                    "静音",
                    "你不要说",
                    "安静",
                    "最大声",
                    "声音最大",
                    "音量最大",
                    "最大音量",
                    "大点声",
                    "大声",
                    "大声点",
                    "声音大",
                    "声音再大一点",
                    "声音再大点",
                    "声音大点",
                    "声音大一点",
                    "再大点",
                    "再大一点",
                    "我听不见",
                    "听不见",
                    "大点儿声",
                    "大声点儿",
                    "大点声儿"
            };

            int count = 0;
            for (int i = 0; i < testGroup.length; i++) {
                MatchFactory.ResultPair<IParseResult, String> result = MatchFactory
                        .getInstance().executeSceneSentence("chat", testGroup[i]);

                if (result == null) {
                    continue;
                }

                if (result.getKey().isSuccess()) {
                    count++;
                }

                System.out.println("String: " + testGroup[i] + "  result: " + result.getKey().isSuccess());
                System.out.println("optional: " + result.getValue());

//                System.out.println("result : " + result.getKey().isSuccess() + " top : " + result.getKey().getTop());
//                System.out.println(result.getKey().getMatchWords());
//                System.out.println("optional: " + result.getValue());

            }

            System.out.println("pass: " + count + " all: " + testGroup.length);


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
