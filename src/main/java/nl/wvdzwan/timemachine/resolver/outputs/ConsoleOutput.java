package nl.wvdzwan.timemachine.resolver.outputs;

import org.eclipse.aether.resolution.DependencyResult;

import nl.wvdzwan.timemachine.OutputTask;
import nl.wvdzwan.timemachine.resolver.util.ConsoleDependencyGraphDumper;

public class ConsoleOutput implements OutputTask<DependencyResult> {

    @Override
    public boolean makeOutput(DependencyResult result) {
        return result.getRoot().accept(new ConsoleDependencyGraphDumper());
    }
}
