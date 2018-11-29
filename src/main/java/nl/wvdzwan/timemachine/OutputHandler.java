package nl.wvdzwan.timemachine;

import java.util.ArrayList;
import java.util.List;

public class OutputHandler<T> {

    private List<OutputTask<T>> outputs = new ArrayList<>();

    public boolean process(T result) {

        boolean success = outputs.stream()
                .map(temp -> temp.makeOutput(result))
                .allMatch(Boolean::booleanValue);

        return success;
    }

    public boolean add(OutputTask<T> output) {
        if (output == null) {
            return false;
        }

        return outputs.add(output);
    }

}
