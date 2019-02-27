package nl.wvdzwan.lapp.callgraph;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.types.ClassLoaderReference;
import com.ibm.wala.types.MethodReference;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultDirectedGraph;

import nl.wvdzwan.lapp.IRDotMerger.AnnotatedVertex;
import nl.wvdzwan.lapp.callgraph.outputs.GraphEdge;

public class IRGraphWithDynamicEdges implements IRGraph {

    private Graph<AnnotatedVertex, GraphEdge> graph = new DefaultDirectedGraph<>(GraphEdge.class);
    private Set<AnnotatedVertex> externalNodes = new HashSet<>();
    private List<DynamicEdge> dynamicEdgeList = new ArrayList<>();
    private ClassArtifactResolver artifactResolver;

    public IRGraphWithDynamicEdges(ClassArtifactResolver artifactResolver) {
        this.artifactResolver = artifactResolver;
    }

    @Override
    public AnnotatedVertex addTypedVertex(MethodReference nodeReference, MethodType type) {
        AnnotatedVertex vertex = addVertex(nodeReference);
        vertex.setAttribute("type", type.toString());

        return vertex;
    }

    @Override
    public AnnotatedVertex addTypedVertex(IMethod method, MethodType type) {
        return addTypedVertex(method.getReference(), type);
    }

    @Override
    public AnnotatedVertex addVertex(MethodReference reference) {

        ArtifactRecord record = artifactResolver.artifactRecordFromMethodReference(reference);
        String namespace = reference.getDeclaringClass().getName().toString().substring(1).replace('/', '.');
        String symbol = reference.getSelector().toString();

        AnnotatedVertex result = AnnotatedVertex.findOrCreate(record, namespace, symbol);

        if (reference.getDeclaringClass().getClassLoader().equals(ClassLoaderReference.Application)) {
            graph.addVertex(result); // TODO handle
        } else {
            externalNodes.add(result);
        }

        return result;
    }


    @Override
    public boolean addEdge(AnnotatedVertex nodeVertex, AnnotatedVertex targetVertex, GraphEdge edge) {
        if (externalNodes.contains(nodeVertex)) {
            // Source is external node
            DynamicEdge newEdge = new DynamicEdge(nodeVertex, targetVertex, edge);
            if (!dynamicEdgeList.contains(newEdge)) {
                dynamicEdgeList.add(newEdge);
            }

        } else if (externalNodes.contains(targetVertex)) {
            // Destination is external node
            DynamicEdge newEdge = new DynamicEdge(nodeVertex, targetVertex, edge);
            if (!dynamicEdgeList.contains(newEdge)) {
                dynamicEdgeList.add(newEdge);
            }
        } else {
            return graph.addEdge(nodeVertex, targetVertex, edge);
        }

        return false;
    }


    @Override
    public Graph<AnnotatedVertex, GraphEdge> getInnerGraph() {
        return this.graph;
    }

    @Override
    public List<DynamicEdge> getDynamicEdges() {
        return this.dynamicEdgeList;
    }

    @Override
    public Set<AnnotatedVertex> getExternalNodes() {
        return this.externalNodes;
    }
}
