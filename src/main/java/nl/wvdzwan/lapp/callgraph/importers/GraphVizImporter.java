package nl.wvdzwan.lapp.callgraph.importers;

import java.io.Reader;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jgrapht.Graph;
import org.jgrapht.io.Attribute;
import org.jgrapht.io.DOTImporter;
import org.jgrapht.io.ImportException;

import nl.wvdzwan.lapp.Method.Method;
import nl.wvdzwan.lapp.callgraph.outputs.GraphEdge;

public abstract class GraphVizImporter {
    private static Logger logger = LogManager.getLogger();

    protected final Graph<Method, GraphEdge> graph;


    public GraphVizImporter(Graph<Method, GraphEdge> graph) {
        this.graph = graph;
    }

    abstract Method vertexProducer(String id, Map<String, Attribute> attributes);

    abstract GraphEdge edgeProducer(Method from, Method to, String label, Map<String, Attribute> attributes);

    abstract void mergeVertexAttributes(Method vertex, Map<String, Attribute> extraAttributes);

    public Graph<Method, GraphEdge> importGraph(Reader reader) throws ImportException {
        DOTImporter<Method, GraphEdge> importer = new DOTImporter<>(
                this::vertexProducer,
                this::edgeProducer,
                this::mergeVertexAttributes
        );

        importer.importGraph(graph, reader);

        return graph;
    }

}
