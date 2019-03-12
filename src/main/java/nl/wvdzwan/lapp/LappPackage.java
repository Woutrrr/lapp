package nl.wvdzwan.lapp;

import java.util.List;
import java.util.Map;
import java.util.Set;

import nl.wvdzwan.lapp.call.Call;

public class LappPackage {
    public final String pkg;
    public final String version;
    public final Set<String> functions;
    public final List<Call> resolvedCalls;
    public final List<Call> unresolvedCall;
    public final Map<String, String> metadata;

    public LappPackage(String pkg, String version, Set<String> functions, List<Call> resolvedCalls, List<Call> unresolvedCall, Map<String, String> metadata) {
        this.pkg = pkg;
        this.version = version;
        this.functions = functions;
        this.resolvedCalls = resolvedCalls;
        this.unresolvedCall = unresolvedCall;
        this.metadata = metadata;
    }


}
