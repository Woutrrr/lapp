package nl.wvdzwan.lapp.callgraph.outputs.calls;

import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.stream.Collectors;

import org.jgrapht.Graph;

import nl.wvdzwan.lapp.Method.Method;
import nl.wvdzwan.lapp.call.Call;
import nl.wvdzwan.lapp.call.Edge;
import nl.wvdzwan.lapp.callgraph.outputs.CallGraphOutput;

public class ResolvedCallOutput implements CallGraphOutput {

    private final Writer output;

    public ResolvedCallOutput (Writer writer) {
        this.output = writer;
    }

    @Override
    public boolean export(Graph<Method, Edge> graph) {


        List<Edge> edges = graph.edgeSet().stream()
                .filter(edge -> {
                    return edge instanceof Call
                            && ((Call) edge).isResolved();
                })
                .collect(Collectors.toList());

        try {
            for (Edge edge : edges) {
                output.write(edge.source.toID() + " -> " + edge.target.toID() + " :" + edge.getLabel());
                output.write("\n");
            }
            output.flush();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }
}
