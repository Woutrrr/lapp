package nl.wvdzwan.lapp.callgraph.outputs.calls;

import java.io.IOException;
import java.io.Writer;

import nl.wvdzwan.lapp.core.LappPackage;
import nl.wvdzwan.lapp.call.Edge;
import nl.wvdzwan.lapp.callgraph.outputs.LappPackageOutput;

public class ResolvedCallOutput implements LappPackageOutput {

    private final Writer output;

    public ResolvedCallOutput (Writer writer) {
        this.output = writer;
    }

    @Override
    public boolean export(LappPackage lappPackage) {

        try {
            for (Edge edge : lappPackage.resolvedCalls) {
                output.write(edge.source.toID() + " -> " + edge.target.toID() + " :" + edge.getLabel());
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
