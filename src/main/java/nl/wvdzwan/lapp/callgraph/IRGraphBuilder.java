package nl.wvdzwan.lapp.callgraph;

import java.util.Iterator;
import java.util.List;
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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultDirectedGraph;

import nl.wvdzwan.lapp.IRDotMerger.AnnotatedVertex;
import nl.wvdzwan.lapp.callgraph.outputs.AttributeMap;
import nl.wvdzwan.lapp.callgraph.outputs.GraphEdge;

public class IRGraphBuilder {

    private static final Logger logger = LogManager.getLogger();

    static final String INTERFACE_METHOD = "Interface";
    static final String ABSTRACT_METHOD = "Abstract";
    static final String IMPLEMENTED_METHOD = "Implementation";
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

    private Graph<AnnotatedVertex, GraphEdge> graph = new DefaultDirectedGraph<>(GraphEdge.class);


    public IRGraphBuilder(CallGraph cg, IClassHierarchy cha, ClassToArtifactResolver artifactResolver) {
        this.callGraph = cg;
        this.cha = cha;
        this.artifactResolver = artifactResolver;
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
            AnnotatedVertex nodeVertex = addVertexWithAttribute(nodeReference, AttributeMap.TYPE, IMPLEMENTED_METHOD);

            for (Iterator<CallSiteReference> callsites = node.iterateCallSites(); callsites.hasNext(); ) {
                CallSiteReference callsite = callsites.next();

                MethodReference targetReference = callsite.getDeclaredTarget();
                AnnotatedVertex targetVertex = addVertex(targetReference);

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

    public void insertCHA() {
        IClassLoader classLoader = cha.getLoader(ClassLoaderReference.Application);

        // Iterate all classes in Application scope
        for (Iterator<IClass> it = classLoader.iterateAllClasses(); it.hasNext(); ) {
            IClass klass = it.next();
            IClass superKlass = klass.getSuperclass();

            Map<Selector, List<IMethod>> interfaceMethods = klass.getDirectInterfaces()
                    .stream()
                    .flatMap(o -> o.getDeclaredMethods().stream())
                    .collect(
                            Collectors.groupingBy(IMethod::getSelector)
                    );

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
                AnnotatedVertex declaredNodeVertex = addVertexWithAttribute(declaredMethod.getReference(), AttributeMap.TYPE, typeValue);


                IMethod superMethod = superKlass.getMethod(declaredMethod.getSelector());
                if (superMethod != null) {
                    AnnotatedVertex superVertex = addVertex(superMethod.getReference());
                    graph.addEdge(superVertex, declaredNodeVertex, new GraphEdge.OverridesEdge());
                }


                List<IMethod> methodInterfaces = interfaceMethods.get(declaredMethod.getSelector());
                if (methodInterfaces != null) {
                    for (IMethod interfaceMethod : methodInterfaces) {
                        AnnotatedVertex interfaceNodeVertex = addVertexWithAttribute(interfaceMethod.getReference(), AttributeMap.TYPE, INTERFACE_METHOD);
                        graph.addEdge(interfaceNodeVertex, declaredNodeVertex, new GraphEdge.ImplementsEdge());
                    }
                }

                // An abstract class doesn't have to define abstract functions for interface methods
                // So if this method doesn't have a super method or an interface method look for them in the interfaces of the abstract superclass
                if (superKlass.isAbstract() && superMethod == null && methodInterfaces == null) {

                    Map<Selector, IMethod> abstractSuperClassInterfaceMethods = superKlass.getDirectInterfaces()
                            .stream()
                            .flatMap(o -> o.getDeclaredMethods().stream())
                            .collect(Collectors.toMap(IMethod::getSelector, Function.identity()));

                    IMethod abstractSuperClassInterfaceMethod = abstractSuperClassInterfaceMethods.get(declaredMethod.getSelector());
                    if (abstractSuperClassInterfaceMethod != null) {
                        AnnotatedVertex abstractSuperClassInterfaceMethodVertex = addVertexWithAttribute(abstractSuperClassInterfaceMethod.getReference(), AttributeMap.TYPE, INTERFACE_METHOD);
                        graph.addEdge(abstractSuperClassInterfaceMethodVertex, declaredNodeVertex, new GraphEdge.ImplementsEdge());
                    }
                }
            }
        }
    }

    public Graph<AnnotatedVertex, GraphEdge> getGraph() { return this.graph;}

    private AnnotatedVertex addVertexWithAttribute(MethodReference methodReference, String attributeName, String value) {

        AnnotatedVertex vertex = addVertex(methodReference);

        vertex.setAttribute(attributeName, value);

        return vertex;
    }

    private AnnotatedVertex addVertex(MethodReference reference) {

        ArtifactRecord record = artifactResolver.artifactRecordFromMethodReference(reference);
        String namespace = reference.getDeclaringClass().getName().toString().substring(1).replace('/', '.');
        String symbol = reference.getSelector().toString();

        AnnotatedVertex result = AnnotatedVertex.findOrCreate(record, namespace, symbol);
        graph.addVertex(result); // TODO handle already existing vertex?
        return result;
    }

}
