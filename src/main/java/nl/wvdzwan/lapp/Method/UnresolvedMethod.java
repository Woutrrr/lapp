package nl.wvdzwan.lapp.Method;

public class UnresolvedMethod extends Method {
    public static final UnresolvedMethodContext DEFAULT_CONTEXT = new DefaultUnresolvedMethodContext();


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
        return DEFAULT_CONTEXT.make(namespace, symbol);
    }
}
