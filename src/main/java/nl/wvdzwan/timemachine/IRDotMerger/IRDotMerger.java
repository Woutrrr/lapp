package nl.wvdzwan.timemachine.IRDotMerger;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.io.*;

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

    private static void exportGraph(Graph<AnnotatedVertex, GraphEdge> graph, Writer writer) {
        DOTExporter<AnnotatedVertex, GraphEdge> exporter = new DOTExporter<>(
                IRDotExporter::vertexIdProvider,
                IRDotExporter::vertexLabelProvider,
                IRDotExporter::edgeLabelProvider,
                IRDotExporter::vertexAttributeProvider,
                IRDotExporter::edgeAttributeProvider);

        exporter.putGraphAttribute("overlap", "false");
        exporter.putGraphAttribute("ranksep", "1");


        exporter.exportGraph(graph, writer);
    }

    private static Graph<AnnotatedVertex, GraphEdge> importGraphs(File file1, File file2) throws IOException, ImportException {
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

    static class AnnotatedVertex {
        private String name;
        private Map<String, Attribute> attributesMap;

        AnnotatedVertex(String name, Map<String, Attribute> attributes) {
            this.name = name;
            this.attributesMap = new HashMap<>(attributes);
        }

        void mergeAttributes(Map<String, Attribute> attributes) {
            for (Map.Entry<String, Attribute> entry : attributes.entrySet()) {
                attributesMap.merge(
                        entry.getKey(),
                        entry.getValue(),
                        (a, a2) -> a.getValue().length() >= a2.getValue().length() ? a : a2);
            }
        }

        public String getName() {
            return name;
        }

        public int hashCode() {
            return name.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            return (obj instanceof AnnotatedVertex) && name.equals(((AnnotatedVertex) obj).name);
        }
    }

    static class IRDotExporter {



        protected static String vertexIdProvider(AnnotatedVertex vertex) {
            return "\"" + vertex.name + "\""; //.replaceAll("[\\.\\(\\)<>/;]", "_");
        }

        protected static String vertexLabelProvider(AnnotatedVertex vertex) {
            String label = vertex.getName();
            Map<String, Attribute> attributes = vertex.attributesMap;

            if (attributes != null && attributes.containsKey("type")) {
                label = "" + attributes.get("type").getValue() + " - " + label;
            }
            return label;
        }

        protected static Map<String, Attribute> vertexAttributeProvider(AnnotatedVertex vertex) {
            return vertex.attributesMap;
        }

        public static String edgeLabelProvider(GraphEdge edge) {
            return edge.getLabel();
        }

        public static Map<String, Attribute> edgeAttributeProvider(GraphEdge edge) {
            Map<String, Attribute> attributes = new HashMap<>();

            if (edge instanceof GraphEdge.InterfaceDispatchEdge) {
                Attribute attribute = DefaultAttribute.createAttribute("bold");
                attributes.put("style", attribute);
            } else if (edge instanceof GraphEdge.VirtualDispatchEdge) {
                Attribute attribute = DefaultAttribute.createAttribute("bold");
                attributes.put("style", attribute);
            } else if (edge instanceof GraphEdge.ImplementsEdge) {
                Attribute attribute = DefaultAttribute.createAttribute("dashed");
                attributes.put("style", attribute);
            } else if (edge instanceof GraphEdge.OverridesEdge) {
                Attribute attribute = DefaultAttribute.createAttribute("dotted");
                attributes.put("style", attribute);
            } else {
                return null;
            }

            return attributes;
        }

    }




    static class MergedInputReader extends Reader {

        Reader f1;
        Reader f2;
        boolean read_from_second_file = false;
        boolean skipped_header = false;

        MergedInputReader(Reader f1, Reader f2) {
            this.f1 = f1;
            this.f2 = f2;
        }


        @Override
        public int read(char[] cbuf, int off, int len) throws IOException {

            if (!read_from_second_file) {
                int res = f1.read(cbuf, off, len);

                char last = cbuf[res - 2];
                char lastt = cbuf[res - 1];


                // Reached end of file 1?
                if (res < len || (last == '}' && lastt == '\n')) {
                    read_from_second_file = true;
                    res = res - 2;


                    // Skip graph header of file 2
                    while (!skipped_header) {
                        int temp = f2.read();
                        if (temp == -1 || temp == '{') {
                            skipped_header = true;
                        }
                    }


                    int subRes = f2.read(cbuf, off + res, len - res);
                    if (subRes < 0) {
                        return subRes;
                    }
                    return res + subRes;
                }

                return res;

            } else {
                // Read only from second file
                return f2.read(cbuf, off, len);
            }
        }


        @Override
        public void close() throws IOException {
            f1.close();
            f2.close();
        }
    }
}
