package nl.wvdzwan.timemachine.callgraph.outputs;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

import com.ibm.wala.classLoader.CallSiteReference;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.shrikeBT.IInvokeInstruction;
import com.ibm.wala.types.MethodReference;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultDirectedGraph;

public class GraphVizOutputTransformer {
    static final String INTERFACE_METHOD = "Interface";
    static final String ABSTRACT_METHOD = "Abstract";
    static final String IMPLEMENTED_METHOD = "Implementation";

    private Predicate<CGNode> nodeFilter;
    private Graph<MethodReference, GraphEdge> graph;

    private Map<MethodReference, AttributeMap> vertexAttributeMap = new HashMap<>();

    private static final Function<MethodReference, AttributeMap> emptyAttributeMapProvider =
            methodReference -> new AttributeMap();

    enum VertexAttributes {
        style, type,
    }

    static class AttributeMap extends EnumMap<VertexAttributes, String> {
        public AttributeMap() {
            super(VertexAttributes.class);
        }
    }

    public GraphVizOutputTransformer(Predicate<CGNode> nodeFilter) {
        this.nodeFilter = nodeFilter;
        graph = new DefaultDirectedGraph<>(GraphEdge.class);
    }

    public Graph<MethodReference, GraphEdge> transform(CallGraph cg, IClassHierarchy cha) {

        Iterator<CGNode> cgIterator = cg.iterator();
        while (cgIterator.hasNext()) {
            CGNode node = cgIterator.next();
            MethodReference nodeReference = node.getMethod().getReference();

            if (nodeFilter.test(node)) {
                continue;
            }
            addVertexWithAttribute(nodeReference, VertexAttributes.type, IMPLEMENTED_METHOD);

            for (Iterator<CallSiteReference> callsites = node.iterateCallSites(); callsites.hasNext(); ) {
                CallSiteReference callsite = callsites.next();


                MethodReference targetReference = callsite.getDeclaredTarget();
                targetReference = cha.resolveMethod(targetReference).getReference();

                switch ((IInvokeInstruction.Dispatch) callsite.getInvocationCode()) {
                    case INTERFACE:
                        graph.addVertex(targetReference);
                        graph.addEdge(nodeReference, targetReference, new GraphEdge.InterfaceDispatchEdge());
                        break;

                    case VIRTUAL:
                        graph.addVertex(targetReference);
                        graph.addEdge(nodeReference, targetReference, new GraphEdge.VirtualDispatchEdge());
                        break;

                    case SPECIAL:
                        graph.addVertex(targetReference);
                        graph.addEdge(nodeReference, targetReference, new GraphEdge.SpecialDispatchEdge());
                        break;
                    case STATIC:
                        graph.addVertex(targetReference);
                        graph.addEdge(nodeReference, targetReference, new GraphEdge.StaticDispatchEdge());
                        break;
                    default:
                        assert false : "Unknown IInvokeInstruction!";
                }
            }

        }
        return graph;
    }

    public Map<MethodReference, AttributeMap> getVertexAttributeMapMap() {
        return this.vertexAttributeMap;
    }

    private AttributeMap addVertexWithAttribute(MethodReference methodReference, VertexAttributes attributeName, String value) {
        graph.addVertex(methodReference);

        AttributeMap attributes = vertexAttributeMap.computeIfAbsent(methodReference, emptyAttributeMapProvider);
        attributes.put(attributeName, value);
        return attributes;
    }
}
