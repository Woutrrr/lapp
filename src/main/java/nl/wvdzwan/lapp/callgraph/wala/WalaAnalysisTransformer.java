package nl.wvdzwan.lapp.callgraph.wala;

import nl.wvdzwan.lapp.callgraph.ClassArtifactResolver;
import nl.wvdzwan.lapp.core.LappPackage;

public class WalaAnalysisTransformer {

    public static LappPackage toPackage(WalaAnalysisResult analysisResult, ClassArtifactResolver artifactResolver) {
        LappPackageBuilder builder = new LappPackageBuilder(artifactResolver);

        ClassHierarchyInserter chaInserter = new ClassHierarchyInserter(analysisResult.extendedCha, builder);
        chaInserter.insertCHA();

        CallGraphInserter cgInserter = new CallGraphInserter(analysisResult.cg, analysisResult.extendedCha, builder);
        cgInserter.insertCallGraph();

        return builder.getLappPackage();
    }
}
