package nl.wvdzwan.lapp.callgraph.outputs;

import java.util.HashMap;
import java.util.Map;

import org.jgrapht.Graph;
import org.jgrapht.io.Attribute;
import org.jgrapht.io.DefaultAttribute;

import nl.wvdzwan.lapp.Method.Method;
import nl.wvdzwan.lapp.Method.ResolvedMethod;
import nl.wvdzwan.lapp.Method.UnresolvedMethod;

public class UnifiedCallGraphExport extends GraphVizOutput {

    public UnifiedCallGraphExport(Graph<Method, GraphEdge> graph) {
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

    public String edgeLabelProvider(GraphEdge edge) {
        return edge.getLabel();
    }

    public Map<String, Attribute> edgeAttributeProvider(GraphEdge edge) {
        Map<String, Attribute> attributes = new HashMap<>();

        Attribute attribute;

        if (edge instanceof GraphEdge.InterfaceDispatchEdge) {
            attribute = DefaultAttribute.createAttribute("bold");
        } else if (edge instanceof GraphEdge.VirtualDispatchEdge) {
            attribute = DefaultAttribute.createAttribute("bold");
        } else if (edge instanceof GraphEdge.ClassHierarchyEdge.ImplementsEdge) {
            attribute = DefaultAttribute.createAttribute("dashed");
        } else if (edge instanceof GraphEdge.ClassHierarchyEdge.OverridesEdge) {
            attribute = DefaultAttribute.createAttribute("dotted");
        } else {
            return null;
        }

        attributes.put("style", attribute);

        return attributes;
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
