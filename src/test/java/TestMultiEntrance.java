import ca.gobits.bnf.MatchFactory;
import ca.gobits.bnf.extention.PlaceHolderRegister;
import ca.gobits.bnf.io.IOUtils;
import ca.gobits.bnf.parser.IParseResult;

import java.io.InputStream;
import java.util.*;

public class TestMultiEntrance {


    public static void main(String[] args) {
        try (InputStream in = TestGroupGrammer.class
                .getClassLoader()
                .getResourceAsStream("support_multi_entrance.rule")) {

            String content = IOUtils.toString(in);
            MatchFactory.getInstance().setSceneRule("chat", content, null);

            List<String> rootName = new ArrayList<>();
            rootName.add("可乐");
            MatchFactory.getInstance().registerPlaceholder("robot_name",
                    new PlaceHolderRegister.ICallback() {
                        @Override
                        public Collection<String> getValue(String label) {
                            return rootName;
                        }

                        @Override
                        public String askNextPhrase(String words, int offset) {
                            return null;
                        }
                    });

            List<String> boatPosition = new ArrayList<>();
            boatPosition.add("前台");
            MatchFactory.getInstance().registerPlaceholder("boat_position",
                    new PlaceHolderRegister.ICallback() {
                        @Override
                        public Collection<String> getValue(String label) {
                            return boatPosition;
                        }

                        @Override
                        public String askNextPhrase(String words, int offset) {

                            String subString = words.substring(offset,offset + 2);

                            if (subString.equals("接待")){
                                return subString;
                            }
                            return null;
                        }
                    });

            String[] testGroup = new String[]{
                    "带我去前台好吗",
                    "带我去接待好吗"
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
                System.out.println(result.getKey().getMatchWords());
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
