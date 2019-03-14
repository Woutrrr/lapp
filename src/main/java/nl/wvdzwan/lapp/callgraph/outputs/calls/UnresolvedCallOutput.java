package nl.wvdzwan.lapp.callgraph.outputs.calls;

import java.io.IOException;
import java.io.Writer;

import nl.wvdzwan.lapp.core.LappPackage;
import nl.wvdzwan.lapp.call.Edge;
import nl.wvdzwan.lapp.callgraph.outputs.LappPackageOutput;

public class UnresolvedCallOutput implements LappPackageOutput {

    private final Writer output;

    public UnresolvedCallOutput(Writer writer) {
        this.output = writer;
    }

    @Override
    public boolean export(LappPackage lappPackage) {

        try {
            for (Edge edge : lappPackage.unresolvedCalls) {
                output.write(edge.source.toID() + " \t->\t " + edge.target.toID() + " :" + edge.getLabel());
                output.write("\n");
            }
            output.flush();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }
}
