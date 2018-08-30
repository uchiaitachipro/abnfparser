package ca.gobits.bnf.extention;

public class DefaultMatchCallback implements IMatchCallback {
    @Override
    public boolean isMatch(String symbol) {
        return false;
    }
}
