package nl.wvdzwan.lapp.Method;

import java.util.HashMap;
import java.util.Objects;

public class DefaultUnresolvedMethodContext implements UnresolvedMethodContext {

    private final HashMap<String, UnresolvedMethod> dictionary = new HashMap<>();

    @Override
    public synchronized UnresolvedMethod make(String namespace, String symbol) {
        Objects.requireNonNull(namespace);
        Objects.requireNonNull(symbol);

        String key = UnresolvedMethod.toID(namespace, symbol);

        UnresolvedMethod val = dictionary.get(key);
        if (val != null) {
            return val;
        }

        val = new UnresolvedMethod(namespace, symbol);
        dictionary.put(key, val);
        return val;

    }
}
