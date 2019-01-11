package nl.wvdzwan.timemachine.filter;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.concurrent.Callable;

import org.jgrapht.graph.DefaultDirectedGraph;
import picocli.CommandLine;

import nl.wvdzwan.timemachine.IRDotMerger.AnnotatedVertex;
import nl.wvdzwan.timemachine.callgraph.outputs.UnifiedCallGraphExport;
import nl.wvdzwan.timemachine.callgraph.importers.UnifiedCallGraphImport;
import nl.wvdzwan.timemachine.callgraph.outputs.GraphEdge;

@CommandLine.Command(
        name = "filter",
        description = "Filter IR graph"
)
public class FilterMain implements Callable<Void> {

    @CommandLine.Option(
            names = {"--output", "-o"},
            description = "Output file"
    )
    private File outputFile = null;

    @CommandLine.Option(
            names = {"--invert", "-i"},
            description = "invert action, so remove supplied libraries"
    )
    private boolean invert = false;

    @CommandLine.Parameters(
            index = "0",
            paramLabel = "graph_file",
            description = "IR graph file to filter."
    )
    private File graphFile;

    @CommandLine.Parameters(
            index = "1..*",
            arity = "0..*",
            paramLabel = "libraries",
            description = "libraries to keep in graph"
    )
    private ArrayList<String> libraries;

    @Override
    public Void call() throws Exception {

        DefaultDirectedGraph<AnnotatedVertex, GraphEdge> graph = new DefaultDirectedGraph<>(GraphEdge.class);
        Reader reader = new FileReader(graphFile);

        UnifiedCallGraphImport importer = new UnifiedCallGraphImport(graph);
        importer.importGraph(reader);

        GraphFilter filter = new GraphFilter(graph);
        filter.libraries(libraries, invert);

        Writer writer;
        if (outputFile != null) {
            writer = new FileWriter(outputFile);
        } else {
            writer = new StringWriter();
        }

        UnifiedCallGraphExport exporter = new UnifiedCallGraphExport(filter.getGraph());
        exporter.export(writer);

        if (outputFile == null) {
            System.out.println(writer.toString());
        }

        return null;
    }
}
