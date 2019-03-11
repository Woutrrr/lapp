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

import nl.wvdzwan.lapp.Method.Method;
import nl.wvdzwan.lapp.callgraph.outputs.GraphEdge;

public class CallGraphInserter {



    private final CallGraph cg;
    private final IClassHierarchy cha;
    private final IRGraphBuilder graph;

    public CallGraphInserter(CallGraph cg, IClassHierarchy cha, IRGraphBuilder graph) {
        this.cg = cg;
        this.cha = cha;
        this.graph = graph;
    }


    public void insertCallGraph() {
        for (CGNode node : this.cg) {
            MethodReference nodeReference = node.getMethod().getReference();

            if (nodeFilter.test(node)) {
                continue;
            }
            Method methodNode = graph.addMethod(nodeReference, IRGraphBuilder.MethodType.IMPLEMENTATION);

            for (Iterator<CallSiteReference> callSites = node.iterateCallSites(); callSites.hasNext(); ) {
                CallSiteReference callSite = callSites.next();

                MethodReference targetReference = correctClassLoader(callSite.getDeclaredTarget());
                GraphEdge.DispatchEdge edge = getInvocationEdge(callSite);

                Method targetMethodNode = graph.addMethod(targetReference);
                graph.addEdge(methodNode, targetMethodNode, edge);

            }

        }
    }

    private Predicate<CGNode> nodeFilter = node -> {
        return !node.getMethod()
                .getDeclaringClass()
                .getClassLoader()
                .getReference()
                .equals(ClassLoaderReference.Application);
    };

    private GraphEdge.DispatchEdge getInvocationEdge(CallSiteReference callsite) {
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
        return edge;
    }

    private MethodReference correctClassLoader(MethodReference reference) {
        IClass klass = cha.lookupClass(reference.getDeclaringClass());

        if (klass == null) {
            return MethodReference.findOrCreate(ClassLoaderReference.Extension,
                    reference.getDeclaringClass().getName().toString(),
                    reference.getName().toString(),
                    reference.getDescriptor().toString());
        }

        return MethodReference.findOrCreate(klass.getReference(), reference.getSelector());

    }
}
