package nl.wvdzwan.lapp.callgraph.wala;

import com.ibm.wala.types.ClassLoaderReference;
import com.ibm.wala.types.MethodReference;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import nl.wvdzwan.lapp.callgraph.ArtifactRecord;
import nl.wvdzwan.lapp.callgraph.ClassArtifactResolver;
import nl.wvdzwan.lapp.core.LappPackage;
import nl.wvdzwan.lapp.core.Method;
import nl.wvdzwan.lapp.core.ResolvedMethod;
import nl.wvdzwan.lapp.core.UnresolvedMethod;
import nl.wvdzwan.lapp.call.Call;
import nl.wvdzwan.lapp.call.ChaEdge;

public class LappPackageBuilder {

    private static final Logger logger = LogManager.getLogger();
    private final LappPackage lappPackage;

    private ClassArtifactResolver artifactResolver;


    enum MethodType {
        INTERFACE, ABSTRACT, IMPLEMENTATION
    }


    public LappPackageBuilder(ClassArtifactResolver artifactResolver) {
        this.artifactResolver = artifactResolver;
        this.lappPackage = new LappPackage("stub", "version"); // TODO fix package name/version
    }


    public Method addMethod(MethodReference nodeReference, MethodType type) {
        Method method = addMethod(nodeReference);
        method.metadata.put("type", type.toString());

        return method;
    }


    public Method addMethod(MethodReference reference) {

        String namespace = reference.getDeclaringClass().getName().toString().substring(1).replace('/', '.');
        String symbol = reference.getSelector().toString();


        if (inApplicationScope(reference)) {
            ArtifactRecord record = artifactResolver.artifactRecordFromMethodReference(reference);

            ResolvedMethod resolvedMethod = ResolvedMethod.findOrCreate(namespace, symbol, record.getIdentifier());

            lappPackage.addResolvedMethod(resolvedMethod);

            return resolvedMethod;

        } else {
            UnresolvedMethod unresolvedMethod = UnresolvedMethod.findOrCreate(namespace, symbol);
            return unresolvedMethod;
        }
    }

    public boolean addCall(Method source, Method target, Call.CallType type) {

        return lappPackage.addCall(source, target, type);

    }

    public boolean addChaEdge(Method related, ResolvedMethod subject, ChaEdge.ChaEdgeType type) {

        return lappPackage.addChaEdge(related, subject, type);

    }

    public LappPackage getLappPackage() {
        return this.lappPackage;
    }

    private boolean inApplicationScope(MethodReference reference) {
        return reference.getDeclaringClass().getClassLoader().equals(ClassLoaderReference.Application);
    }
}
