package nl.wvdzwan.lapp.core;

public class ResolvedMethod extends Method {

    public static final AnalysisContext DEFAULT_CONTEXT = new DefaultAnalysisContext();

    public final String artifact;

    ResolvedMethod(String namespace, String symbol, String artifact) {
        super(namespace, symbol);

        this.artifact = artifact;
    }

    public String toID() {
        return toID(namespace, symbol, artifact);
    }

    public static String toID(String namespace, String symbol, String artifact) {
        return artifact + "::" + namespace + "." + symbol;
    }

    public static ResolvedMethod findOrCreate(String namespace, String symbol, String artifact) {
        return DEFAULT_CONTEXT.makeResolved(namespace, symbol, artifact);
    }
}
