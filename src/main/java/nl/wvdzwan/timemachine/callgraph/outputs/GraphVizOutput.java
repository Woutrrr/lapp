package nl.wvdzwan.timemachine.callgraph.outputs;

import java.io.File;
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
import com.ibm.wala.types.ClassLoaderReference;
import com.ibm.wala.types.MethodReference;
import com.ibm.wala.types.Selector;
import com.ibm.wala.util.WalaException;
import com.ibm.wala.util.graph.Graph;
import com.ibm.wala.util.graph.impl.SlowSparseNumberedGraph;
import com.ibm.wala.util.strings.Atom;
import com.ibm.wala.viz.DotUtil;
import com.ibm.wala.viz.NodeDecorator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import nl.wvdzwan.timemachine.callgraph.GlobalUniqueSymbolDecorator;
import nl.wvdzwan.timemachine.callgraph.MavenFolderLayout;

import static com.ibm.wala.types.ClassLoaderReference.Java;

public class GraphVizOutput implements CallgraphOutputTask<CallGraph> {
    private static Logger logger = LogManager.getLogger();

    private File output;
    private Predicate<CGNode> nodeFilter;
    private boolean includePhantom;
    private boolean includeInterfaceInvocation;
    private String repositoryPathPrefix;
    private Graph<IGraphNode> graph;

    public final static ClassLoaderReference ClassLoaderMissing = new ClassLoaderReference(Atom.findOrCreateUnicodeAtom("Missing"), Java, null);
    public final static ClassLoaderReference ClassLoaderUtility = new ClassLoaderReference(Atom.findOrCreateUnicodeAtom("Utility"), Java, null);


    public GraphVizOutput(
            File output,
            Predicate<CGNode> nodeFilter,
            boolean includePhantom,
            boolean includeInterfaceInvocation,
            String repositoryPathPrefix) {

        this.output = output;
        this.nodeFilter = nodeFilter;
        this.includePhantom = includePhantom;
        this.includeInterfaceInvocation = includeInterfaceInvocation;
        this.repositoryPathPrefix = repositoryPathPrefix;

        graph = SlowSparseNumberedGraph.make();
    }

    @Override
    public boolean makeOutput(CallGraph cg, IClassHierarchy extendedCha) {

        IClassHierarchy cha;
        if (extendedCha == null) {
            cha = cg.getClassHierarchy();
        } else {
            cha = extendedCha;
        }
        Graph<IGraphNode> methodGraph = outputcg(cg);

        NodeDecorator<IGraphNode> labelDecorator = new GlobalUniqueSymbolDecorator(
                cha,
                new MavenFolderLayout(repositoryPathPrefix)
        );

        try {
            DotUtil.writeDotFile(methodGraph, labelDecorator, "\", splines=true, overlap=false, ranksep=5, fontsize=36, root =  \"", output.getAbsolutePath());
        } catch (WalaException e) {
            e.printStackTrace();
        }


        return false;
    }

    private Graph<IGraphNode> outputcg(CallGraph cg) {


        Iterator<CGNode> cgIterator = cg.iterator();
        while (cgIterator.hasNext()) {
            CGNode node = cgIterator.next();
            MethodReference nodeReference = node.getMethod().getReference();

            if (nodeFilter.test(node)) {
                continue;
            }
            MethodRefNode graphNode = new MethodRefNode(nodeReference);
            graph.addNode(graphNode);



            Collection<IClass> interfaces = node.getMethod().getDeclaringClass().getAllImplementedInterfaces();

            if (interfaces.size() > 0) {
                Map<Selector, IMethod> methods = interfaces.stream()
                        .flatMap(implementedInterface -> implementedInterface.getDeclaredMethods().stream())
                        .collect(Collectors.toMap(IMethod::getSelector, Function.identity()));

                if (methods.containsKey(nodeReference.getSelector())) {
                    IMethod interfaceMethod = methods.get(nodeReference.getSelector());
                    InterfaceMethodNode interfaceMethodNode = new InterfaceMethodNode(interfaceMethod.getReference());

                    graph.addNode(interfaceMethodNode);
                    if (!graph.hasEdge(interfaceMethodNode, graphNode)) {
                        graph.addEdge(interfaceMethodNode, graphNode);
                    }
                }
            }


            for (Iterator<CallSiteReference> callsites = node.iterateCallSites(); callsites.hasNext(); ) {
                CallSiteReference callsite = callsites.next();

                MethodReference targetReference = callsite.getDeclaredTarget();

                switch ((IInvokeInstruction.Dispatch) callsite.getInvocationCode()) {
                    case INTERFACE:
                        InvokeInterfaceNode invokeInterfaceNode = new InvokeInterfaceNode(targetReference);

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
