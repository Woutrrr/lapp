package nl.wvdzwan.lapp.callgraph;

import com.ibm.wala.types.ClassLoaderReference;
import com.ibm.wala.types.MethodReference;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultDirectedGraph;

import nl.wvdzwan.lapp.LappPackage;
import nl.wvdzwan.lapp.Method.Method;
import nl.wvdzwan.lapp.Method.ResolvedMethod;
import nl.wvdzwan.lapp.Method.UnresolvedMethod;
import nl.wvdzwan.lapp.call.Call;
import nl.wvdzwan.lapp.call.ChaEdge;
import nl.wvdzwan.lapp.call.Edge;

public class IRGraphBuilder {

    private static final Logger logger = LogManager.getLogger();
    private final LappPackage lappPackage;

    private Graph<Method, Edge> graph = new DefaultDirectedGraph<>(Edge.class);

    private ClassArtifactResolver artifactResolver;


    enum MethodType {
        INTERFACE, ABSTRACT, IMPLEMENTATION
    }


    public IRGraphBuilder(ClassArtifactResolver artifactResolver) {
        this.artifactResolver = artifactResolver;
        this.lappPackage = new LappPackage("stub", "version");
    }


    public Method addMethod(MethodReference nodeReference, MethodType type) {
        Method method = addMethod(nodeReference);
        method.metadata.put("type", type.toString());

        return method;
    }


    public Method addMethod(MethodReference reference) {

        String namespace = reference.getDeclaringClass().getName().toString().substring(1).replace('/', '.');
        String symbol = reference.getSelector().toString();

        Method method;

        if (inApplicationScope(reference)) {
            ArtifactRecord record = artifactResolver.artifactRecordFromMethodReference(reference);

            ResolvedMethod resolvedMethod = ResolvedMethod.findOrCreate(namespace, symbol, record.getIdentifier());

            // lappPackage
            lappPackage.addResolvedMethod(resolvedMethod);
            // lappPackage
            method = resolvedMethod;

        } else {
            method = UnresolvedMethod.findOrCreate(namespace, symbol);
        }

        graph.addVertex(method);


        return method;
    }

    public boolean addCall(Method source, Method target, Call.CallType type) {
        // lappPackage
        lappPackage.addCall(source, target, type);
        // lappPackage

        Call call = new Call(source, target, type);

        return graph.addEdge(source, target, call);

    }

    public boolean addChaEdge(Method related, ResolvedMethod subject, ChaEdge.ChaEdgeType type) {
        // lappPackage
        lappPackage.addChaEdge(related, subject, type);
        // lappPackage

        return graph.addEdge(related, subject, new ChaEdge(related, subject, type));

    }

    public Graph<Method, Edge> getInnerGraph() {
        return this.graph;
    }

    private boolean inApplicationScope(MethodReference reference) {
        return reference.getDeclaringClass().getClassLoader().equals(ClassLoaderReference.Application);
    }
}
