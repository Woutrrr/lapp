package nl.wvdzwan.timemachine.callgraph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.ibm.wala.classLoader.CallSiteReference;
import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IClassLoader;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.shrikeBT.IInvokeInstruction;
import com.ibm.wala.types.ClassLoaderReference;
import com.ibm.wala.types.MethodReference;
import com.ibm.wala.types.Selector;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultDirectedGraph;

import nl.wvdzwan.timemachine.callgraph.outputs.AttributeMap;
import nl.wvdzwan.timemachine.callgraph.outputs.GraphEdge;

public class IRGraphBuilder {
    static final String INTERFACE_METHOD = "Interface";
    static final String ABSTRACT_METHOD = "Abstract";
    static final String IMPLEMENTED_METHOD = "Implementation";
    private final CallGraph callGraph;
    private final IClassHierarchy cha;

    private Predicate<CGNode> nodeFilter = node -> {
        return !node.getMethod()
                .getDeclaringClass()
                .getClassLoader()
                .getReference()
                .equals(ClassLoaderReference.Application);
    };

    private Graph<MethodReference, GraphEdge> graph = new DefaultDirectedGraph<>(GraphEdge.class);
    private Map<MethodReference, AttributeMap> vertexAttributeMap = new HashMap<>();

    private static final Function<MethodReference, AttributeMap> emptyAttributeMapProvider =
            methodReference -> new AttributeMap();

    public IRGraphBuilder(CallGraph cg, IClassHierarchy cha) {
        this.callGraph = cg;
        this.cha = cha;
    }


    public void build() {

        insertCHA();

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
            addVertexWithAttribute(nodeReference, AttributeMap.TYPE, IMPLEMENTED_METHOD);

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
    }

    public void insertCHA() {
        IClassLoader classLoader = cha.getLoader(ClassLoaderReference.Application);

        // Iterate all classes in Application scope
        for (Iterator<IClass> it = classLoader.iterateAllClasses(); it.hasNext(); ) {
            IClass klass = it.next();
            IClass superKlass = klass.getSuperclass();

            Collection<IClass> interfaces = new ArrayList<>(klass.getDirectInterfaces());
            Map<Selector, IMethod> interfaceMethods = interfaces.stream().flatMap(o -> o.getDeclaredMethods().stream()).collect(Collectors.toMap(IMethod::getSelector, Function.identity()));

            for (IMethod declaredMethod : klass.getDeclaredMethods()) {

                if (declaredMethod.isPrivate()) {
                    // Private methods cannot be overridden, so no need for them.
                    continue;
                }

                String typeValue;
                if (declaredMethod.isAbstract()) {

                    if (klass.isInterface()) {
                        typeValue = INTERFACE_METHOD;
                    } else {
                        typeValue = ABSTRACT_METHOD;
                    }

                } else {
                    typeValue = IMPLEMENTED_METHOD;
                }
                addVertexWithAttribute(declaredMethod.getReference(), AttributeMap.TYPE, typeValue);


                IMethod superMethod = superKlass.getMethod(declaredMethod.getSelector());
                if (superMethod != null) {
                    graph.addVertex(superMethod.getReference());
                    graph.addEdge(superMethod.getReference(), declaredMethod.getReference(), new GraphEdge.OverridesEdge());
                }


                IMethod interfaceMethod = interfaceMethods.get(declaredMethod.getSelector());
                if (interfaceMethod != null) {
                    addVertexWithAttribute(interfaceMethod.getReference(), AttributeMap.TYPE, INTERFACE_METHOD);
                    graph.addEdge(interfaceMethod.getReference(), declaredMethod.getReference(), new GraphEdge.ImplementsEdge());

                }

                // An abstract class doesn't have to define abstract functions for interface methods
                // So if this method doesn't have a super method or an interface method look for them in the interfaces of the abstract superclass
                if (superKlass.isAbstract() && superMethod == null && interfaceMethod == null) {

                    Map<Selector, IMethod> abstractSuperClassInterfaceMethods = superKlass.getDirectInterfaces()
                            .stream()
                            .flatMap(o -> o.getDeclaredMethods().stream())
                            .collect(Collectors.toMap(IMethod::getSelector, Function.identity()));

                    IMethod abstractSuperClassInterfaceMethod = abstractSuperClassInterfaceMethods.get(declaredMethod.getSelector());
                    if (abstractSuperClassInterfaceMethod != null) {
                        addVertexWithAttribute(abstractSuperClassInterfaceMethod.getReference(), AttributeMap.TYPE, INTERFACE_METHOD);
                        graph.addEdge(abstractSuperClassInterfaceMethod.getReference(), declaredMethod.getReference(), new GraphEdge.ImplementsEdge());
                    }
                }
            }
        }
    }

    public Map<MethodReference, AttributeMap> getVertexAttributeMapMap() {
        return this.vertexAttributeMap;
    }

    public Graph<MethodReference, GraphEdge> getGraph() { return this.graph;}

    private AttributeMap addVertexWithAttribute(MethodReference methodReference, String attributeName, String value) {
        graph.addVertex(methodReference);

        AttributeMap attributes = vertexAttributeMap.computeIfAbsent(methodReference, emptyAttributeMapProvider);
        attributes.put(attributeName, value);
        return attributes;
    }
}
