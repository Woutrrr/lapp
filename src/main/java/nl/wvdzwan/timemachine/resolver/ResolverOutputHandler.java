package nl.wvdzwan.timemachine.resolver;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.aether.resolution.DependencyResult;

import nl.wvdzwan.timemachine.resolver.outputs.ResolverOutput;

public class ResolverOutputHandler {

    private List<ResolverOutput> outputs = new ArrayList<>();

    public boolean process(DependencyResult result) {

        boolean success = outputs.stream()
                .map(temp -> temp.makeOutput(result))
                .allMatch(Boolean::booleanValue);

        return success;
    }

    public boolean add(ResolverOutput output) {
        if (output == null) {
            return false;
        }

        return outputs.add(output);
    }

}
