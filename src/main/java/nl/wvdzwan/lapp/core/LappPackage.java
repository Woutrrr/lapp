package nl.wvdzwan.lapp.core;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import nl.wvdzwan.lapp.call.Call;

public class LappPackage {
    public final Set<String> artifacts = new HashSet<>();

    public final Set<Call> resolvedCalls = new HashSet<>();
    public final Set<Call> unresolvedCalls = new HashSet<>();

    public final Set<ClassRecord> classRecords = new HashSet<>();
    public final Map<String, String> metadata = new HashMap<>();

    public boolean addCall(Method source, Method target, Call.CallType type, int lineNumber, int programCounter) {

        if (target instanceof ResolvedMethod
                && source instanceof ResolvedMethod) {
            return addResolvedCall((ResolvedMethod) source, (ResolvedMethod) target, type, lineNumber, programCounter);
        }

        return addUnresolvedCall(source, target, type, lineNumber, programCounter);
    }

    private boolean addUnresolvedCall(Method source, Method target, Call.CallType type, int lineNumber, int programCounter) {
        Call call = new Call(source, target, type, lineNumber, programCounter);

        return unresolvedCalls.add(call);
    }

    private boolean addResolvedCall(ResolvedMethod source, ResolvedMethod target, Call.CallType type, int lineNumber, int programCounter) {
        Call call = new Call(source, target, type, lineNumber, programCounter);

        return resolvedCalls.add(call);
    }

    public boolean addClassRecord(ClassRecord classRecord) {
        return classRecords.add(classRecord);
    }

}
