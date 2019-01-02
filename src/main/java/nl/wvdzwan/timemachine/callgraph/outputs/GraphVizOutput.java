package nl.wvdzwan.timemachine.callgraph.outputs;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Map;

import com.ibm.wala.types.MethodReference;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jgrapht.Graph;
import org.jgrapht.io.Attribute;
import org.jgrapht.io.DOTExporter;

import nl.wvdzwan.timemachine.callgraph.FolderLayout.ArtifactFolderLayout;

public abstract class GraphVizOutput {
    private static Logger logger = LogManager.getLogger();

    protected final ArtifactFolderLayout folderLayout;
    protected final Graph<MethodReference, GraphEdge> graph;
    protected final Map<MethodReference, AttributeMap> vertexAttributeMap;

    public GraphVizOutput(ArtifactFolderLayout folderLayout, Graph<MethodReference, GraphEdge> graph, Map<MethodReference, AttributeMap> vertexAttributeMap) {
        this.folderLayout = folderLayout;
        this.graph = graph;
        this.vertexAttributeMap = vertexAttributeMap;
    }


    public boolean export(File output) {

        DOTExporter<MethodReference, GraphEdge> exporter = new DOTExporter<>(
                this::vertexIdProvider,
                this::vertexLabelProvider,
                this::edgeLabelProvider,
                this::vertexAttributeProvider,
                this::edgeAttributeProvider);


        setGraphAttributes(exporter);

        try {
            Writer writer = new FileWriter(output.getAbsolutePath());
            exporter.exportGraph(graph, writer);

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    protected void setGraphAttributes(DOTExporter<MethodReference, GraphEdge> exported) {
        // No graph attributes by default
    };

    abstract String vertexIdProvider(MethodReference reference);
    abstract String vertexLabelProvider(MethodReference reference);
    abstract Map<String, Attribute> vertexAttributeProvider(MethodReference reference);

    abstract String edgeLabelProvider(GraphEdge edge);
    abstract Map<String, Attribute> edgeAttributeProvider(GraphEdge edge);

}
