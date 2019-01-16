package nl.wvdzwan.lapp.callgraph.outputs;

import java.util.HashMap;
import java.util.Map;

import org.jgrapht.Graph;
import org.jgrapht.io.Attribute;
import org.jgrapht.io.DefaultAttribute;

import nl.wvdzwan.lapp.IRDotMerger.AnnotatedVertex;

public class UnifiedCallGraphExport extends GraphVizOutput {

    public UnifiedCallGraphExport(Graph<AnnotatedVertex, GraphEdge> graph) {
        super(graph);
    }

    @Override
    public String vertexIdProvider(AnnotatedVertex vertex) {
        return "\"" + vertex.toGlobalIdentifier() + "\"";
    }


    public String vertexLabelProvider(AnnotatedVertex vertex) {
        String label = vertex.toGlobalIdentifier();

        Map<String, Attribute> attributes = vertex.getAttributes();
        if (attributes != null && attributes.containsKey("type")) {
            label = "" + attributes.get("type").getValue() + " - " + label;
        }
        return label;
    }


    public Map<String, Attribute> vertexAttributeProvider(AnnotatedVertex vertex) {
        return vertex.getAttributes();
    }

    public String edgeLabelProvider(GraphEdge edge) {
        return edge.getLabel();
    }


    public Map<String, Attribute> edgeAttributeProvider(GraphEdge edge) {
        Map<String, Attribute> attributes = new HashMap<>();

        if (edge instanceof GraphEdge.InterfaceDispatchEdge) {
            Attribute attribute = DefaultAttribute.createAttribute("bold");
            attributes.put("style", attribute);
        } else if (edge instanceof GraphEdge.VirtualDispatchEdge) {
            Attribute attribute = DefaultAttribute.createAttribute("bold");
            attributes.put("style", attribute);
        } else if (edge instanceof GraphEdge.ImplementsEdge) {
            Attribute attribute = DefaultAttribute.createAttribute("dashed");
            attributes.put("style", attribute);
        } else if (edge instanceof GraphEdge.OverridesEdge) {
            Attribute attribute = DefaultAttribute.createAttribute("dotted");
            attributes.put("style", attribute);
        } else {
            return null;
        }

        return attributes;
    }
}
