package nl.wvdzwan.lapp.Method;

import java.util.HashMap;
import java.util.Objects;

public class ResolvedMethod extends Method {

    final private static HashMap<String, ResolvedMethod> dictionary = new HashMap<>();

    public final String artifact;

    private ResolvedMethod(String namespace, String symbol, String artifact) {
        super(namespace, symbol);

        this.artifact = artifact;
    }

    public String toID() {
        return toID(namespace, symbol, artifact);
    }

    public static String toID(String namespace, String symbol, String artifact) {
        return artifact + "::" + namespace + "." + symbol;
    }


    public static synchronized ResolvedMethod findOrCreate(String namespace, String symbol, String artifact) {
        Objects.requireNonNull(artifact);
        Objects.requireNonNull(namespace);
        Objects.requireNonNull(symbol);

        String key = toID(namespace, symbol, artifact);

        ResolvedMethod val = dictionary.get(key);
        if (val != null) {
            return val;
        }

        val = new ResolvedMethod(namespace, symbol, artifact);
        dictionary.put(key, val);
        return val;
    }
}
