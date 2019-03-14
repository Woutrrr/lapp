package nl.wvdzwan.lapp.callgraph.outputs.resolved_methods;

import java.util.Set;
import java.util.stream.Collectors;

import org.jgrapht.Graph;

import nl.wvdzwan.lapp.core.Method;
import nl.wvdzwan.lapp.core.ResolvedMethod;
import nl.wvdzwan.lapp.call.Edge;

public class ResolvedMethodExtractor {

    public Set<ResolvedMethod> from(Graph<Method, Edge> graph) {

        return graph.vertexSet().stream()
                .filter(method -> method instanceof ResolvedMethod)
                .map(method -> (ResolvedMethod) method)
                .collect(Collectors.toSet());

    }

}
