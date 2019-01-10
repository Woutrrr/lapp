package nl.wvdzwan.timemachine.IRDotMerger;

import java.io.*;

import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.io.ComponentUpdater;
import org.jgrapht.io.DOTExporter;
import org.jgrapht.io.DOTImporter;
import org.jgrapht.io.EdgeProvider;
import org.jgrapht.io.ImportException;
import org.jgrapht.io.VertexProvider;

import nl.wvdzwan.timemachine.callgraph.outputs.GraphEdge;

public class IRDotMerger {

    public static void main(String[] args) throws IOException, ImportException {

        if (args.length < 2) {
            System.out.println("Not enough parameters!");
            System.out.println("Usage: dotmerger file1 file2 [output_file]");
            System.exit(1);
        }

        File file1 = new File(args[0]);
        if (!file1.exists()) {
            System.err.println(String.format("File %s not found!", args[0]));
            System.exit(1);
        }

        File file2 = new File(args[1]);
        if (!file2.exists()) {
            System.err.println(String.format("File %s not found!", args[1]));
            System.exit(1);
        }


        Graph<AnnotatedVertex, GraphEdge> graph = importGraphs(file1, file2);

        try {
            Writer writer;
            if (args.length == 3) {
                writer = new FileWriter(args[3]);
            } else {
                writer = new StringWriter();
            }

            exportGraph(graph, writer);

            if (args.length == 2) {
                System.out.println(writer.toString());
            }


        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }

    }

    public static void exportGraph(Graph<AnnotatedVertex, GraphEdge> graph, Writer writer) {
        DOTExporter<AnnotatedVertex, GraphEdge> exporter = new DOTExporter<>(
                MergedIRExporter::vertexIdProvider,
                MergedIRExporter::vertexLabelProvider,
                MergedIRExporter::edgeLabelProvider,
                MergedIRExporter::vertexAttributeProvider,
                MergedIRExporter::edgeAttributeProvider);

        exporter.putGraphAttribute("overlap", "false");
        exporter.putGraphAttribute("ranksep", "1");


        exporter.exportGraph(graph, writer);
    }

    public static Graph<AnnotatedVertex, GraphEdge> importGraphs(File file1, File file2) throws IOException, ImportException {
        VertexProvider<AnnotatedVertex> vertexProvider = AnnotatedVertex::new;

        EdgeProvider<AnnotatedVertex, GraphEdge> edgeProvider = (from, to, label, attributes) -> {
            switch (label) {
                case "invoke_interface":
                    return new GraphEdge.InterfaceDispatchEdge();
                case "invoke_virtual":
                    return new GraphEdge.VirtualDispatchEdge();
                case "invoke_special":
                    return new GraphEdge.SpecialDispatchEdge();
                case "invoke_static":
                    return new GraphEdge.SpecialDispatchEdge();
                case "overridden by":
                    return new GraphEdge.OverridesEdge();
                case "implemented by":
                    return new GraphEdge.ImplementsEdge();
                default:
                    assert false : "Unknown edge";
                    return null;
            }
        };

        ComponentUpdater<AnnotatedVertex> vertexUpdater = AnnotatedVertex::mergeAttributes;

        DOTImporter<AnnotatedVertex, GraphEdge> importer = new DOTImporter<>(
                vertexProvider,
                edgeProvider,
                vertexUpdater);

        Graph<AnnotatedVertex, GraphEdge> graph = new DefaultDirectedGraph<>(GraphEdge.class);

        Reader reader = new BufferedReader(
                new MergedInputReader(
                        new FileReader(file1),
                        new FileReader(file2)
                ));
        // Use combined reader, because importGraph won't update existing vertices in a second call
        importer.importGraph(graph, reader);

        return graph;
    }

}
