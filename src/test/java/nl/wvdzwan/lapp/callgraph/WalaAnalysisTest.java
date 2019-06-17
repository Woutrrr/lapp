package nl.wvdzwan.lapp.callgraph;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.ibm.wala.ipa.cha.ClassHierarchyException;
import org.jgrapht.Graph;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import nl.wvdzwan.lapp.LappPackageTransformer;
import nl.wvdzwan.lapp.call.Call;
import nl.wvdzwan.lapp.callgraph.FolderLayout.DollarSeparatedLayout;
import nl.wvdzwan.lapp.callgraph.wala.WalaAnalysisResult;
import nl.wvdzwan.lapp.callgraph.wala.WalaAnalysisTransformer;
import nl.wvdzwan.lapp.core.LappPackage;
import nl.wvdzwan.lapp.core.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WalaAnalysisTest {

    static String resourceSubFolder = "example_jars";
    static String expectationsFolder = "WalaAnalysisTest";

    protected static WalaAnalysisResult analysisResult;
    protected static Graph<Method, Call> graph;
    protected static LappPackage lappPackage;

    @BeforeAll
    static void run() throws IOException, ClassHierarchyException {

        String mainJar = getResourcePath("com.company$app$3.2.jar");
        String classpath = Stream.of("com.company$core$1.1.jar", "com.company$extension-a$1.0.jar")
                .map(WalaAnalysisTest::getResourcePath)
                .collect(Collectors.joining(":"));


        WalaAnalysis analysis = new WalaAnalysis(mainJar, classpath, "Java60RegressionExclusions.txt");
        analysisResult = analysis.run();

        lappPackage = WalaAnalysisTransformer.toPackage(analysisResult, new DollarSeparatedLayout());
        graph = LappPackageTransformer.toGraph(lappPackage);

    }

    @Test
    void DoAnalysisAssertions() {
        assertTrue(analysisResult.extendedCha.getNumberOfClasses() > 0);
        assertTrue(analysisResult.cg.getClassHierarchy().getNumberOfClasses() > 0);

        assertTrue(analysisResult.extendedCha.getNumberOfClasses() >= analysisResult.cg.getClassHierarchy().getNumberOfClasses());
    }

//    @Test
//    void verifyResolvedmethods() throws IOException {
//        Set<ResolvedMethod> methods = lappPackage.methods;
//
//        String actual = methods.stream()
//                .map(ResolvedMethod::toID)
//                .sorted()
//                .collect(Collectors.joining("\n"));
//
//        String expectedNodes = getExpectationFileAsString("resolved_methods.txt");
//        assertEquals(expectedNodes, actual);
//
//    }

    @Test
    void verifyResolvedCalls() throws IOException {
        Set<Call> calls = lappPackage.resolvedCalls;

        String actual = calls.stream()
                .map(edge -> edge.source.toID() + " -> " + edge.target.toID() + " :" + edge.getLabel())
                .sorted()
                .collect(Collectors.joining("\n"));

        String expectedNodes = getExpectationFileAsString("resolved_calls.txt");
        assertEquals(expectedNodes, actual);
    }

    @Test
    void verifyUnresolvedCalls() throws IOException {
        Set<Call> calls = lappPackage.unresolvedCalls;

        String actual = calls.stream()
                .map(edge -> edge.source.toID() + " -> " + edge.target.toID() + " :" + edge.getLabel())
                .sorted()
                .collect(Collectors.joining("\n"));

        String expectedNodes = getExpectationFileAsString("unresolved_calls.txt");
        assertEquals(expectedNodes, actual);
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