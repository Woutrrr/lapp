package nl.wvdzwan.lapp.callgraph;

import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jgrapht.Graph;

import nl.wvdzwan.lapp.Method.Method;
import nl.wvdzwan.lapp.callgraph.outputs.GraphEdge;

public class WalaGraphTransformer {

    private static final Logger logger = LogManager.getLogger();


    private final CallGraph callGraph;
    private final IClassHierarchy cha;
    private ClassArtifactResolver artifactResolver;


    private IRGraphBuilder graphBuilder;


    public WalaGraphTransformer(CallGraph cg, IClassHierarchy cha, ClassArtifactResolver artifactResolver) {
        this.callGraph = cg;
        this.cha = cha;
        this.artifactResolver = artifactResolver;
    }


    public Graph<Method, GraphEdge> build() {
        this.graphBuilder = new IRGraphBuilder(artifactResolver);

        ClassHierarchyInserter chaInserter = new ClassHierarchyInserter(cha, graphBuilder);
        chaInserter.insertCHA();

        CallGraphInserter cgInserter = new CallGraphInserter(callGraph, cha, graphBuilder);
        cgInserter.insertCallGraph();

        return graphBuilder.getInnerGraph();
    }


}
