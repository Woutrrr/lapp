package nl.wvdzwan.timemachine.callgraph;

import java.io.File;
import java.util.Arrays;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.types.ClassLoaderReference;
import com.ibm.wala.util.strings.Atom;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import picocli.CommandLine;

import nl.wvdzwan.timemachine.callgraph.FolderLayout.ArtifactFolderLayout;
import nl.wvdzwan.timemachine.callgraph.FolderLayout.MavenFolderLayout;
import nl.wvdzwan.timemachine.callgraph.outputs.GraphVizOutput;
import nl.wvdzwan.timemachine.callgraph.outputs.HumanReadableDotGraph;
import nl.wvdzwan.timemachine.resolver.util.Booter;

import static com.ibm.wala.types.ClassLoaderReference.Java;

@CommandLine.Command(
        name = "callgraph",
        description = "Create a call graph from resolve output",
        mixinStandardHelpOptions = true
)
public class CallGraphMain implements Callable<Void> {
    private static Logger logger = LogManager.getLogger();

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
            index = "0..*",
            paramLabel = "jars",
            description = "Application/Libary jars to analyse, first jar will be considered as main jar"
    )
    private String[] jars;

    private static final ClassLoaderReference ClassLoaderMissing = new ClassLoaderReference(Atom.findOrCreateUnicodeAtom("Missing"), Java, null);


    public static void main(String[] args) {
        logger.debug("Supplied arguments: {}", Arrays.toString(args));
        CommandLine.call(new CallGraphMain(), args);
    }


    @Override
    public Void call() throws Exception {

        // Setup
        String mainJar = jars[0];

        String classPath;
        if (jars.length > 1) {
            classPath = Arrays.stream(jars).skip(1).collect(Collectors.joining(":"));
        } else {
            classPath = "";
        }

        // Analysis
        WalaAnalysis analysis = new WalaAnalysis(mainJar, classPath, exclusionFile);
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
