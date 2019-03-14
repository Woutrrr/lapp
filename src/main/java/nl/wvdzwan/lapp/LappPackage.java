package nl.wvdzwan.lapp;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import nl.wvdzwan.lapp.Method.Method;
import nl.wvdzwan.lapp.Method.ResolvedMethod;
import nl.wvdzwan.lapp.call.Call;
import nl.wvdzwan.lapp.call.ChaEdge;

public class LappPackage {
    public final String pkg;
    public final String version;
    public final Set<ResolvedMethod> methods = new HashSet<>();
    public final Set<Call> resolvedCalls = new HashSet<>();
    public final Set<Call> unresolvedCalls = new HashSet<>();

    public final Set<ChaEdge> cha = new HashSet<>();

    public final Map<String, String> metadata = new HashMap<>();


    public LappPackage(String pkg, String version) {
        this.pkg = pkg;
        this.version = version;
    }

    public void addResolvedMethod(ResolvedMethod resolvedMethod) {
        methods.add(resolvedMethod);
    }

    public boolean addCall(Method source, Method target, Call.CallType type) {

        if (target instanceof ResolvedMethod
                && source instanceof ResolvedMethod) {
            return addResolvedCall((ResolvedMethod) source, (ResolvedMethod) target, type);
        }

        return addUnresolvedCall(source, target, type);
    }

    private boolean addUnresolvedCall(Method source, Method target, Call.CallType type) {

        Call call = new Call(source, target, type);

        return unresolvedCalls.add(call);
    }

    private boolean addResolvedCall(ResolvedMethod source, ResolvedMethod target, Call.CallType type) {
        Call call = new Call(source, target, type);

        return resolvedCalls.add(call);
    }

    public boolean addChaEdge(Method related, ResolvedMethod subject, ChaEdge.ChaEdgeType type) {

        return cha.add(new ChaEdge(related, subject, type));

    }

}
