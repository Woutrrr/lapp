package nl.wvdzwan.timemachine.callgraph;

import java.io.File;
import java.util.concurrent.Callable;

import com.ibm.wala.ipa.callgraph.CallGraph;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import picocli.CommandLine;

import nl.wvdzwan.timemachine.callgraph.FolderLayout.ArtifactFolderLayout;
import nl.wvdzwan.timemachine.callgraph.FolderLayout.MavenFolderLayout;
import nl.wvdzwan.timemachine.callgraph.outputs.GraphVizOutput;
import nl.wvdzwan.timemachine.callgraph.outputs.HumanReadableDotGraph;
import nl.wvdzwan.timemachine.resolver.util.Booter;

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
        String classPath = String.join(":", dependencies);

        // Analysis
        logger.info("Starting analysis for {} with dependencies: {}", jar, classPath);
        WalaAnalysis analysis = new WalaAnalysis(jar, classPath, exclusionFile);
        CallGraph cg = analysis.run();

        // Build IR graph
        IRGraphBuilder builder = new IRGraphBuilder(cg, analysis.getExtendedCha());
        builder.build();

        // Output
        String localRepoPrefix = (new File(Booter.LOCAL_REPO)).getAbsolutePath();
        ArtifactFolderLayout folderLayout = new MavenFolderLayout(localRepoPrefix);
        GraphVizOutput dotOutput = new HumanReadableDotGraph(
                folderLayout,
                builder.getGraph(),
                builder.getVertexAttributeMapMap());
        dotOutput.export(new File(outputDirectory, "app.dot"));


        return null;
    }
}
