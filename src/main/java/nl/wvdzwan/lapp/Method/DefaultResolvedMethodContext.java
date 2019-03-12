package nl.wvdzwan.lapp.Method;

import java.util.HashMap;
import java.util.Objects;

public class DefaultResolvedMethodContext implements ResolvedMethodContext {

    private final HashMap<String, ResolvedMethod> dictionary = new HashMap<>();

    @Override
    public synchronized ResolvedMethod make(String namespace, String symbol, String artifact) {
        Objects.requireNonNull(artifact);
        Objects.requireNonNull(namespace);
        Objects.requireNonNull(symbol);

        String key = ResolvedMethod.toID(namespace, symbol, artifact);

        ResolvedMethod val = dictionary.get(key);
        if (val != null) {
            return val;
        }

        val = new ResolvedMethod(namespace, symbol, artifact);
        dictionary.put(key, val);
        return val;

    }
}
