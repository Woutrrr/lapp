package nl.wvdzwan.lapp.callgraph.outputs.resolved_methods;

import java.io.IOException;
import java.io.Writer;
import java.util.Set;

import org.jgrapht.Graph;

import nl.wvdzwan.lapp.core.Method;
import nl.wvdzwan.lapp.core.ResolvedMethod;
import nl.wvdzwan.lapp.call.Edge;
import nl.wvdzwan.lapp.callgraph.outputs.CallGraphOutput;

public class ResolvedMethodOutput implements CallGraphOutput {

    private final Writer output;

    public ResolvedMethodOutput(Writer writer) {
        this.output = writer;
    }

    @Override
    public boolean export(Graph<Method, Edge> graph) {

        ResolvedMethodExtractor extractor = new ResolvedMethodExtractor();

        Set<ResolvedMethod> resolvedMethods = extractor.from(graph);

        try {
            for (ResolvedMethod method : resolvedMethods) {
                output.write(method.artifact + "::" + method.namespace + "." + method.symbol);
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
