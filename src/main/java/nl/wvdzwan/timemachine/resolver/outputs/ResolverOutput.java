package nl.wvdzwan.timemachine.resolver.outputs;

import org.eclipse.aether.resolution.DependencyResult;

public interface ResolverOutput {

    boolean makeOutput(DependencyResult result);

}
