package nl.wvdzwan.lapp.protobuf;

import java.util.stream.Collectors;

import nl.wvdzwan.lapp.callgraph.ArtifactRecord;
import nl.wvdzwan.lapp.core.LappPackage;
import nl.wvdzwan.lapp.core.Method;
import nl.wvdzwan.lapp.core.ResolvedMethod;
import nl.wvdzwan.lapp.call.Call;
import nl.wvdzwan.lapp.call.ChaEdge;

public class Protobuf {

    public static Lapp.Package of(LappPackage p) {
        return Lapp.Package.newBuilder()
                .addAllArtifacts(p.artifacts.stream().map(Protobuf::of).collect(Collectors.toList()))
                .addAllMethods(p.methods.stream().map(Protobuf::of).collect(Collectors.toList()))
                .addAllResolvedCalls(p.resolvedCalls.stream().map(Protobuf::of).collect(Collectors.toList()))
                .addAllUnresolvedCalls(p.unresolvedCalls.stream().map(Protobuf::of).collect(Collectors.toList()))
                .addAllCha(p.cha.stream().map(Protobuf::of).collect(Collectors.toList()))
                .addAllUnresolvedCha(p.unresolvedCha.stream().map(Protobuf::of).collect(Collectors.toList()))
                .build();
    }

    public static Lapp.Artifact of(ArtifactRecord artifact) {
        return Lapp.Artifact.newBuilder()
                .setGroup(artifact.groupId)
                .setName(artifact.artifactId)
                .setVersion(artifact.getVersion())
                .build();
    }

    public static Lapp.Method of(Method m) {
        Lapp.Method.Builder builder = Lapp.Method.newBuilder()
                .setNamespace(m.namespace)
                .setSymbol(m.symbol);

        if (m instanceof ResolvedMethod) {
            builder.setArtifact(((ResolvedMethod) m).artifact);
        }

        return builder.build();

    }

    public static Lapp.Call of(Call c) {
        return Lapp.Call.newBuilder()
                .setSource(of(c.source))
                .setTarget(of(c.target))
                .setCallType(of(c.callType))
                .build();

    }

    public static Lapp.Call.CallType of(Call.CallType c) {
        switch (c) {
            case INTERFACE:
                return Lapp.Call.CallType.INTERFACE;
            case VIRTUAL:
                return Lapp.Call.CallType.VIRTUAL;
            case SPECIAL:
                return Lapp.Call.CallType.SPECIAL;
            case STATIC:
                return Lapp.Call.CallType.STATIC;
            case UNKNOWN:
                return Lapp.Call.CallType.UNKNOWN;
            default:
                return Lapp.Call.CallType.UNRECOGNIZED;
        }
    }

    public static Lapp.ChaRelation of(ChaEdge c) {
        return Lapp.ChaRelation.newBuilder()
                .setRelated(of(c.source))
                .setSubject(of(c.target))
                .setType(of(c.type))
                .build();
    }

    public static Lapp.ChaRelation.RelationType of(ChaEdge.ChaEdgeType t) {
        switch (t) {
            case OVERRIDE:
                return Lapp.ChaRelation.RelationType.OVERRIDE;
            case IMPLEMENTS:
                return Lapp.ChaRelation.RelationType.IMPLEMENTS;
            default:
                return Lapp.ChaRelation.RelationType.UNRECOGNIZED;
        }
    }
}
