package nl.wvdzwan.lapp.Method;

public class ResolvedMethod extends Method {

    public final String artifact;

    public ResolvedMethod(String namespace, String symbol, String artifact) {
        super(namespace, symbol);

        this.artifact = artifact;
    }

}
