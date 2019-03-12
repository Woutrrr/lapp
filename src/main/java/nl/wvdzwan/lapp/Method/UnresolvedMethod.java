package nl.wvdzwan.lapp.Method;

import java.util.HashMap;
import java.util.Objects;

public class UnresolvedMethod extends Method {
    final private static HashMap<String, UnresolvedMethod> dictionary = new HashMap<>();


    private UnresolvedMethod(String namespace, String symbol) {
        super(namespace, symbol);
    }


    public String toID() {
        return toID(namespace, symbol);
    }

    public static String toID(String namespace, String symbol) {
        return "__::" + namespace + "." + symbol;
    }


    public static synchronized UnresolvedMethod findOrCreate(String namespace, String symbol) {

        Objects.requireNonNull(namespace);
        Objects.requireNonNull(symbol);

        String key = toID(namespace, symbol);

        UnresolvedMethod val = dictionary.get(key);
        if (val != null) {
            return val;
        }

        val = new UnresolvedMethod(namespace, symbol);
        dictionary.put(key, val);
        return val;
    }
}
