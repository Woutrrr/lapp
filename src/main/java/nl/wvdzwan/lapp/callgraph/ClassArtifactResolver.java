package nl.wvdzwan.lapp.callgraph;

import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.types.MethodReference;

public interface ClassArtifactResolver {
    String artifactFromMethodReference(MethodReference n);

    String artifactFromClass(IClass klass);
}
