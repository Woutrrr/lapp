package nl.wvdzwan.lapp.resolver.outputs;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.aether.resolution.DependencyResult;

public class OutputHandler {

    private List<ResolveOutputTask> outputs = new ArrayList<>();

    public boolean process(DependencyResult result) {

        boolean success = outputs.stream()
                .allMatch(temp -> temp.makeOutput(result));

        return success;
    }

    public boolean add(ResolveOutputTask output) {
        if (output == null) {
            return false;
        }

        return outputs.add(output);
    }

}
