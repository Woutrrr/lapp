package nl.wvdzwan.lapp.callgraph.outputs;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import nl.wvdzwan.lapp.callgraph.DynamicEdge;
import nl.wvdzwan.lapp.callgraph.IRGraph;

public class DynamicEdgesExport implements CallGraphOutput
{
    private final File output;

    public DynamicEdgesExport(File output) {
        this.output = output;
    }

    @Override
    public boolean export(IRGraph graph) {

        PrintWriter printWriter;
        try {
            printWriter = new PrintWriter(new FileWriter(output));
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        for (DynamicEdge edge : graph.getDynamicEdges()) {

            String line = String.format("%-16s", edge.getEdgeType().getLabel()) +
                    " : " + edge.getSrc().getNamespace() + "." + edge.getSrc().getSymbol() +
                    "  ->  " + edge.getDst().getNamespace() + "." + edge.getDst().getSymbol();

            printWriter.println(line);
        }
        printWriter.flush();

        return false;
    }
}
