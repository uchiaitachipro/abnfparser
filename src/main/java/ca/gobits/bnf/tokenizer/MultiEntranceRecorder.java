package ca.gobits.bnf.tokenizer;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public class MultiEntranceRecorder {

    private static volatile MultiEntranceRecorder instance;

    private Map<String,String> entrances = new LinkedHashMap<>(4);

    private MultiEntranceRecorder(){

    }

    public static MultiEntranceRecorder getInstance(){
        if (instance == null){
            synchronized (MultiEntranceRecorder.class){
                if (instance == null){
                    instance = new MultiEntranceRecorder();
                }
            }
        }
        return instance;
    }

    public void register(String rule,String sentences){
        if (rule == null || rule == "" || sentences == null || sentences == ""){
            return;
        }

        String[] array = sentences.split(";");
        if (array.length < 1){
            return;
        }

        String optionalValue = UUID.randomUUID().toString();
        if (array.length == 2){
            optionalValue = array[1];
        }
        entrances.put(array[0],optionalValue);
    }

    public String getOptionalInfo(String key){
        return entrances.get(key);
    }

    public Iterable<String> getEntrances(){
        if (entrances.size() < 1){
            return null;
        }
        return entrances.keySet();
    }


    public void clear(){
        entrances.clear();
    }

}
