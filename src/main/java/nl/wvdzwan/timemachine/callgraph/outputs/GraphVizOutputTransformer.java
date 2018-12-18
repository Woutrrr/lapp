package nl.wvdzwan.timemachine.callgraph.outputs;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.ibm.wala.classLoader.CallSiteReference;
import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.shrikeBT.IInvokeInstruction;
import com.ibm.wala.types.MethodReference;
import com.ibm.wala.types.Selector;
import com.ibm.wala.util.graph.Graph;
import com.ibm.wala.util.graph.impl.SlowSparseNumberedGraph;

public class GraphVizOutputTransformer {

    private Predicate<CGNode> nodeFilter;
    private Graph<IGraphNode> graph;

    public GraphVizOutputTransformer(Predicate<CGNode> nodeFilter) {
        this.nodeFilter = nodeFilter;
        graph = SlowSparseNumberedGraph.make();
    }

    public Graph<IGraphNode> transform(CallGraph cg, IClassHierarchy cha) {


        Iterator<CGNode> cgIterator = cg.iterator();
        while (cgIterator.hasNext()) {
            CGNode node = cgIterator.next();
            MethodReference nodeReference = node.getMethod().getReference();

            if (nodeFilter.test(node)) {
                continue;
            }
            MethodRefNode graphNode = new MethodRefNode(nodeReference);
            graph.addNode(graphNode);

            IClass declaringClass = node.getMethod().getDeclaringClass();

            Collection<IClass> interfaces = declaringClass.getAllImplementedInterfaces();
            if (interfaces.size() > 0) {
                Map<Selector, IMethod> methods = interfaces.stream()
                        .flatMap(implementedInterface -> implementedInterface.getDeclaredMethods().stream())
                        .collect(Collectors.toMap(IMethod::getSelector, Function.identity()));

                if (methods.containsKey(nodeReference.getSelector())) {
                    IMethod interfaceMethod = methods.get(nodeReference.getSelector());

                    IGraphNode interfaceMethodNode = new MethodRefNode(interfaceMethod.getReference(), NodeAnnotation.InterfaceMethod);
                    graph.addNode(interfaceMethodNode);

                    // Reverse because we need a edge from interface def to implementation
                    if (!graph.hasEdge(interfaceMethodNode, graphNode)) {
                        graph.addEdge(interfaceMethodNode, graphNode);
                    }
                }
            }

            IMethod method = node.getMethod();
            IMethod superMethod = null;
            IClass superClass = null;
            IClass walkerClass = declaringClass.getSuperclass();

            while (walkerClass != null) {
                // Is method found in superclass ?
                Collection<? extends IMethod> declaredSuperMethods = walkerClass.getDeclaredMethods();
                if (declaredSuperMethods.contains(method)) {
                    superClass = walkerClass;
                    superMethod = cha.resolveMethod(superClass, method.getSelector());

                    break;
                }


                walkerClass = walkerClass.getSuperclass();
            }


            if (superClass != null) {
                IGraphNode implementedSuperMethodNode = new MethodRefNode(superMethod.getReference(), NodeAnnotation.SuperMethod);
                graph.addNode(implementedSuperMethodNode);

                if (!graph.hasEdge(implementedSuperMethodNode, graphNode)) {
                    graph.addEdge(implementedSuperMethodNode, graphNode);
                }
            }


            for (Iterator<CallSiteReference> callsites = node.iterateCallSites(); callsites.hasNext(); ) {
                CallSiteReference callsite = callsites.next();

                MethodReference targetReference = callsite.getDeclaredTarget();

                switch ((IInvokeInstruction.Dispatch) callsite.getInvocationCode()) {
                    case INTERFACE:
                        IGraphNode invokeInterfaceNode = new MethodRefNode(targetReference, NodeAnnotation.InvokeInterface);
                        addEdgeToNewNode(graphNode, invokeInterfaceNode);
                        break;

                    case VIRTUAL:
                    case SPECIAL:
                    case STATIC:
                    default:
                        MethodRefNode targetNode = new MethodRefNode(targetReference);

                        addEdgeToNewNode(graphNode, targetNode);
                }
            }

        }
        return graph;
    }

    private void addEdgeToNewNode(IGraphNode src, IGraphNode dst) {
        graph.addNode(dst);

        if (!graph.hasEdge(src, dst)) {
            graph.addEdge(src, dst);
        }
    }
}
