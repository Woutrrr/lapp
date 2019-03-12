package nl.wvdzwan.lapp.callgraph;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.ibm.wala.ipa.cha.ClassHierarchyException;
import org.jgrapht.Graph;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import nl.wvdzwan.lapp.Method.Method;
import nl.wvdzwan.lapp.call.Edge;
import nl.wvdzwan.lapp.callgraph.outputs.resolved_calls.ResolvedCallOutput;
import nl.wvdzwan.lapp.callgraph.outputs.resolved_calls.UnresolvedCallOutput;
import nl.wvdzwan.lapp.callgraph.outputs.resolved_methods.ResolvedMethodOutput;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WalaAnalysisTest {

    static String resourceSubFolder = "example_jars";
    static String expectationsFolder = "WalaAnalysisTest";

    protected static WalaAnalysisResult analysisResult;
    protected static Graph<Method, Edge> graph;

    @BeforeAll
    static void run() throws IOException, ClassHierarchyException {

        String mainJar = getResourcePath("com.company$app$3.2.jar");
        String classpath = Stream.of("com.company$core$1.1.jar", "com.company$extension-a$1.0.jar")
                .map(WalaAnalysisTest::getResourcePath)
                .collect(Collectors.joining(":"));


        WalaAnalysis analysis = new WalaAnalysis(mainJar, classpath, "Java60RegressionExclusions.txt");
        analysisResult = analysis.run();

        WalaGraphTransformer graphBuilder = new WalaGraphTransformer(analysisResult.cg, analysisResult.extendedCha, StubClassResolver.build());
        graph = graphBuilder.build();

    }

    @Test
    void DoAnalysisAssertions() {
        assertTrue(analysisResult.extendedCha.getNumberOfClasses() > 0);
        assertTrue(analysisResult.cg.getClassHierarchy().getNumberOfClasses() > 0);

        assertTrue(analysisResult.extendedCha.getNumberOfClasses() > analysisResult.cg.getClassHierarchy().getNumberOfClasses());
    }


    @Test
    void verifyResolvedMethodOutput() throws IOException {

        StringWriter writer = new StringWriter();

        ResolvedMethodOutput resolvedMethodOutput = new ResolvedMethodOutput(writer);
        resolvedMethodOutput.export(graph);

        String result = writer.toString();

        String[] lines = result.split("\\n");
        Arrays.sort(lines);

        String sortedResult = String.join("\n", lines);

        String expectedNodes = getExpectationFileAsString("resolved_methods.txt");
        assertEquals(expectedNodes, sortedResult);
    }

    @Test
    void verifyResolvedCallOutput() throws IOException {

        StringWriter writer = new StringWriter();

        ResolvedCallOutput resolvedCallOutput = new ResolvedCallOutput(writer);
        resolvedCallOutput.export(graph);

        String result = writer.toString();
        String[] lines = result.split("\\n");
        Arrays.sort(lines);
        String sortedResult = String.join("\n", lines);

        String expectedNodes = getExpectationFileAsString("resolved_calls.txt");
        assertEquals(expectedNodes, sortedResult);
    }

    @Test
    void verifyUnresolvedCallOutput() throws IOException {

        StringWriter writer = new StringWriter();

        UnresolvedCallOutput unresolvedCallOutput = new UnresolvedCallOutput(writer);
        unresolvedCallOutput.export(graph);

        String result = writer.toString();
        String[] lines = result.split("\\n");
        Arrays.sort(lines);
        String sortedResult = String.join("\n", lines);

        String expectedNodes = getExpectationFileAsString("unresolved_calls.txt");
        assertEquals(expectedNodes, sortedResult);
    }


    private String getExpectationFileAsString(String file) throws IOException {
        return new String(
                Files.readAllBytes(
                        Paths.get(
                                getResourcePath(expectationsFolder, file))));
    }


    private static String getResourcePath(String... file) {
        String path = Paths.get(resourceSubFolder, file).toString();
        return WalaAnalysisTest.class.getResource("/" + path).getFile();

    }
}