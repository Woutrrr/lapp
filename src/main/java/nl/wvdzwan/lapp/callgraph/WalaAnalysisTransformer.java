package nl.wvdzwan.lapp.callgraph;

import nl.wvdzwan.lapp.LappPackage;

public class WalaAnalysisTransformer {

    public static LappPackage toPackage(WalaAnalysisResult analysisResult, ClassArtifactResolver artifactResolver) {
        PackageBuilder builder = new PackageBuilder(artifactResolver);

        ClassHierarchyInserter chaInserter = new ClassHierarchyInserter(analysisResult.extendedCha, builder);
        chaInserter.insertCHA();

        CallGraphInserter cgInserter = new CallGraphInserter(analysisResult.cg, analysisResult.extendedCha, builder);
        cgInserter.insertCallGraph();

        return builder.getLappPackage();
    }
}
