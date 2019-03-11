package nl.wvdzwan.lapp.callgraph.outputs;

import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jgrapht.Graph;
import org.jgrapht.io.Attribute;
import org.jgrapht.io.DOTExporter;
import org.jgrapht.io.DefaultAttribute;

import nl.wvdzwan.lapp.Method.Method;

public abstract class GraphVizOutput {
    private static Logger logger = LogManager.getLogger();

    protected final Graph<Method, GraphEdge> graph;


    public GraphVizOutput(Graph<Method, GraphEdge> graph) {
        this.graph = graph;
    }

    public boolean export(Writer writer) {

        DOTExporter<Method, GraphEdge> exporter = new DOTExporter<>(
                this::vertexIdProvider,
                this::vertexLabelProvider,
                this::edgeLabelProvider,
                this::vertexAttributeProvider,
                this::edgeAttributeProvider);


        setGraphAttributes(exporter);

        exporter.exportGraph(graph, writer);

        return true;
    }

    protected void setGraphAttributes(DOTExporter<Method, GraphEdge> exported) {
        // No graph attributes by default
    }

    abstract String vertexIdProvider(Method vertex);

    abstract String vertexLabelProvider(Method vertex);

    abstract Map<String, Attribute> vertexAttributeProvider(Method vertex);

    abstract String edgeLabelProvider(GraphEdge edge);

    abstract Map<String, Attribute> edgeAttributeProvider(GraphEdge edge);


    protected Map<String, Attribute> mapAsAttributeMap(Map<String, String> metadata) {
        Map<String, Attribute> result = new HashMap<>();


        for (Map.Entry<String, String> entry : metadata.entrySet()) {
            result.put(
                    entry.getKey(),
                    DefaultAttribute.createAttribute(entry.getValue())
            );
        }

        return result;
    }
}
