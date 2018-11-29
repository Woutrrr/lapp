package nl.wvdzwan.timemachine.callgraph;

import java.io.File;
import java.util.Arrays;
import java.util.concurrent.Callable;
import java.util.function.Predicate;

import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.types.ClassLoaderReference;
import com.ibm.wala.util.strings.Atom;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import picocli.CommandLine;

import nl.wvdzwan.timemachine.callgraph.outputs.GraphVizOutput;
import nl.wvdzwan.timemachine.resolver.util.Booter;

import static com.ibm.wala.types.ClassLoaderReference.Java;

@CommandLine.Command(
        name = "timemachine-graph",
        description = "Create a call graph from timemachine output",
        mixinStandardHelpOptions = true,
        version = "timemachine-graph version 1.0"
)
public class CallGraphMain implements Callable<Void> {
    private static Logger logger = LogManager.getLogger();
    @CommandLine.Option(
            names = {"-p", "--phantom"},
            description = "Make phantom nodes in call graph."
    )
    private boolean outputPhantomNodes = true;

    @CommandLine.Option(
            names = {"-i", "--include-interface"},
            description = "Make interface node for interface invocations"
    )
    private boolean outputInterfaceInvocations = true;

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
            paramLabel = "main-jar",
            description = "Application/Libary jar to analyse"
    )
    private String mainJar;

    @CommandLine.Parameters(
            index = "1",
            paramLabel = "classpath",
            description = "Classpath with all dependencies of main jar"
    )
    private String classPath;

    private static final ClassLoaderReference ClassLoaderMissing = new ClassLoaderReference(Atom.findOrCreateUnicodeAtom("Missing"), Java, null);


    public static void main(String[] args) {
        logger.debug("Supplied arguments: {}", Arrays.toString(args));
        CommandLine.call(new CallGraphMain(), args);
    }

    @Override
    public Void call() throws Exception {

        WalaAnalysis analysis = new WalaAnalysis(mainJar, classPath, exclusionFile);

        CallGraph cg = analysis.run();

        String localRepoPrefix = (new File(Booter.LOCAL_REPO)).getAbsolutePath();

        makeApplicationScopeGraph(cg, localRepoPrefix);

        makeAppLibScopeGraph(cg, localRepoPrefix);

        makeFullScopeGraph(cg, localRepoPrefix);

        return null;
    }



    private boolean makeApplicationScopeGraph(CallGraph cg, String localRepoPrefix) {
        Predicate<CGNode> onlyApplicationScope = node -> {
            return !node.getMethod()
                    .getDeclaringClass()
                    .getClassLoader().getReference()
                    .equals(ClassLoaderReference.Application);
        };
        GraphVizOutput applicationScopeCallGraph = new GraphVizOutput(
                new File(outputDirectory, "app.dot"),
                onlyApplicationScope,
                outputPhantomNodes,
                outputInterfaceInvocations,
                localRepoPrefix);

        return applicationScopeCallGraph.makeOutput(cg);
    }

    private void makeAppLibScopeGraph(CallGraph cg, String localRepoPrefix) {
        Predicate<CGNode> anyThingButPrimodialScope = node -> {
            return node.getMethod()
                    .getDeclaringClass()
                    .getClassLoader().getReference()
                    .equals(ClassLoaderReference.Primordial);
        };
        GraphVizOutput appAndLibScopeCallGraph = new GraphVizOutput(
                new File(outputDirectory, "app_lib.dot"),
                anyThingButPrimodialScope,
                outputPhantomNodes,
                outputInterfaceInvocations,
                localRepoPrefix);

        appAndLibScopeCallGraph.makeOutput(cg);
    }

    private boolean makeFullScopeGraph(CallGraph cg, String localRepoPrefix) {
        Predicate<CGNode> filterNone = node -> false;
        GraphVizOutput applicationScopeCallGraph = new GraphVizOutput(
                new File(outputDirectory, "app_full.dot"),
                filterNone,
                outputPhantomNodes,
                outputInterfaceInvocations,
                localRepoPrefix);

        return applicationScopeCallGraph.makeOutput(cg);
    }


}
