package nl.wvdzwan.lapp.callgraph.outputs;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import nl.wvdzwan.lapp.IRDotMerger.AnnotatedVertex;
import nl.wvdzwan.lapp.callgraph.IRGraph;

public class DynamicNodesExport implements CallGraphOutput
{
    private final File output;

    public DynamicNodesExport(File output) {
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

        for (AnnotatedVertex vertex : graph.getExternalNodes()) {
            String label = vertex.getNamespace() + "." + vertex.getSymbol();

            if (vertex.getAttributes().containsKey(AttributeMap.TYPE)) {
                label = "" + vertex.getAttributes().get(AttributeMap.TYPE) + " - " + label;
            }

            printWriter.println(label);
        }
        printWriter.flush();

        return true;
    }
}
