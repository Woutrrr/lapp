package nl.wvdzwan.timemachine.callgraph.importers;

import java.io.Reader;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jgrapht.Graph;
import org.jgrapht.io.Attribute;
import org.jgrapht.io.DOTImporter;
import org.jgrapht.io.ImportException;

import nl.wvdzwan.timemachine.IRDotMerger.AnnotatedVertex;
import nl.wvdzwan.timemachine.callgraph.outputs.GraphEdge;

public abstract class GraphVizImporter {
    private static Logger logger = LogManager.getLogger();

    protected final Graph<AnnotatedVertex, GraphEdge> graph;


    public GraphVizImporter(Graph<AnnotatedVertex, GraphEdge> graph) {
        this.graph = graph;
    }

    abstract AnnotatedVertex vertexProducer(String id, Map<String, Attribute> attributes);

    abstract GraphEdge edgeProducer(AnnotatedVertex from, AnnotatedVertex to, String label, Map<String, Attribute> attributes);

    abstract void mergeVertexAttributes(AnnotatedVertex vertex, Map<String, Attribute> extraAttributes);

    public Graph<AnnotatedVertex, GraphEdge> importGraph(Reader reader) throws ImportException {
        DOTImporter<AnnotatedVertex, GraphEdge> importer = new DOTImporter<>(
                this::vertexProducer,
                this::edgeProducer,
                this::mergeVertexAttributes
        );

        importer.importGraph(graph, reader);

        return graph;
    }

}
