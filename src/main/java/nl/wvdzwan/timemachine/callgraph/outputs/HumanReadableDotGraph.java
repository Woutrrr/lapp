package nl.wvdzwan.timemachine.callgraph.outputs;

import java.util.HashMap;
import java.util.Map;

import com.ibm.wala.types.MethodReference;
import org.jgrapht.Graph;
import org.jgrapht.io.Attribute;
import org.jgrapht.io.DOTExporter;
import org.jgrapht.io.DefaultAttribute;

import nl.wvdzwan.timemachine.callgraph.FolderLayout.ArtifactFolderLayout;

public class HumanReadableDotGraph extends GraphVizOutput {

    public HumanReadableDotGraph(ArtifactFolderLayout folderLayout, Graph<MethodReference, GraphEdge> graph, Map<MethodReference, AttributeMap> vertexAttributeMap) {
        super(folderLayout, graph, vertexAttributeMap);
    }

    @Override
    protected void setGraphAttributes(DOTExporter<MethodReference, GraphEdge> dotExporter) {
        dotExporter.putGraphAttribute("overlap", "false");
        dotExporter.putGraphAttribute("ranksep", "1");
    }

    @Override
    protected String vertexIdProvider(MethodReference reference) {
        return "\"" + reference.getSignature() + "\""; //.replaceAll("[\\.\\(\\)<>/;]", "_");
    }

    @Override
    protected String vertexLabelProvider(MethodReference reference) {
        String label = reference.getSignature();

        if (vertexAttributeMap.containsKey(reference)) {
            AttributeMap attributes = vertexAttributeMap.get(reference);

            if (attributes.containsKey(AttributeMap.TYPE)) {
                label = "" + attributes.get(AttributeMap.TYPE) + " - " + label;
            }
        }

        return label;
    }

    @Override
    protected Map<String, Attribute> vertexAttributeProvider(MethodReference reference) {

        if (vertexAttributeMap.containsKey(reference)) {
            Map<String, Attribute> attrs = new HashMap<>();
            AttributeMap attributeMap = vertexAttributeMap.get(reference);

            attributeMap.forEach((key, value) -> {
                attrs.put(key, DefaultAttribute.createAttribute(value));
            });

            return attrs;
        } else {
            return null;
        }

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


