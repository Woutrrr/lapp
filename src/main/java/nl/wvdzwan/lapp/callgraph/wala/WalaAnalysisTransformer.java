package nl.wvdzwan.lapp.callgraph.wala;

import com.ibm.wala.classLoader.IClassLoader;
import com.ibm.wala.types.ClassLoaderReference;

import nl.wvdzwan.lapp.callgraph.ClassToArtifactResolver;
import nl.wvdzwan.lapp.callgraph.FolderLayout.ArtifactFolderLayout;
import nl.wvdzwan.lapp.core.LappPackage;

public class WalaAnalysisTransformer {

    public static LappPackage toPackage(WalaAnalysisResult analysisResult,  ArtifactFolderLayout layout, boolean analyseStdlib) {

        IClassLoader classLoader = analysisResult.extendedCha.getLoader(ClassLoaderReference.Application);

        if (analyseStdlib) {
            classLoader = analysisResult.extendedCha.getLoader(ClassLoaderReference.Primordial);
        }

        ClassToArtifactResolver artifactResolver = new ClassToArtifactResolver(analysisResult.extendedCha, layout, classLoader);

        LappPackageBuilder builder = new LappPackageBuilder(artifactResolver, layout, classLoader);

        return builder.setPackages(analysisResult.extendedCha.getScope().getModules(classLoader.getReference()))
                .insertCha(analysisResult.extendedCha)
                .insertCallGraph(analysisResult.cg)
                .build();
    }
}
