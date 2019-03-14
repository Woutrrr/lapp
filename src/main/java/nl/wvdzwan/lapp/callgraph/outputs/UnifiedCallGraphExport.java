package nl.wvdzwan.lapp.callgraph.outputs;

import java.util.Map;

import org.jgrapht.Graph;
import org.jgrapht.io.Attribute;

import nl.wvdzwan.lapp.core.Method;
import nl.wvdzwan.lapp.core.ResolvedMethod;
import nl.wvdzwan.lapp.core.UnresolvedMethod;
import nl.wvdzwan.lapp.call.Edge;

public class UnifiedCallGraphExport extends GraphVizOutput {

    public UnifiedCallGraphExport(Graph<Method, Edge> graph) {
        super(graph);
    }

    @Override
    public String vertexIdProvider(Method method) {
        String id;
        if (method instanceof ResolvedMethod) {
            id = resolvedMethodToLabel((ResolvedMethod) method);
        } else {
            id = unresolvedMethodToLabel((UnresolvedMethod) method);
        }

        return "\"" + id + "\"";
    }

    public String vertexLabelProvider(Method method) {

        String label = vertexIdProvider(method);

        if (method.metadata.containsKey("type")) {
            label = "" + method.metadata.get("type") + " - " + label;
        }
        return label;
    }

    public Map<String, Attribute> vertexAttributeProvider(Method vertex) {
        return mapAsAttributeMap(vertex.metadata);
    }

    private String resolvedMethodToLabel(ResolvedMethod method) {
        String separator = "::";
        return "mvn"
                + separator + method.artifact
                + separator + method.namespace
                + separator + method.symbol;
    }

    private String unresolvedMethodToLabel(UnresolvedMethod method) {
        return "mvn::__"
                + "::" + method.namespace
                + "::" + method.symbol;

    }
}
