package nl.wvdzwan.timemachine.callgraph.outputs;

import java.io.File;
import java.util.Iterator;
import java.util.Set;
import java.util.function.Predicate;

import com.ibm.wala.classLoader.CallSiteReference;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.types.ClassLoaderReference;
import com.ibm.wala.types.MethodReference;
import com.ibm.wala.util.WalaException;
import com.ibm.wala.util.graph.Graph;
import com.ibm.wala.util.graph.impl.SlowSparseNumberedGraph;
import com.ibm.wala.util.strings.Atom;
import com.ibm.wala.viz.DotUtil;
import com.ibm.wala.viz.NodeDecorator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import nl.wvdzwan.timemachine.OutputTask;
import nl.wvdzwan.timemachine.callgraph.GlobalUniqueSymbolDecorator;
import nl.wvdzwan.timemachine.callgraph.MavenFolderLayout;

import static com.ibm.wala.types.ClassLoaderReference.Java;

public class GraphVizOutput implements OutputTask<CallGraph> {
    private static Logger logger = LogManager.getLogger();

    private File output;
    private Predicate<CGNode> nodeFilter;
    private boolean includePhantom;
    private boolean includeInterfaceInvocation;
    private String repositoryPathPrefix;

    public final static ClassLoaderReference ClassLoaderMissing = new ClassLoaderReference(Atom.findOrCreateUnicodeAtom("Missing"), Java, null);


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
    }

    @Override
    public boolean makeOutput(CallGraph cg) {

        Graph<MethodReference> methodGraph = outputcg(cg);

        NodeDecorator<MethodReference> labelDecorator = new GlobalUniqueSymbolDecorator(
                cg.getClassHierarchy(),
                new MavenFolderLayout(repositoryPathPrefix)
        );

        try {
            DotUtil.writeDotFile(methodGraph, labelDecorator, "\", splines=true, overlap=false, ranksep=5, fontsize=36, root =  \"", output.getAbsolutePath());
        } catch (WalaException e) {
            e.printStackTrace();
        }


        return false;
    }

    private Graph<MethodReference> outputcg(CallGraph cg) {
        Graph<MethodReference> graph = SlowSparseNumberedGraph.make();

        Iterator<CGNode> cgIterator = cg.iterator();
        while (cgIterator.hasNext()) {
            CGNode node = cgIterator.next();
            MethodReference nodeReference = node.getMethod().getReference();

            if (nodeFilter.test(node)) {
                continue;
            }

            graph.addNode(nodeReference);

            for (Iterator<CallSiteReference> callsites = node.iterateCallSites(); callsites.hasNext(); ) {
                CallSiteReference callsite = callsites.next();

                MethodReference targetReference = callsite.getDeclaredTarget();


                Set<CGNode> possibleTargets = cg.getPossibleTargets(node, callsite); // More specific, takes call site into consideration
                //Set<CGNode> possibleTargets = cg.getClassHierarchy().getPossibleTargets(targetReference));

                if (possibleTargets.size() == 0) {


                    if (includePhantom) {
                        logger.debug("No targets found for {}, creating phantom node", targetReference);
                        MethodReference missingMethod = MethodReference.findOrCreate(ClassLoaderMissing, targetReference.getDeclaringClass().getName().toString(), targetReference.getName().toString(), targetReference.getSelector().getDescriptor().toString());
                        graph.addNode(missingMethod);

                        if (!graph.hasEdge(nodeReference, missingMethod)) {
                            graph.addEdge(nodeReference, missingMethod);
                        }
                    } else {
                        logger.warn("No targets found for {}", targetReference);
                    }
                }

                if (includeInterfaceInvocation && callsite.isInterface()) {
                    try {
                        MethodReference interfaceReference = cg.getClassHierarchy().resolveMethod(targetReference).getReference();

                        graph.addNode(interfaceReference);
                        if (!graph.hasEdge(nodeReference, interfaceReference)) {
                            graph.addEdge(nodeReference, interfaceReference);
                        }
                    } catch (NullPointerException e) {
                        logger.warn("NPE for {}", targetReference);
                    }
                }

                for (CGNode possibleTarget : possibleTargets) {
                    MethodReference callSiteTargetReference = possibleTarget.getMethod().getReference();

                    graph.addNode(callSiteTargetReference);

                    if (!graph.hasEdge(nodeReference, callSiteTargetReference)) {
                        graph.addEdge(nodeReference, callSiteTargetReference);
                    }
                }
            }

        }
        return graph;
    }
}
