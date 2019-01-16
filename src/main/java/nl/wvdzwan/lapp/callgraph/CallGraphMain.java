package nl.wvdzwan.lapp.callgraph;

import java.io.File;
import java.io.FileWriter;
import java.util.concurrent.Callable;

import com.ibm.wala.ipa.callgraph.CallGraph;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import picocli.CommandLine;

import nl.wvdzwan.lapp.callgraph.FolderLayout.DollarSeparatedLayout;
import nl.wvdzwan.lapp.callgraph.outputs.GraphVizOutput;
import nl.wvdzwan.lapp.callgraph.outputs.UnifiedCallGraphExport;

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

    @CommandLine.Parameters(
            index = "0",
            paramLabel = "jar",
            description = "jar to generate IR graph of."
    )
    private String jar;

    @CommandLine.Parameters(
            index = "1..*",
            arity = "0..*",
            paramLabel = "dependencies",
            description = "Dependency jars consider when building IR graph."
    )
    private String[] dependencies;


    public static void main(String[] args) {
        CommandLine.call(new CallGraphMain(), args);
    }

    @Override
    public Void call() throws Exception {

        // Setup
        String classPath = makeClassPath();

        // Analysis
        logger.info("Starting analysis for {} with dependencies: {}", jar, classPath);
        WalaAnalysis analysis = new WalaAnalysis(jar, classPath, exclusionFile);
        CallGraph cg = analysis.run();

        // Build IR graph
        ClassToArtifactResolver artifactResolver = new ClassToArtifactResolver(analysis.getExtendedCha(), new DollarSeparatedLayout());
        IRGraphBuilder builder = new IRGraphBuilder(cg, analysis.getExtendedCha(), artifactResolver);
        builder.build();

        // Output
        GraphVizOutput dotOutput = new UnifiedCallGraphExport(builder.getGraph());

        FileWriter writer = new FileWriter(new File(outputDirectory, "app.dot"));
        dotOutput.export(writer);

        return null;
    }

    private String makeClassPath() {
        if (dependencies != null) {
            return String.join(":", dependencies);
        }

        return "";
    }
}
