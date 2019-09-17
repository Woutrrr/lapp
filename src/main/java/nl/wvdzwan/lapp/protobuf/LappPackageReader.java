package nl.wvdzwan.lapp.protobuf;

import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import nl.wvdzwan.lapp.call.Call;
import nl.wvdzwan.lapp.callgraph.ArtifactRecord;
import nl.wvdzwan.lapp.core.ClassRecord;
import nl.wvdzwan.lapp.core.ExpectedCall;
import nl.wvdzwan.lapp.core.LappPackage;
import nl.wvdzwan.lapp.core.Method;
import nl.wvdzwan.lapp.core.ResolvedMethod;
import nl.wvdzwan.lapp.core.UnresolvedMethod;

public class LappPackageReader {


    public static LappPackage from(Lapp.Package proto) {

        LappPackage lappPackage = new LappPackage();

        lappPackage.artifacts.addAll(proto.getArtifactsList());

        lappPackage.classRecords.addAll(fromClassRecords(proto.getClassRecordsList()));
        lappPackage.resolvedCalls.addAll(fromCalls(proto.getResolvedCallsList()));
        lappPackage.unresolvedCalls.addAll(fromCalls(proto.getUnresolvedCallsList()));

        lappPackage.metadata.putAll(new HashMap<>(proto.getMetadataMap()));

        return lappPackage;
    }


    public static Set<ArtifactRecord> fromArtifacts(List<Lapp.Artifact> proto) {
        return proto.stream()
                .map(a -> new ArtifactRecord(a.getGroup(), a.getName(), a.getVersion()))
                .collect(Collectors.toSet());
    }


    public static ResolvedMethod fromResolvedMethod(Lapp.Method proto) {
        return ResolvedMethod.findOrCreate(proto.getNamespace(), proto.getSymbol(), proto.getArtifact());
    }

    public static Method fromMethod(Lapp.Method proto) {
        if (proto.getArtifact() != null && !proto.getArtifact().equals("")) {
            return fromResolvedMethod(proto);
        } else {
            return UnresolvedMethod.findOrCreate(proto.getNamespace(), proto.getSymbol());
        }
    }

    private static Set<Call> fromCalls(List<Lapp.Call> proto) {
        return proto.stream()
                .map(c -> new Call(
                        fromMethod(c.getSource()),
                        fromMethod(c.getTarget()),
                        fromCallType(c.getCallType()
                        )))
                .collect(Collectors.toSet());
    }

    private static Set<ExpectedCall> fromExpectedCalls(List<Lapp.ExpectedCall> proto) {
        return proto.stream()
                .map(c -> new ExpectedCall(
                        fromMethod(c.getSource()),
                        fromMethod(c.getTarget())
                        ))
                .collect(Collectors.toSet());
    }

    private static Set<ClassRecord> fromClassRecords(List<Lapp.ClassRecord> proto) {
        return proto.stream()
                .map(c -> {
                    ClassRecord result = new ClassRecord(c.getPackage(), c.getName());
                    result.setSuperClass(c.getSuperClass());
                    result.interfaces.addAll(c.getInterfacesList());
                    result.methods.addAll(c.getMethodsList());
                    result.expectedCalls.addAll(fromExpectedCalls(c.getExpectedCallsList()));

                    result.isPublic = c.getPublic();
                    result.isPrivate = c.getPrivate();
                    result.isInterface = c.getInterface();
                    result.isAbstract = c.getAbstract();

                    return result;
                })
                .collect(Collectors.toSet());
    }


    public static Call.CallType fromCallType(Lapp.Call.CallType proto) {
        switch (proto) {
            case INTERFACE:
                return Call.CallType.INTERFACE;
            case VIRTUAL:
                return Call.CallType.VIRTUAL;
            case SPECIAL:
                return Call.CallType.SPECIAL;
            case STATIC:
                return Call.CallType.STATIC;
            case RESOLVED:
                return Call.CallType.RESOLVED_DISPATCH;
            case UNRECOGNIZED:
            case UNKNOWN:
            default:
                return Call.CallType.UNKNOWN;
        }
    }
}
