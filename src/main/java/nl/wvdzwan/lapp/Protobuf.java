package nl.wvdzwan.lapp;

import java.util.stream.Collectors;

import nl.wvdzwan.lapp.Method.Method;
import nl.wvdzwan.lapp.Method.ResolvedMethod;
import nl.wvdzwan.lapp.call.Call;
import nl.wvdzwan.lapp.call.ChaEdge;
import nl.wvdzwan.lapp.protos.LappProtos;

public class Protobuf {

    public static LappProtos.Package of(LappPackage p) {
        return LappProtos.Package.newBuilder()
                .setName(p.pkg)
                .setVersion(p.version)
                .addAllMethods(p.methods.stream().map(Protobuf::of).collect(Collectors.toList()))
                .addAllResolvedCalls(p.resolvedCalls.stream().map(Protobuf::of).collect(Collectors.toList()))
                .addAllUnresolvedCalls(p.unresolvedCalls.stream().map(Protobuf::of).collect(Collectors.toList()))
                .addAllCha(p.cha.stream().map(Protobuf::of).collect(Collectors.toList()))
                .build();
    }

    public static LappProtos.Method of(Method m) {
        LappProtos.Method.Builder builder = LappProtos.Method.newBuilder()
                .setNamespace(m.namespace)
                .setSymbol(m.symbol);

        if (m instanceof ResolvedMethod) {
            builder.setArtifact(((ResolvedMethod) m).artifact);
        }

        return builder.build();

    }

    public static LappProtos.Call of(Call c) {
        return LappProtos.Call.newBuilder()
                .setSource(of(c.source))
                .setTarget(of(c.target))
                .setCallType(of(c.callType))
                .build();

    }

    public static LappProtos.Call.CallType of(Call.CallType c) {
        switch (c) {
            case INTERFACE:
                return LappProtos.Call.CallType.INTERFACE;
            case VIRTUAL:
                return LappProtos.Call.CallType.VIRTUAL;
            case SPECIAL:
                return LappProtos.Call.CallType.SPECIAL;
            case STATIC:
                return LappProtos.Call.CallType.STATIC;
            case UNKNOWN:
                return LappProtos.Call.CallType.UNKNOWN;
            default:
                return LappProtos.Call.CallType.UNRECOGNIZED;
        }
    }

    public static LappProtos.ChaRelation of(ChaEdge c) {
        return LappProtos.ChaRelation.newBuilder()
                .setRelated(of(c.source))
                .setSubject(of(c.target))
                .setType(of(c.type))
                .build();
    }

    public static LappProtos.ChaRelation.RelationType of(ChaEdge.ChaEdgeType t) {
        switch (t) {
            case OVERRIDE:
                return LappProtos.ChaRelation.RelationType.OVERRIDE;
            case IMPLEMENTS:
                return LappProtos.ChaRelation.RelationType.IMPLEMENTS;
            default:
                return LappProtos.ChaRelation.RelationType.UNRECOGNIZED;
        }
    }
}
