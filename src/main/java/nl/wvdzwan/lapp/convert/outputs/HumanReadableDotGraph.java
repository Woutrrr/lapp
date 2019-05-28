package nl.wvdzwan.lapp.convert.outputs;

import java.util.Map;

import org.jgrapht.io.Attribute;
import org.jgrapht.io.DOTExporter;

import nl.wvdzwan.lapp.call.Edge;
import nl.wvdzwan.lapp.core.Method;

public class HumanReadableDotGraph extends GraphVizOutput {

    @Override
    protected void setGraphAttributes(DOTExporter<Method, Edge> dotExporter) {
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

}


