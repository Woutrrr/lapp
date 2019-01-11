package nl.wvdzwan.timemachine.filter;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.jgrapht.graph.AbstractBaseGraph;

import nl.wvdzwan.timemachine.IRDotMerger.AnnotatedVertex;
import nl.wvdzwan.timemachine.callgraph.outputs.GraphEdge;

public class GraphFilter {

    private final AbstractBaseGraph<AnnotatedVertex, GraphEdge> graph;

    public GraphFilter(AbstractBaseGraph<AnnotatedVertex, GraphEdge> sourceGraph) {


        graph = (AbstractBaseGraph<AnnotatedVertex, GraphEdge>) sourceGraph.clone();
    }


    public boolean libraries(List<String> allowedLibraries, boolean inverted) {

        Set<AnnotatedVertex> verticesToRemove = graph.vertexSet().stream()
                .filter(annotatedVertex -> {
                    String identifier = annotatedVertex.getArtifactRecord().getIdentifier();
                    return allowedLibraries.contains(identifier) == inverted;
                })
                .collect(Collectors.toSet());




        return graph.removeAllVertices(verticesToRemove);


    }

    public AbstractBaseGraph<AnnotatedVertex, GraphEdge> getGraph() {
        return graph;
    }
}
