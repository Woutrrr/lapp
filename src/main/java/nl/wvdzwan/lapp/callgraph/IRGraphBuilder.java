package nl.wvdzwan.lapp.callgraph;

import java.util.Iterator;
import java.util.function.Predicate;

import com.ibm.wala.classLoader.CallSiteReference;
import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.shrikeBT.IInvokeInstruction;
import com.ibm.wala.types.ClassLoaderReference;
import com.ibm.wala.types.MethodReference;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jgrapht.Graph;

import nl.wvdzwan.lapp.IRDotMerger.AnnotatedVertex;
import nl.wvdzwan.lapp.callgraph.IRGraph.MethodType;
import nl.wvdzwan.lapp.callgraph.outputs.GraphEdge;

public class IRGraphBuilder {

    private static final Logger logger = LogManager.getLogger();


    private final CallGraph callGraph;
    private final IClassHierarchy cha;
    private final ClassToArtifactResolver artifactResolver;


    private Predicate<CGNode> nodeFilter = node -> {
        return !node.getMethod()
                .getDeclaringClass()
                .getClassLoader()
                .getReference()
                .equals(ClassLoaderReference.Application);
    };

    private IRGraph graph;


    public IRGraphBuilder(CallGraph cg, IClassHierarchy cha, ClassToArtifactResolver artifactResolver) {
        this.callGraph = cg;
        this.cha = cha;
        this.artifactResolver = artifactResolver;

        this.graph = new IRGraphWithDynamicEdges(artifactResolver);
    }


    public void build() {

        ClassHierarchyInserter chaInserter = new ClassHierarchyInserter(cha, graph);
        chaInserter.insertCHA();

        insertCallGraph();
    }

    private void insertCallGraph() {
        Iterator<CGNode> cgIterator = callGraph.iterator();
        while (cgIterator.hasNext()) {
            CGNode node = cgIterator.next();
            MethodReference nodeReference = node.getMethod().getReference();

            if (nodeFilter.test(node)) {
                continue;
            }
            AnnotatedVertex nodeVertex = graph.addTypedVertex(nodeReference, MethodType.IMPLEMENTATION);

            for (Iterator<CallSiteReference> callsites = node.iterateCallSites(); callsites.hasNext(); ) {
                CallSiteReference callsite = callsites.next();

                MethodReference targetReference = callsite.getDeclaredTarget();

                IClass klass = cha.lookupClass(callsite.getDeclaredTarget().getDeclaringClass());

                if (klass == null) {
                    targetReference = MethodReference.findOrCreate(ClassLoaderReference.Extension,
                            targetReference.getDeclaringClass().getName().toString(),
                            targetReference.getName().toString(),
                            targetReference.getDescriptor().toString());
                } else {
                    targetReference = MethodReference.findOrCreate(klass.getReference(), targetReference.getSelector());
                }
                AnnotatedVertex targetVertex = graph.addVertex(targetReference);



                GraphEdge.DispatchEdge edge;

                switch ((IInvokeInstruction.Dispatch) callsite.getInvocationCode()) {
                    case INTERFACE:
                        edge = new GraphEdge.InterfaceDispatchEdge();
                        break;

                    case VIRTUAL:
                        edge = new GraphEdge.VirtualDispatchEdge();
                        break;

                    case SPECIAL:
                        edge = new GraphEdge.SpecialDispatchEdge();
                        break;

                    case STATIC:
                        edge = new GraphEdge.StaticDispatchEdge();
                        break;
                    default:
                        assert false : "Unknown IInvokeInstruction!";
                        edge = null;
                }
                graph.addEdge(nodeVertex, targetVertex, edge);

            }

        }
    }



    public Graph<AnnotatedVertex, GraphEdge> getGraph() { return this.graph.getInnerGraph();}

    public IRGraph getIRGraph() {
        return this.graph;
    }


    public IClassHierarchy getCha() {
        return cha;
    }
}
