package nl.wvdzwan.timemachine.callgraph.outputs;

import java.util.Map;

import com.ibm.wala.types.MethodReference;
import org.jgrapht.Graph;
import org.jgrapht.io.Attribute;

import nl.wvdzwan.timemachine.callgraph.ArtifactRecord;
import nl.wvdzwan.timemachine.callgraph.ClassToArtifactResolver;
import nl.wvdzwan.timemachine.callgraph.FolderLayout.ArtifactFolderLayout;

public class UnifiedCallGraphFormatGraph extends GraphVizOutput {

    private final static String separator = "::";
    private final static String ecosystem = "mvn";

    private final ClassToArtifactResolver libraryResolver;


    public UnifiedCallGraphFormatGraph(
            ArtifactFolderLayout folderLayout,
            Graph<MethodReference, GraphEdge> graph,
            Map<MethodReference, AttributeMap> vertexAttributeMap,
            ClassToArtifactResolver libraryResolver) {
        super(folderLayout, graph, vertexAttributeMap);

        this.libraryResolver = libraryResolver;
    }

    @Override
    String vertexIdProvider(MethodReference reference) {
        return vertexLabelProvider(reference);
    }

    @Override
    protected String vertexLabelProvider(MethodReference reference) {

        String namespace = getNamespaceFromReference(reference);
        String symbol = reference.getSelector().toString();

        ArtifactRecord artifactRecord = libraryResolver.artifactRecordFromMethodReference(reference);

        return ecosystem
                + separator + artifactRecord.getUnversionedIdentifier()
                + separator + artifactRecord.getVersion()
                + separator + namespace
                + separator + symbol;
    }

    private String getNamespaceFromReference(MethodReference reference) {
        return reference.getDeclaringClass().getName().toString().substring(1).replace('/', '.');
    }


    @Override
    Map<String, Attribute> vertexAttributeProvider(MethodReference reference) {
        return null;
    }

    @Override
    String edgeLabelProvider(GraphEdge edge) {
        return null;
    }

    @Override
    Map<String, Attribute> edgeAttributeProvider(GraphEdge edge) {
        return null;
    }
}
