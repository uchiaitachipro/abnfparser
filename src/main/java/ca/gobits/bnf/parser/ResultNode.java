package ca.gobits.bnf.parser;

public class ResultNode {

    private String key;
    private String value;

    private int startIndex;

    public ResultNode(){}

    public ResultNode(String key,String value,int startIndex){
        this.key = key;
        this.value = value;
        this.startIndex = startIndex;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public int getStartIndex() {
        return startIndex;
    }

    public void setStartIndex(int startIndex) {
        this.startIndex = startIndex;
    }


    @Override
    public String toString() {
        return key + " " + value;
    }
}
