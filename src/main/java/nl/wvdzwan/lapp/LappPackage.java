package nl.wvdzwan.lapp;

import java.util.List;
import java.util.Map;
import java.util.Set;

import nl.wvdzwan.lapp.call.ResolvedCall;
import nl.wvdzwan.lapp.call.UnresolvedCall;

public class LappPackage {
    public final String pkg;
    public final String Version;
    public final Set<String> functions;
    public final List<ResolvedCall> resolvedCalls;
    public final List<UnresolvedCall> unresolvedCall;
    public final Map<String, String> metadata;

    public LappPackage(String pkg, String version, Set<String> functions, List<ResolvedCall> resolvedCalls, List<UnresolvedCall> unresolvedCall, Map<String, String> metadata) {
        this.pkg = pkg;
        Version = version;
        this.functions = functions;
        this.resolvedCalls = resolvedCalls;
        this.unresolvedCall = unresolvedCall;
        this.metadata = metadata;
    }


}
