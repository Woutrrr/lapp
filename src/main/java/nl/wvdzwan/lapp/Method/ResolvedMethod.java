package nl.wvdzwan.lapp.Method;

import java.util.HashMap;

public class ResolvedMethod extends Method {

    public static final ResolvedMethodContext DEFAULT_CONTEXT = new DefaultResolvedMethodContext();

    final private static HashMap<String, ResolvedMethod> dictionary = new HashMap<>();

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
        return DEFAULT_CONTEXT.make(namespace, symbol, artifact);
    }
}
