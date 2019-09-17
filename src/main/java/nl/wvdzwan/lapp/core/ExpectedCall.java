package nl.wvdzwan.lapp.core;

public class ExpectedCall {

    public final Method source;
    public final Method target;


    public ExpectedCall(Method source, Method target) {
        this.source = source;
        this.target = target;
    }

    @Override
    public String toString() {
        return "ExpectedCall{" +
                "source=" + source +
                ", target=" + target +
                '}';
    }
}
