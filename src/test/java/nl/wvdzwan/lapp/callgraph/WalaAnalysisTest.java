package nl.wvdzwan.lapp.callgraph;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ipa.cha.ClassHierarchyException;
import org.jgrapht.Graph;

import nl.wvdzwan.lapp.IRDotMerger.AnnotatedVertex;
import nl.wvdzwan.lapp.Method.Method;
import nl.wvdzwan.lapp.callgraph.outputs.GraphEdge;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WalaAnalysisTest {

    static String resourceSubFolder = "example_jars";
    static String expectationsFolder = "WalaAnalysisTest";


    void run() throws IOException, ClassHierarchyException {

        String mainJar = getResourcePath("com.company$app$3.2.jar");
        String classpath = Stream.of("com.company$core$1.1.jar", "com.company$extension-a$1.0.jar")
                .map(this::getResourcePath)
                .collect(Collectors.joining(":"));


        WalaAnalysis analysis = new WalaAnalysis(mainJar, classpath, "Java60RegressionExclusions.txt");
        WalaAnalysisResult analysisResult = analysis.run();

        WalaGraphTransformer graphBuilder = new WalaGraphTransformer(analysisResult.cg, analysisResult.extendedCha, StubClassResolver.build());
        Graph<Method, GraphEdge> graph = graphBuilder.build();


        DoAnalysisAssertions(analysis, analysisResult.cg);
        DoGraphBuilderAssertions(graph);
    }

    private void DoAnalysisAssertions(WalaAnalysis analysis, CallGraph cg) {
        assertTrue(analysis.getExtendedCha().getNumberOfClasses() > 0);
        assertTrue(cg.getClassHierarchy().getNumberOfClasses() > 0);

        assertTrue(analysis.getExtendedCha().getNumberOfClasses() > cg.getClassHierarchy().getNumberOfClasses());
    }

    private void DoGraphBuilderAssertions(Graph<Method, GraphEdge> graph) throws IOException {
//        MakeDynamicNodeAssertions(graph.getExternalNodes());

//        MakeDynamicEdgesAssertions(graph.getDynamicEdges());

//        MakeGraphAssertions(graph.getInnerGraph());
    }

    private void MakeDynamicNodeAssertions(Collection<AnnotatedVertex> externalNodes) throws IOException {
        String externalNodeList = externalNodes.stream()
                .map(annotatedVertex -> {
                    return annotatedVertex.getNamespace() + "." + annotatedVertex.getSymbol();
                })
                .sorted()
                .collect(Collectors.joining("\n"));


        String expectedNodes = getExpectationFileAsString("dynamic_nodes.txt");
        assertEquals(expectedNodes, externalNodeList);
    }

    private void MakeDynamicEdgesAssertions(Collection<DynamicEdge> dynamicEdges) throws IOException {
        String externalEdgeList = dynamicEdges.stream()
                .map(edge -> {
                    return String.format("%-16s", edge.getEdgeType().getLabel()) +
                            " : " + edge.getSrc().getNamespace() + "." + edge.getSrc().getSymbol() +
                            "  ->  " + edge.getDst().getNamespace() + "." + edge.getDst().getSymbol();
                })
                .sorted()
                .collect(Collectors.joining("\n"));


        String expectedEdges = getExpectationFileAsString("dynamic_edges.txt");
        assertEquals(expectedEdges, externalEdgeList);
    }

    private String getExpectationFileAsString(String file) throws IOException {
        return new String(
                Files.readAllBytes(
                        Paths.get(
                                getResourcePath(expectationsFolder, file))));
    }

    private void MakeGraphAssertions(Graph<AnnotatedVertex, GraphEdge> graph) throws IOException {

        String nodes = graph.vertexSet().stream()
                .map(annotatedVertex -> {
                    return annotatedVertex.getNamespace() + "." + annotatedVertex.getSymbol();
                })
                .sorted()
                .collect(Collectors.joining("\n"));

        String edges = graph.edgeSet().stream()
                .map(edge -> {
                    AnnotatedVertex src = graph.getEdgeSource(edge);
                    AnnotatedVertex dst = graph.getEdgeTarget(edge);
                    return String.format("%-16s", edge.getLabel()) +
                            " : " + src.getNamespace() + "." + src.getSymbol() +
                            "  ->  " + dst.getNamespace() + "." + dst.getSymbol();
                })
                .sorted()
                .collect(Collectors.joining("\n"));

        String expectedNodes = getExpectationFileAsString("graph_nodes.txt");
        String expectedEdges = getExpectationFileAsString("graph_edges.txt");

        assertEquals(expectedNodes, nodes);
        assertEquals(expectedEdges, edges);
    }

    private String getResourcePath(String... file) {
        String path = Paths.get(resourceSubFolder, file).toString();
        return getClass().getResource("/" + path).getFile();

    }
}