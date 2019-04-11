package ca.gobits.bnf;

import ca.gobits.bnf.extention.ParserContextPool;
import ca.gobits.bnf.extention.PlaceHolderRegister;
import ca.gobits.bnf.parser.*;
import ca.gobits.bnf.tokenizer.ABNFTokenizerFactoryImpl;
import ca.gobits.bnf.tokenizer.Token;

import java.io.InputStream;
import java.util.*;

public class MatchFactory {

    private static volatile MatchFactory instance;

    ISequenceFactory factory = new ABNFSequenceFactoryImpl();
    ABNFTokenizerFactoryImpl tokenizerFactory = new ABNFTokenizerFactoryImpl();

    Map<String,IParser> parserPools = new HashMap<>(8);

    Map<String,List<IParser>> scenesMap = new LRUCache<>(512);

    private MatchFactory(){

    }

    public static MatchFactory getInstance(){
        if (instance == null){
            synchronized (MatchFactory.class){
                if (instance == null){
                    instance = new MatchFactory();
                }
            }
        }
        return instance;
    }

    public void setRule(String tag,String rule){
        Map<String, List<BNFSequence>> map = factory.map(rule);
        IParser parser = new ABNFParserImpl(map);
        parserPools.put(tag,parser);
    }

    public <T> void setSceneRule(String scene, String rule,T obj){
        Map<String, List<BNFSequence>> map = factory.map(rule);
        setRuleInternal(map,scene,obj);
    }

    public void setRule(String tag,InputStream stream){
        Map<String, List<BNFSequence>> map = factory.map(stream);
        IParser parser = new ABNFParserImpl(map);
        parserPools.put(tag,parser);
    }

    public <T> void setSceneRule(String scene,InputStream stream,T obj){
        Map<String, List<BNFSequence>> map = factory.map(stream);
        setRuleInternal(map,scene,obj);
    }

    private <T> void setRuleInternal(Map<String, List<BNFSequence>> map, String scene, T obj){
        IParser parser = new ABNFParserImpl(map);
        parser.setTag(obj);
        if (scenesMap.containsKey(scene)){
            scenesMap.get(scene).add(parser);
            return;
        }

        List<IParser> list = new LinkedList<>();
        list.add(parser);
        scenesMap.put(scene,list);
    }

    public IParseResult executeRule(String rule,String content, boolean needCache){

        if (rule == null || rule == "" || content == null || content == ""){
            return null;
        }

        String[] sentences = rule.split(";");

        if (sentences.length == 0){
            return null;
        }

        if (!sentences[0].startsWith("@start")){
            sentences[0] = "@start = " + sentences[0];
        }

        if (sentences.length == 2 && parserPools.containsKey(sentences[1])){
            return parserPools.get(sentences[1]).parse(content);
        }

        Map<String, List<BNFSequence>> map = factory.map(sentences[0]);
        IParser parser = new ABNFParserImpl(map);

        if (sentences.length == 2 && sentences[1] != null && needCache){
            parserPools.put(sentences[1],parser);
        }

        IParseResult result = parser.parse(content);
        result.setMatchWords(sentences[1]);
        return result;
    }


    public <T> ResultPair<IParseResult,T> executeSceneSentence(String scene,String content) {
        if (content == null || content == "") {
            return null;
        }

        if (scenesMap.containsKey(scene)) {
            List<IParser> list = scenesMap.get(scene);
            for (IParser parser : list) {

                IParseResult result = parser.parse(content);

                if (result == null){
                    continue;
                }

                if (result.isSuccess()) {
                    return new ResultPair<>(result,(T)parser.getTag());
                }
            }
        }
        return null;
    }


    public IParseResult useABNFToMatch(String tag,String content) {
        if (!parserPools.containsKey(tag)){
            return null;
        }
        Token token = tokenizerFactory.tokens(content);
        return parserPools.get(tag).parse(token,content);
    }

    public void registerPlaceholder(String label, PlaceHolderRegister.ICallback callback){
        PlaceHolderRegister.getInstance().register(label,callback);
    }

    public void unRegisterPlaceholder(String label){
        PlaceHolderRegister.getInstance().unRegister(label);
    }

    public void clearCache(){
        PlaceHolderRegister.getInstance().clearAllData();
        ParserContextPool.getInstance().clear();
    }

    public static void main(String[] args){

    }

    public static class LRUCache<K, V> extends LinkedHashMap<K, V> {
        private final int maxCacheSize;

        public LRUCache(int cacheSize) {
            super((int) Math.ceil(cacheSize / 0.75) + 1, 0.75f, true);
            maxCacheSize = cacheSize;
        }

        @Override
        protected boolean removeEldestEntry(Map.Entry eldest) {
            return size() > maxCacheSize;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            for (Map.Entry<K, V> entry : entrySet()) {
                sb.append(String.format("%s:%s ", entry.getKey(), entry.getValue()));
            }
            return sb.toString();
        }
    }


    public static class ResultPair<K,V> {

        private K key;
        private V value;

        public ResultPair(K key,V value){
            this.key = key;
            this.value = value;
        }

        public K getKey() {
            return key;
        }

        public void setKey(K key) {
            this.key = key;
        }

        public V getValue() {
            return value;
        }

        public void setValue(V value) {
            this.value = value;
        }
    }


}
