package nl.wvdzwan.timemachine.resolver.outputs;


import org.eclipse.aether.resolution.DependencyResult;

public interface ResolveOutputTask {

    boolean makeOutput(DependencyResult result);

}
