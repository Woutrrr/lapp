package nl.wvdzwan.lapp.core;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import nl.wvdzwan.lapp.call.Call;
import nl.wvdzwan.lapp.callgraph.ArtifactRecord;

public class LappPackage {
    public final Set<ArtifactRecord> artifacts = new HashSet<>();

    public final Set<Call> resolvedCalls = new HashSet<>();
    public final Set<Call> unresolvedCalls = new HashSet<>();

    public final Set<ClassRecord> classRecords = new HashSet<>();
    public final Map<String, String> metadata = new HashMap<>();

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

    public boolean addClassRecord(ClassRecord classRecord) {
        return classRecords.add(classRecord);
    }

}
