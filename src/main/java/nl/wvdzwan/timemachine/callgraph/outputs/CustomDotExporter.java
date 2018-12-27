package nl.wvdzwan.timemachine.callgraph.outputs;

import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

import com.ibm.wala.types.MethodReference;
import org.jgrapht.Graph;
import org.jgrapht.io.Attribute;
import org.jgrapht.io.DOTExporter;
import org.jgrapht.io.DefaultAttribute;
import org.jgrapht.io.GraphExporter;

public class CustomDotExporter implements GraphExporter<MethodReference, GraphEdge> {

    private Map<MethodReference, GraphVizOutputTransformer.AttributeMap> vertexAttributeMap = new HashMap<>();
    private DOTExporter<MethodReference, GraphEdge> dotExporter;


    public CustomDotExporter(Map<MethodReference, GraphVizOutputTransformer.AttributeMap> vertexAttributeMap) {

        this.vertexAttributeMap = vertexAttributeMap;

        dotExporter = new DOTExporter<>(
                this::vertexIdProvider,
                this::vertexLabelProvider,
                CustomDotExporter::edgeLabelProvider,
                this::vertexAttributeProvider,
                CustomDotExporter::edgeAttributeProvider);

        setGraphAttributes();
    }

    private void setGraphAttributes() {
        dotExporter.putGraphAttribute("overlap", "false");
        dotExporter.putGraphAttribute("ranksep", "1");
    }

    private String vertexIdProvider(MethodReference reference) {
        return "\"" + reference.getSignature() + "\""; //.replaceAll("[\\.\\(\\)<>/;]", "_");
    }

    private String vertexLabelProvider(MethodReference reference) {
        String label = reference.getSignature();
        GraphVizOutputTransformer.AttributeMap attributes = vertexAttributeMap.get(reference);
        if (attributes != null) {
            label = "" + attributes.get(GraphVizOutputTransformer.VertexAttributes.type) + " - " + label;
        }
        return label;
    }

    private Map<String, Attribute> vertexAttributeProvider(MethodReference reference) {

        if (vertexAttributeMap.containsKey(reference)) {
            Map<String, Attribute> attrs = new HashMap<String, Attribute>();
            GraphVizOutputTransformer.AttributeMap typeAttr = vertexAttributeMap.get(reference);

            typeAttr.forEach((vertexAttributes, s) -> {
                attrs.put(vertexAttributes.name(), DefaultAttribute.createAttribute(s));
            });

            return attrs;
        } else {
            return null;
        }

    }

    public static String edgeLabelProvider(GraphEdge edge) {
        return edge.getLabel();
    }

    public static Map<String, Attribute> edgeAttributeProvider(GraphEdge edge) {
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

    public void exportGraph(Graph<MethodReference, GraphEdge> g, Writer writer) {
        dotExporter.exportGraph(g, writer);
    }
}


