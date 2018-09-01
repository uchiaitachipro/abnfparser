import ca.gobits.bnf.MatchFactory;
import ca.gobits.bnf.io.IOUtils;
import ca.gobits.bnf.parser.IParseResult;

import java.io.IOException;
import java.io.InputStream;

public class TestCommon {

    public static void main(String[] args){
//        testCommon();
        testExecuteRuleDynamically();
    }

    private static void testExecuteRuleDynamically(){
//       IParseResult result = MatchFactory.getInstance().executeRule(
//                "(去 | 送去) (食堂);go_canteen",
//                "去食堂",
//                true);
//
//        System.out.println("result : " + result.isSuccess() + " top : " + result.getTop());
//        System.out.println(result.getMatchWords());

//        IParseResult result = MatchFactory.getInstance().executeRule(
//                "@care(巨屋 [公司]) @care([年] 销售额) [多少];sale",
//                "请问巨屋年销售额多少",
//                true);

        MatchFactory.getInstance().setSceneRule("chat",
                "@start = @care((巨人) | (产品) | (你们)) @care(做什么) [的];",
                "我们巨屋主要从事智能家居的开发、生产与销售，" +
                        "尤其专注于别墅和高端楼宇等无线智能家居系统及产品的研发和销售，" +
                        "以实现家电远程控制、安防报警监控、灯光音乐场景、智能门锁对讲、" +
                        "智能电动窗帘、环境健康医疗等多元化家居产品的智能化。");

        MatchFactory.ResultPair<IParseResult,String> parseResult =
                MatchFactory.getInstance().executeSceneSentence("chat","巨人公司是做什么");

        if (parseResult != null){
            System.out.println("result : " + parseResult.getKey().isSuccess() + " top : "
                    + parseResult.getKey().getTop());
            System.out.println(parseResult.getKey().getMatchWords());
        }

//        result = MatchFactory.getInstance().executeRule(
//                "(去 | 送去) 食堂;go_canteen",
//                "送去食堂",
//                true);
//
//        System.out.println("result : " + result.isSuccess() + " top : " + result.getTop());
//        System.out.println(result.getMatchWords());
//
//        result = MatchFactory.getInstance().executeRule(
//                "(去 | 送去) 食堂;go_canteen",
//                "送去咖啡厅",
//                true);
//
//        System.out.println("result : " + result.isSuccess() + " top : " + result.getTop());
//        System.out.println(result.getMatchWords());
//
//        result = MatchFactory.getInstance().executeRule(
//                "(去 | 送去) 食堂;go_canteen",
//                "还有谁？",
//                true);
//
//        System.out.println("result : " + result.isSuccess() + " top : " + result.getTop());
//        System.out.println(result.getMatchWords());

    }

    private static void testCommon(){
        String key = "common";

        try(InputStream in = TestGroupGrammer.class
                .getClassLoader()
                .getResourceAsStream("common.rule")){

            String rule = IOUtils.toString(in);
            MatchFactory.getInstance().setSceneRule("chat",rule,null);

//            MatchFactory.getInstance().setRule(key,rule);
            String[] testGroup = new String[] {
                    "去会议室",
                    "送去会议室",
                    "到会议室",
                    "送到会议室",
                    "带我去会议室",
                    "那我去会议室",
                    "我要去会议室",
                    "我想去会议室"
            };
            for (int i = 0 ; i < testGroup.length; i++){
                MatchFactory.ResultPair<IParseResult,Object> result = MatchFactory.getInstance().executeSceneSentence("chat",testGroup[i]);
                System.out.println("result : " + result.getKey().isSuccess() + " top : " + result.getKey().getTop());
                System.out.println(result.getKey().getMatchWords());
            }



        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
