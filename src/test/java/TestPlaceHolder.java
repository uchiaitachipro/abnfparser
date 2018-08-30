import ca.gobits.bnf.MatchFactory;
import ca.gobits.bnf.extention.PlaceHolderRegister;
import ca.gobits.bnf.io.IOUtils;
import ca.gobits.bnf.parser.IParseResult;

import java.io.InputStream;

public class TestPlaceHolder {

    public static void main(String[] args) {
        String key = "support_placeholder";

        try (InputStream in = TestGroupGrammer.class
                .getClassLoader()
                .getResourceAsStream("support_placeholder.rule")) {

            String rule = IOUtils.toString(in);
            MatchFactory.getInstance().setSceneRule("food",rule,"eat");
            MatchFactory.getInstance().registerPlaceholder("foodname",
                    new PlaceHolderRegister.ICallback() {
                @Override
                public String getValue(String label) {
                    return "milk";
                }
            });

            MatchFactory.getInstance().registerPlaceholder("first",
                    new PlaceHolderRegister.ICallback() {
                @Override
                public String getValue(String label) {
                    return "d";
                }
            });

            MatchFactory.getInstance().registerPlaceholder("second",
                    new PlaceHolderRegister.ICallback() {
                        @Override
                        public String getValue(String label) {
                            return "abc";
                        }
                    });

            MatchFactory.getInstance().registerPlaceholder("third",
                    new PlaceHolderRegister.ICallback() {
                        @Override
                        public String getValue(String label) {
                            return "e";
                        }
                    });

//            String[] testGroup = new String[]{
//                    "Iwantmilkplease",
//                    "Iwantwaterplease",
//                    "Iwantplease"
//            };

            String[] testGroup = new String[]{
                    "abec",
                    "dbec",
                    "dabcabcbec",
                    "dabcbec",
            };

            int count = 0;
            for (int i = 0 ; i < testGroup.length; i++){
                 MatchFactory.ResultPair<IParseResult,String> result =  MatchFactory.getInstance()
                         .executeSceneSentence("food",testGroup[i]);

                 if (result != null && !result.getKey().isSuccess()){
                     continue;
                 }

                 System.out.println("result: " + result.getKey() + "match words"
                         + result.getKey().getMatchWords());
                 System.out.println("optional: " + result.getValue());
            }


        } catch (Exception e){
            e.printStackTrace();
        }
    }

}
