package nl.wvdzwan.lapp.callgraph;

import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.types.MethodReference;

public class StubClassResolver implements ClassArtifactResolver {
    public static ClassArtifactResolver build() {
        return new StubClassResolver();
    }

    @Override
    public String artifactFromMethodReference(MethodReference n) {
        return "company.testArtifact.1.0";
    }

    @Override
    public String artifactFromClass(IClass klass) {
        return "company.testArtifact.1.0";
    }
}
