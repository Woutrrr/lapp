package nl.wvdzwan.lapp.callgraph.outputs;

import java.io.Writer;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jgrapht.Graph;
import org.jgrapht.io.Attribute;
import org.jgrapht.io.DOTExporter;

import nl.wvdzwan.lapp.IRDotMerger.AnnotatedVertex;

public abstract class GraphVizOutput {
    private static Logger logger = LogManager.getLogger();

    protected final Graph<AnnotatedVertex, GraphEdge> graph;


    public GraphVizOutput(Graph<AnnotatedVertex, GraphEdge> graph) {
        this.graph = graph;
    }

    public boolean export(Writer writer) {

        DOTExporter<AnnotatedVertex, GraphEdge> exporter = new DOTExporter<>(
                this::vertexIdProvider,
                this::vertexLabelProvider,
                this::edgeLabelProvider,
                this::vertexAttributeProvider,
                this::edgeAttributeProvider);


        setGraphAttributes(exporter);

        exporter.exportGraph(graph, writer);

        return true;
    }

    protected void setGraphAttributes(DOTExporter<AnnotatedVertex, GraphEdge> exported) {
        // No graph attributes by default
    }

    abstract String vertexIdProvider(AnnotatedVertex vertex);
    abstract String vertexLabelProvider(AnnotatedVertex vertex);
    abstract Map<String, Attribute> vertexAttributeProvider(AnnotatedVertex vertex);

    abstract String edgeLabelProvider(GraphEdge edge);
    abstract Map<String, Attribute> edgeAttributeProvider(GraphEdge edge);

}
