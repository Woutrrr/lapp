package nl.wvdzwan.lapp.protobuf;

import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import nl.wvdzwan.lapp.call.Call;
import nl.wvdzwan.lapp.call.ChaEdge;
import nl.wvdzwan.lapp.callgraph.ArtifactRecord;
import nl.wvdzwan.lapp.core.LappPackage;
import nl.wvdzwan.lapp.core.Method;
import nl.wvdzwan.lapp.core.ResolvedMethod;
import nl.wvdzwan.lapp.core.UnresolvedMethod;

public class LappPackageReader {


    public static LappPackage from(Lapp.Package proto) {

        LappPackage lappPackage = new LappPackage();

        lappPackage.artifacts.addAll(fromArtifacts(proto.getArtifactsList()));

        lappPackage.methods.addAll(fromMethods(proto.getMethodsList()));

        lappPackage.resolvedCalls.addAll(fromCalls(proto.getResolvedCallsList()));
        lappPackage.unresolvedCalls.addAll(fromCalls(proto.getUnresolvedCallsList()));
        lappPackage.cha.addAll(fromCha(proto.getChaList()));
        lappPackage.unresolvedCha.addAll(fromCha(proto.getUnresolvedChaList()));

        lappPackage.metadata.putAll(new HashMap<>(proto.getMetadataMap()));

        return lappPackage;
    }


    public static Set<ArtifactRecord> fromArtifacts(List<Lapp.Artifact> proto) {
        return proto.stream()
                .map(a -> new ArtifactRecord(a.getGroup(), a.getName(), a.getVersion()))
                .collect(Collectors.toSet());
    }

    public static Set<ResolvedMethod> fromMethods(List<Lapp.Method> proto) {
        return proto.stream()
                .map(m -> fromResolvedMethod(m))
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

    public static Set<ChaEdge> fromCha(List<Lapp.ChaRelation> proto) {
        return proto.stream()
                .map(c -> new ChaEdge(fromMethod(c.getRelated()), fromResolvedMethod(c.getSubject()), fromChaType(c.getType())))
                .collect(Collectors.toSet());
    }


    public static ChaEdge.ChaEdgeType fromChaType(Lapp.ChaRelation.RelationType proto) {
        switch (proto) {
            case IMPLEMENTS:
                return ChaEdge.ChaEdgeType.IMPLEMENTS;
            case OVERRIDE:
                return ChaEdge.ChaEdgeType.OVERRIDE;
            case UNKNOWN:
            case UNRECOGNIZED:
            default:
                return ChaEdge.ChaEdgeType.UNKNOWN;
        }
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
            case UNRECOGNIZED:
            case UNKNOWN:
            default:
                return Call.CallType.UNKNOWN;
        }
    }
}
