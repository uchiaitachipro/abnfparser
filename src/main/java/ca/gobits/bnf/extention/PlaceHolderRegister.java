package ca.gobits.bnf.extention;

import java.util.HashMap;
import java.util.Map;

public class PlaceHolderRegister {

    private static volatile PlaceHolderRegister instance;

    private Map<String,ICallback> map = new HashMap<>();

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

        map.put(label,callback);
    }

    public void unregister(String label){
        if (map.containsKey(label)){
            map.remove(label);
        }
    }

    public ICallback getPlaceholderValue(String label){
        if (!map.containsKey(label)){
            return null;
        }
        return map.get(label);
    }

    public void clear(){
        map.clear();
    }


    public interface ICallback{
        String getValue(String label);
    }
}
