package nl.wvdzwan.lapp.callgraph.outputs;

import org.jgrapht.Graph;

import nl.wvdzwan.lapp.Method.Method;

public interface CallGraphOutput {

    boolean export(Graph<Method, GraphEdge> graph);
}
