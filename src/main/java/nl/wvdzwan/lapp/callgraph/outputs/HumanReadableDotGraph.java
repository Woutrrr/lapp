package nl.wvdzwan.lapp.callgraph.outputs;

import java.util.HashMap;
import java.util.Map;

import org.jgrapht.Graph;
import org.jgrapht.io.Attribute;
import org.jgrapht.io.DOTExporter;
import org.jgrapht.io.DefaultAttribute;

import nl.wvdzwan.lapp.Method.Method;

public class HumanReadableDotGraph extends GraphVizOutput {

    public HumanReadableDotGraph(Graph<Method, GraphEdge> graph) {
        super(graph);
    }

    @Override
    protected void setGraphAttributes(DOTExporter<Method, GraphEdge> dotExporter) {
        dotExporter.putGraphAttribute("overlap", "false");
        dotExporter.putGraphAttribute("ranksep", "1");
    }

    @Override
    protected String vertexIdProvider(Method vertex) {
        return "\"" + vertex.namespace + "." + vertex.symbol + "\"";
    }

    @Override
    protected String vertexLabelProvider(Method vertex) {
        String label = vertex.namespace + "." + vertex.symbol;

        if (vertex.metadata.containsKey("type")) {
            label = "" + vertex.metadata.get("type") + " - " + label;
        }

        return label;
    }

    @Override
    protected Map<String, Attribute> vertexAttributeProvider(Method vertex) {
        return mapAsAttributeMap(vertex.metadata);
    }

    @Override
    protected String edgeLabelProvider(GraphEdge edge) {
        return edge.getLabel();
    }


    @Override
    protected Map<String, Attribute> edgeAttributeProvider(GraphEdge edge) {
        Map<String, Attribute> attributes = new HashMap<>();

        if (edge instanceof GraphEdge.InterfaceDispatchEdge) {
            Attribute attribute = DefaultAttribute.createAttribute("bold");
            attributes.put("style", attribute);
        } else if (edge instanceof GraphEdge.VirtualDispatchEdge) {
            Attribute attribute = DefaultAttribute.createAttribute("bold");
            attributes.put("style", attribute);
        } else if (edge instanceof GraphEdge.ClassHierarchyEdge.ImplementsEdge) {
            Attribute attribute = DefaultAttribute.createAttribute("dashed");
            attributes.put("style", attribute);
        } else if (edge instanceof GraphEdge.ClassHierarchyEdge.OverridesEdge) {
            Attribute attribute = DefaultAttribute.createAttribute("dotted");
            attributes.put("style", attribute);
        } else {
            return null;
        }

        return attributes;
    }

}


