package nl.wvdzwan.lapp.callgraph;

import com.ibm.wala.types.MethodReference;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultDirectedGraph;

import nl.wvdzwan.lapp.IRDotMerger.AnnotatedVertex;
import nl.wvdzwan.lapp.callgraph.outputs.GraphEdge;

public class IRGraphWithDynamicEdges implements IRGraph {

    private Graph<AnnotatedVertex, GraphEdge> graph = new DefaultDirectedGraph<>(GraphEdge.class);
    private ClassToArtifactResolver artifactResolver;

    public IRGraphWithDynamicEdges(ClassToArtifactResolver artifactResolver) {
        this.artifactResolver = artifactResolver;
    }

    @Override
    public AnnotatedVertex addTypedVertex(MethodReference methodReference, String value) {
        AnnotatedVertex vertex = addVertex(methodReference);
        vertex.setAttribute("type", value);

        return vertex;
    }

    @Override
    public AnnotatedVertex addVertex(MethodReference reference) {

        ArtifactRecord record = artifactResolver.artifactRecordFromMethodReference(reference);
        String namespace = reference.getDeclaringClass().getName().toString().substring(1).replace('/', '.');
        String symbol = reference.getSelector().toString();

        AnnotatedVertex result = AnnotatedVertex.findOrCreate(record, namespace, symbol);
        graph.addVertex(result); // TODO handle already existing vertex?
        return result;
    }


    @Override
    public boolean addEdge(AnnotatedVertex nodeVertex, AnnotatedVertex targetVertex, GraphEdge edge) {
        return graph.addEdge(nodeVertex, targetVertex, edge);
    }


    @Override
    public Graph<AnnotatedVertex, GraphEdge> getInnerGraph() {
        return this.graph;
    }
}
