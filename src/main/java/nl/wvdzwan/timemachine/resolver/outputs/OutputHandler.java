package nl.wvdzwan.timemachine.resolver.outputs;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.aether.resolution.DependencyResult;

public class OutputHandler {

    private List<ResolveOutputTask> outputs = new ArrayList<>();

    public boolean process(DependencyResult result) {

        boolean success = outputs.stream()
                .map(temp -> temp.makeOutput(result))
                .allMatch(Boolean::booleanValue);

        return success;
    }

    public boolean add(ResolveOutputTask output) {
        if (output == null) {
            return false;
        }

        return outputs.add(output);
    }

}
