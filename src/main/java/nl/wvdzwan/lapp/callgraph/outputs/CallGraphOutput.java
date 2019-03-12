package nl.wvdzwan.lapp.callgraph.outputs;

import org.jgrapht.Graph;

import nl.wvdzwan.lapp.Method.Method;
import nl.wvdzwan.lapp.call.Edge;

public interface CallGraphOutput {

    boolean export(Graph<Method, Edge> graph);
}
