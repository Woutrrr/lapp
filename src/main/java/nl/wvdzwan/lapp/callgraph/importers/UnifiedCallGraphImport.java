package nl.wvdzwan.lapp.callgraph.importers;

import java.util.Map;

import org.jgrapht.Graph;
import org.jgrapht.io.Attribute;

import nl.wvdzwan.lapp.Method.Method;
import nl.wvdzwan.lapp.Method.ResolvedMethod;
import nl.wvdzwan.lapp.callgraph.outputs.GraphEdge;

public class UnifiedCallGraphImport extends GraphVizImporter {

    private static final int groupArtifactIndex = 1;
    private static final int versionIndex = 2;
    private static final int namespaceIndex = 3;
    private static final int symbolIndex = 4;

    public UnifiedCallGraphImport(Graph<Method, GraphEdge> graph) {
        super(graph);
    }


    public Method vertexProducer(String id, Map<String, Attribute> attributes) {
        String[] parts = id.split("::");

        Method result = new ResolvedMethod(parts[namespaceIndex], parts[symbolIndex], parts[groupArtifactIndex] + ":" + parts[versionIndex]);

        attributes.forEach((key, attribute) -> result.metadata.put(key, attribute.getValue()));

        return result;
    }

    public GraphEdge edgeProducer(Method from, Method to, String label, Map<String, Attribute> attributes) {
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
                return new GraphEdge.ClassHierarchyEdge.OverridesEdge();
            case "implemented by":
                return new GraphEdge.ClassHierarchyEdge.ImplementsEdge();
            default:
                assert false : "Unknown edge";
                return null;
        }
    }

    @Override
    void mergeVertexAttributes(Method vertex, Map<String, Attribute> extraAttributes) {

        extraAttributes.forEach((key, attribute) -> vertex.metadata.put(key, attribute.getValue()));

    }
}
