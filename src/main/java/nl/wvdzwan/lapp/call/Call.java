package nl.wvdzwan.lapp.call;

import nl.wvdzwan.lapp.Method.Method;
import nl.wvdzwan.lapp.Method.ResolvedMethod;
import nl.wvdzwan.lapp.Method.UnresolvedMethod;

public abstract class Call {

    public final Method source;
    public final Method callee;
    public final String label;


    protected Call(Method source, Method callee, String label) {
        this.source = source;
        this.callee = callee;
        this.label = label;
    }

    public static ResolvedCall make(ResolvedMethod source, ResolvedMethod target, String label) {
        return new ResolvedCall(source, target, label);
    }

    public static UnresolvedCall make(ResolvedMethod source, UnresolvedMethod target, String label) {
        return new UnresolvedCall(source, target, label);
    }

    public static ChaCall makeChaCall(Method source, ResolvedMethod target, String label) {
        return new ChaCall(source, target, label);
    }

}
