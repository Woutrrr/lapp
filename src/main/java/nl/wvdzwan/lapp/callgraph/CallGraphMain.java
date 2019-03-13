package nl.wvdzwan.lapp.callgraph;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.concurrent.Callable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jgrapht.Graph;
import picocli.CommandLine;

import nl.wvdzwan.lapp.LappPackage;
import nl.wvdzwan.lapp.LappPackageTransformer;
import nl.wvdzwan.lapp.Method.Method;
import nl.wvdzwan.lapp.call.Edge;
import nl.wvdzwan.lapp.callgraph.FolderLayout.DollarSeparatedLayout;
import nl.wvdzwan.lapp.callgraph.outputs.GraphVizOutput;
import nl.wvdzwan.lapp.callgraph.outputs.HumanReadableDotGraph;
import nl.wvdzwan.lapp.callgraph.outputs.UnifiedCallGraphExport;
import nl.wvdzwan.lapp.callgraph.outputs.resolved_methods.ResolvedMethodOutput;

@CommandLine.Command(
        name = "callgraph",
        description = "Create a call graph from resolve output"
)
public class CallGraphMain implements Callable<Void> {
    private static final Logger logger = LogManager.getLogger();

    @CommandLine.Option(
            names = {"-e", "--exclusion"},
            description = "Location of exclusion file"
    )
    private String exclusionFile = "Java60RegressionExclusions.txt";

    @CommandLine.Option(
            names = {"-o", "--output"},
            description = "Output folder"
    )
    private File outputDirectory = new File("output");

    @CommandLine.Option(
            names = {"-i", "--in-place"},
            description = "Use location of first argument as output folder and ignore output option"
    )
    private boolean inPlace = false;

    @CommandLine.Option(
            names = {"-c", "--classpath"},
            description = "Read first argument as classpath file"
    )
    private boolean isClassPath = false;

    @CommandLine.Option(
            names = {"-j", "--jars"},
            description = "Location of jars, defaults to folder of mainJar or \"jar/\" relative to classpath.txt"
    )
    private Path jarsLocation;

    @CommandLine.Parameters(
            index = "0",
            paramLabel = "jar/classpath file",
            description = "Jar or classpath file to generate IR graph of."
    )
    private String jar;

    @CommandLine.Parameters(
            index = "1..*",
            arity = "0..*",
            paramLabel = "dependencies",
            description = "Dependency jars consider when building IR graph."
    )
    private ArrayList<String> dependencies = new ArrayList<>();


    @Override
    public Void call() throws Exception {


        if (inPlace) {
            outputDirectory = Paths.get(jar).getParent().toFile();
        }

        // Setup
        if (isClassPath || jar.endsWith("classpath.txt")) {

            if (jarsLocation == null) {
                jarsLocation = Paths.get(jar).getParent().resolve("jars");
            }

            if (!parseClassPathFile(jarsLocation)) {
                logger.error("Error parsing classpath file \"{}\"", jar);
                return null;
            }
        }

        if (jarsLocation == null) {
            jarsLocation = Paths.get(jar).getParent();
        }


        String dependencyClassPath;
        try {
            dependencyClassPath = makeAndVerifyDependencyClassPath();
        } catch (FileNotFoundException e) {
            logger.error(e.getMessage());
            return null;
        }


        // Analysis
        logger.info("Starting analysis for {} with dependencies: {}", jar, dependencyClassPath);
        WalaAnalysis analysis = new WalaAnalysis(jar, dependencyClassPath, exclusionFile);
        WalaAnalysisResult analysisResult = analysis.run();

        // Build IR graph
        ClassToArtifactResolver artifactResolver = new ClassToArtifactResolver(analysisResult.extendedCha, new DollarSeparatedLayout());
        LappPackage lappPackage = WalaAnalysisTransformer.toPackage(analysisResult, artifactResolver);

        Graph<Method, Edge> graph = LappPackageTransformer.toGraph(lappPackage);

        // Output
        GraphVizOutput dotOutput = new UnifiedCallGraphExport(graph);
        FileWriter writer = new FileWriter(new File(outputDirectory, "app.dot"));
        dotOutput.export(writer);

        GraphVizOutput humanOutput = new HumanReadableDotGraph(graph);
        FileWriter writerHuman = new FileWriter(new File(outputDirectory, "app_human.dot"));
        humanOutput.export(writerHuman);

        ResolvedMethodOutput resolvedMethodOutput = new ResolvedMethodOutput(new FileWriter(new File(outputDirectory, "resolved_methods.txt")));
        resolvedMethodOutput.export(graph);

        return null;
    }

    private boolean parseClassPathFile(Path jarsLocation) {
        File classpath = new File(jar);


        ClassPathFile classPathFile;
        try {
            classPathFile = new ClassPathFile(classpath, jarsLocation);
        } catch (FileNotFoundException e) {
            return false;
        }

        jar = classPathFile.getMainJar();
        dependencies.addAll(classPathFile.getDependencies());

        return true;
    }

    private String makeAndVerifyDependencyClassPath() throws FileNotFoundException {

        ArrayList<String> missing = new ArrayList<>();

        for (String s : dependencies) {
            Path p = Paths.get(s);

            if (!Files.isRegularFile(p)) {
                missing.add(p.toString());
            }
        }

        if (missing.size() != 0) {
            throw new FileNotFoundException("Could not find dependencies: " + missing.toString());
        }

        return String.join(":", dependencies);
    }

}
