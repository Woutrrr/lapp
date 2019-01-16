package nl.wvdzwan.lapp.resolver.outputs;


import org.eclipse.aether.resolution.DependencyResult;

public interface ResolveOutputTask {

    boolean makeOutput(DependencyResult result);

}
