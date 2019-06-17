package nl.wvdzwan.lapp.core;

public class UnresolvedMethod extends Method {

    public static final AnalysisContext DEFAULT_CONTEXT = new DefaultAnalysisContext();

    UnresolvedMethod(String namespace, String symbol) {
        super(namespace, symbol);
    }

    public String toID() {
        return toID(namespace, symbol);
    }

    public static String toID(String namespace, String symbol) {
        return "__::" + namespace + "." + symbol;
    }

    public static synchronized UnresolvedMethod findOrCreate(String namespace, String symbol) {
        return DEFAULT_CONTEXT.makeUnresolved(namespace, symbol);
    }
}
