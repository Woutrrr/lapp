package nl.wvdzwan.lapp.merge;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import nl.wvdzwan.lapp.protobuf.Lapp;
import nl.wvdzwan.lapp.protobuf.Lapp.Method;
import nl.wvdzwan.lapp.protobuf.Lapp.Package;

public class LappPackageMerger {

    private Package.Builder builder = Package.newBuilder();
    private List<Package> toMerge = new ArrayList<>();
    private Map<String, Method> resolvedMethodMap = new HashMap<>();

    public LappPackageMerger add(Package lappPackage) {
        // Only add to work list
        // Wait with merging until we have all resolved methods
        toMerge.add(lappPackage);

        return this;
    }

    public Package merge() {

        // Combine all facts and resolved things
        for (Package p : toMerge) {
            builder
                    .addAllArtifacts(p.getArtifactsList())
                    .addAllMethods(p.getMethodsList())
                    .addAllResolvedCalls(p.getResolvedCallsList())
                    .addAllCha(p.getChaList());

            resolvedMethodMap.putAll(
                    p.getMethodsList()
                            .stream()
                            .collect(
                                    Collectors.toMap(
                                            LappPackageMerger::methodToMethodKey,
                                            Function.identity()
                                    ))
            );
        }

        // resolve unresolvedCalls/ChaRelations
        for (Package p : toMerge) {
            resolveCalls(p.getUnresolvedCallsList());
            resolveCha(p.getUnresolvedChaList());
        }

        return builder.build();
    }


    public void resolveCalls(List<Lapp.Call> unresolvedCalls) {
        for (Lapp.Call call : unresolvedCalls) {

            String methodKey = methodToMethodKey(call.getTarget());
            Lapp.Method resolvedMethod = resolvedMethodMap.get(methodKey);

            if (resolvedMethod != null) {
                builder.addResolvedCalls(Lapp.Call.newBuilder(call).setTarget(resolvedMethod));
            } else {
                builder.addUnresolvedCalls(call);
            }
        }
    }

    public void resolveCha(List<Lapp.ChaRelation> unresolvedChaRelations) {
        for (Lapp.ChaRelation relation : unresolvedChaRelations) {

            String methodKey = methodToMethodKey(relation.getRelated());
            Lapp.Method resolvedMethod = resolvedMethodMap.get(methodKey);

            if (resolvedMethod != null) {
                builder.addCha(Lapp.ChaRelation.newBuilder(relation).setRelated(resolvedMethod));
            } else {
                builder.addUnresolvedCha(relation);
            }
        }
    }

    static String methodToMethodKey(Lapp.Method method) {
        return method.getNamespace() + ":" + method.getSymbol();
    }

}
