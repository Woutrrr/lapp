package nl.wvdzwan.lapp.callgraph.wala;

import com.ibm.wala.types.ClassLoaderReference;

import nl.wvdzwan.lapp.callgraph.ClassToArtifactResolver;
import nl.wvdzwan.lapp.callgraph.FolderLayout.ArtifactFolderLayout;
import nl.wvdzwan.lapp.core.LappPackage;

public class WalaAnalysisTransformer {

    public static LappPackage toPackage(WalaAnalysisResult analysisResult,  ArtifactFolderLayout layout) {
        ClassToArtifactResolver artifactResolver = new ClassToArtifactResolver(analysisResult.cg.getClassHierarchy(), layout);

        LappPackageBuilder builder = new LappPackageBuilder(artifactResolver, layout);

        return builder.setPackages(analysisResult.cg.getClassHierarchy().getScope().getModules(ClassLoaderReference.Application))
                .insertCha(analysisResult.cg.getClassHierarchy())
                .insertCallGraph(analysisResult.cg)
                .build();
    }
}
