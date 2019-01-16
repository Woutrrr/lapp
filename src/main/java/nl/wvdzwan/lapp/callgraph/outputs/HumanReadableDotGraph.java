package nl.wvdzwan.lapp.callgraph.outputs;

import java.util.HashMap;
import java.util.Map;

import org.jgrapht.Graph;
import org.jgrapht.io.Attribute;
import org.jgrapht.io.DOTExporter;
import org.jgrapht.io.DefaultAttribute;

import nl.wvdzwan.lapp.IRDotMerger.AnnotatedVertex;

public class HumanReadableDotGraph extends GraphVizOutput {

    public HumanReadableDotGraph(Graph<AnnotatedVertex, GraphEdge> graph) {
        super(graph);
    }

    @Override
    protected void setGraphAttributes(DOTExporter<AnnotatedVertex, GraphEdge> dotExporter) {
        dotExporter.putGraphAttribute("overlap", "false");
        dotExporter.putGraphAttribute("ranksep", "1");
    }

    @Override
    protected String vertexIdProvider(AnnotatedVertex vertex) {
        return "\"" + vertex.getNamespace() + "." + vertex.getSymbol() + "\""; //.replaceAll("[\\.\\(\\)<>/;]", "_");
    }

    @Override
    protected String vertexLabelProvider(AnnotatedVertex vertex) {
        String label = vertex.getNamespace() + "." + vertex.getSymbol();

        if (vertex.getAttributes().containsKey(AttributeMap.TYPE)) {
            label = "" + vertex.getAttributes().get(AttributeMap.TYPE) + " - " + label;
        }

        return label;
    }

    @Override
    protected Map<String, Attribute> vertexAttributeProvider(AnnotatedVertex vertex) {
        return vertex.getAttributes();
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


