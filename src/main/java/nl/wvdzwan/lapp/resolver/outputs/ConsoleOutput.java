package nl.wvdzwan.lapp.resolver.outputs;

import org.eclipse.aether.resolution.DependencyResult;

import nl.wvdzwan.lapp.resolver.util.ConsoleDependencyGraphDumper;

public class ConsoleOutput implements ResolveOutputTask {

    @Override
    public boolean makeOutput(DependencyResult result) {
        return result.getRoot().accept(new ConsoleDependencyGraphDumper());
    }
}
