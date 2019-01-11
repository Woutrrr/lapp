package nl.wvdzwan.timemachine.callgraph.importers;

import java.util.Map;

import org.jgrapht.Graph;
import org.jgrapht.io.Attribute;

import nl.wvdzwan.timemachine.IRDotMerger.AnnotatedVertex;
import nl.wvdzwan.timemachine.callgraph.ArtifactRecord;
import nl.wvdzwan.timemachine.callgraph.outputs.GraphEdge;

public class UnifiedCallGraphImport extends GraphVizImporter {

    private static final int groupArtifactIndex = 1;
    private static final int versionIndex = 2;
    private static final int namespaceIndex = 3;
    private static final int symbolIndex = 4;

    public UnifiedCallGraphImport(Graph<AnnotatedVertex, GraphEdge> graph) {
        super(graph);
    }


    public AnnotatedVertex vertexProducer(String id, Map<String, Attribute> attributes) {
        String[] parts = id.split("::");

        ArtifactRecord record = new ArtifactRecord(parts[groupArtifactIndex] + ":" + parts[versionIndex]);

        AnnotatedVertex result = AnnotatedVertex.findOrCreate(record, parts[namespaceIndex], parts[symbolIndex]);
        result.getAttributes().putAll(attributes);
        return result;
    }

    public GraphEdge edgeProducer(AnnotatedVertex from, AnnotatedVertex to, String label, Map<String, Attribute> attributes) {
        switch (label) {
            case "invoke_interface":
                return new GraphEdge.InterfaceDispatchEdge();
            case "invoke_virtual":
                return new GraphEdge.VirtualDispatchEdge();
            case "invoke_special":
                return new GraphEdge.SpecialDispatchEdge();
            case "invoke_static":
                return new GraphEdge.SpecialDispatchEdge();
            case "overridden by":
                return new GraphEdge.OverridesEdge();
            case "implemented by":
                return new GraphEdge.ImplementsEdge();
            default:
                assert false : "Unknown edge";
                return null;
        }
    }

    @Override
    void mergeVertexAttributes(AnnotatedVertex vertex, Map<String, Attribute> extraAttributes) {
        vertex.mergeAttributes(extraAttributes);
    }
}
