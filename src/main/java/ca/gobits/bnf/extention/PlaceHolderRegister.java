package ca.gobits.bnf.extention;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class PlaceHolderRegister {

    private static volatile PlaceHolderRegister instance;

    private Map<String,ICallback> map = new HashMap<>();

    private Map<String,String> placeHolderValues = new HashMap<>();

    private PlaceHolderRegister(){}

    public static PlaceHolderRegister getInstance(){
        if (instance == null){
            synchronized (PlaceHolderRegister.class){
                if (instance == null){
                    instance = new PlaceHolderRegister();
                }
            }
        }
        return instance;
    }

    public void register(String label,ICallback callback){
        if (label == null || callback == null){
            return;
        }

        if (map.containsKey(label)){
            throw new IllegalArgumentException("This label has binded callback,please chose another label");
        }

        map.put(label,callback);
    }

    public void unregister(String label){
        if (map.containsKey(label)){
            map.remove(label);
        }
    }

    public void savePlaceholderValue(String key,String value){
        placeHolderValues.put(key,value);
    }

    public String getPlaceholderRealValue(String key){
        return placeHolderValues.get(key);
    }

    public ICallback getPlaceholderCallback(String label){
        if (!map.containsKey(label)){
            return null;
        }
        return map.get(label);
    }

    public void clear(){
        map.clear();
    }


    public interface ICallback{

        Collection<String> getValue(String label);

        String askNextPhrase(String words,int offset);
    }
}
