package nl.wvdzwan.lapp.callgraph;

import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import nl.wvdzwan.lapp.LappPackage;

public class PackageBuilder {

    private static final Logger logger = LogManager.getLogger();


    private final CallGraph callGraph;
    private final IClassHierarchy cha;
    private ClassArtifactResolver artifactResolver;


    private IRGraphBuilder graphBuilder;


    public PackageBuilder(WalaAnalysisResult analysisResult, ClassArtifactResolver artifactResolver) {
        this.callGraph = analysisResult.cg;
        this.cha = analysisResult.extendedCha;
        this.artifactResolver = artifactResolver;
    }


    public LappPackage build() {
        this.graphBuilder = new IRGraphBuilder(artifactResolver);

        ClassHierarchyInserter chaInserter = new ClassHierarchyInserter(cha, graphBuilder);
        chaInserter.insertCHA();

        CallGraphInserter cgInserter = new CallGraphInserter(callGraph, cha, graphBuilder);
        cgInserter.insertCallGraph();

        return graphBuilder.getLappPackage();
    }

    public static LappPackage build(WalaAnalysisResult analysisResult, ClassArtifactResolver artifactResolver) {
        IRGraphBuilder graphBuilder = new IRGraphBuilder(artifactResolver);

        ClassHierarchyInserter chaInserter = new ClassHierarchyInserter(analysisResult.extendedCha, graphBuilder);
        chaInserter.insertCHA();

        CallGraphInserter cgInserter = new CallGraphInserter(analysisResult.cg, analysisResult.extendedCha, graphBuilder);
        cgInserter.insertCallGraph();

        return graphBuilder.getLappPackage();
    }


}
