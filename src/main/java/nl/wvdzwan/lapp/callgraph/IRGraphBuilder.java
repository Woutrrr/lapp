package nl.wvdzwan.lapp.callgraph;

import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.types.ClassLoaderReference;
import com.ibm.wala.types.MethodReference;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultDirectedGraph;

import nl.wvdzwan.lapp.Method.Method;
import nl.wvdzwan.lapp.Method.ResolvedMethod;
import nl.wvdzwan.lapp.Method.UnresolvedMethod;
import nl.wvdzwan.lapp.callgraph.outputs.GraphEdge;

public class IRGraphBuilder {

    private Graph<Method, GraphEdge> graph = new DefaultDirectedGraph<>(GraphEdge.class);

    private ClassArtifactResolver artifactResolver;


    enum MethodType {
        INTERFACE, ABSTRACT, IMPLEMENTATION
    }


    public IRGraphBuilder(ClassArtifactResolver artifactResolver) {
        this.artifactResolver = artifactResolver;
    }


    public Method addMethod(MethodReference nodeReference, MethodType type) {
        Method method = addMethod(nodeReference);
        method.metadata.put("type", type.toString());

        return method;
    }


    public Method addMethod(IMethod method, MethodType type) {
        return addMethod(method.getReference(), type);
    }


    public Method addMethod(MethodReference reference) {

        String namespace = reference.getDeclaringClass().getName().toString().substring(1).replace('/', '.');
        String symbol = reference.getSelector().toString();

        Method method;

        if (inApplicationScope(reference)) {
            ArtifactRecord record = artifactResolver.artifactRecordFromMethodReference(reference);

            method = new ResolvedMethod(namespace, symbol, record.getIdentifier());
        } else {
            method = new UnresolvedMethod(namespace, symbol);
        }

        graph.addVertex(method);

        return method;
    }

    public boolean addEdge(Method source, Method target, GraphEdge edge) {
        return graph.addEdge(source, target, edge);
    }

    public Graph<Method, GraphEdge> getInnerGraph() {
        return this.graph;
    }

    private boolean inApplicationScope(MethodReference reference) {
        return reference.getDeclaringClass().getClassLoader().equals(ClassLoaderReference.Application);
    }
}
