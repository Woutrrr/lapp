package nl.wvdzwan.lapp.IRDotMerger;

import java.io.*;
import java.util.concurrent.Callable;

import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.io.ImportException;
import picocli.CommandLine;

import nl.wvdzwan.lapp.callgraph.outputs.UnifiedCallGraphExport;
import nl.wvdzwan.lapp.callgraph.importers.GraphVizImporter;
import nl.wvdzwan.lapp.callgraph.importers.UnifiedCallGraphImport;
import nl.wvdzwan.lapp.callgraph.outputs.GraphEdge;

@CommandLine.Command(
        name = "merge",
        description = "Merge two IR graphs"
)
public class IRDotMerger implements Callable<Void> {

    @CommandLine.Parameters(
            index = "0",
            paramLabel = "graph 1",
            description = "graph 1"
    )
    private File file1;

    @CommandLine.Parameters(
            index = "1",
            paramLabel = "graph 2",
            description = "graph 2"
    )
    private File file2;

    @CommandLine.Option(
            names = {"--output", "-o"},
            description = "Output file"
    )
    private File outputFile = null;


    @Override
    public Void call() throws Exception {

        if (!file1.exists()) {
            System.err.println(String.format("File %s not found!", file1));
            return null;
        }

        if (!file2.exists()) {
            System.err.println(String.format("File %s not found!", file2));
            return null;
        }


        Graph<AnnotatedVertex, GraphEdge> graph = importGraphs(file1, file2);


        Writer writer;
        if (outputFile != null) {
            writer = new FileWriter(outputFile);
        } else {
            writer = new StringWriter();
        }

        UnifiedCallGraphExport exporter = new UnifiedCallGraphExport(graph);
        exporter.export(writer);

        if (outputFile == null) {
            System.out.println(writer.toString());
        }

        return null;
    }


    public static Graph<AnnotatedVertex, GraphEdge> importGraphs(File file1, File file2) throws
            IOException, ImportException {


        Graph<AnnotatedVertex, GraphEdge> graph = new DefaultDirectedGraph<>(GraphEdge.class);
        // Use combined reader, because importGraph won't update existing vertices in a second call
        Reader reader = new BufferedReader(
                new MergedInputReader(
                        new FileReader(file1),
                        new FileReader(file2)
                ));


        GraphVizImporter importer = new UnifiedCallGraphImport(graph);
        importer.importGraph(reader);

        return graph;
    }


}
