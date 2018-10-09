import ca.gobits.bnf.MatchFactory;
import ca.gobits.bnf.extention.PlaceHolderRegister;
import ca.gobits.bnf.io.IOUtils;
import ca.gobits.bnf.parser.IParseResult;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class TestPlaceHolder {

    public static void main(String[] args) {
        String key = "support_placeholder";

        try (InputStream in = TestGroupGrammer.class
                .getClassLoader()
                .getResourceAsStream("support_placeholder.rule")) {

            String rule = IOUtils.toString(in);
            MatchFactory.getInstance().setSceneRule("food",rule,"eat");
            List<String> foodNames = new ArrayList<>();
            foodNames.add("milk");
            MatchFactory.getInstance().registerPlaceholder("foodname",
                    new PlaceHolderRegister.ICallback() {
                @Override
                public Collection<String> getValue(String label) {
                    return foodNames;
                }
            });

            List<String> firstList = new ArrayList<>();
            firstList.add("d");
            MatchFactory.getInstance().registerPlaceholder("first",
                    new PlaceHolderRegister.ICallback() {
                @Override
                public Collection<String> getValue(String label) {
                    return firstList;
                }
            });

            List<String> second = new ArrayList<>();
            second.add("abc");
            MatchFactory.getInstance().registerPlaceholder("second",
                    new PlaceHolderRegister.ICallback() {
                        @Override
                        public Collection<String> getValue(String label) {
                            return second;
                        }
                    });

            List<String> thirdList = new ArrayList<>();
            thirdList.add("e");
            MatchFactory.getInstance().registerPlaceholder("third",
                    new PlaceHolderRegister.ICallback() {
                        @Override
                        public Collection<String> getValue(String label) {
                            return thirdList;
                        }
                    });

            List<String> fifthList = new ArrayList<>();
            fifthList.add("uchia");
            MatchFactory.getInstance().registerPlaceholder("fifth", new PlaceHolderRegister.ICallback() {
                @Override
                public Collection<String> getValue(String label) {
                    return fifthList;
                }
            });

//            String[] testGroup = new String[]{
//                    "Iwantmilkplease",
//                    "Iwantwaterplease",
//                    "Iwantplease"
//            };

//            String[] testGroup = new String[]{
//                    "abec",
//                    "dbec",
//                    "dabcabcbec",
//                    "dabcbec",
//            };

            String[] testGroup = new String[]{
                    "Iwantuchia"
            };

            int count = 0;
            for (int i = 0 ; i < testGroup.length; i++){
                 MatchFactory.ResultPair<IParseResult,String> result =  MatchFactory.getInstance()
                         .executeSceneSentence("food",testGroup[i]);

                 if (result == null || !result.getKey().isSuccess()){
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
