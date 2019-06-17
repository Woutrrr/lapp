package nl.wvdzwan.lapp.callgraph;

import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.types.MethodReference;

public class StubClassResolver implements ClassArtifactResolver {
    public static ClassArtifactResolver build() {
        return new StubClassResolver();
    }

    @Override
    public ArtifactRecord artifactRecordFromMethodReference(MethodReference n) {
        return new ArtifactRecord("company:testArtifact:1.0");
    }

    @Override
    public ArtifactRecord artifactRecordFromClass(IClass klass) {
        return new ArtifactRecord("company:testArtifact:1.0");
    }
}
