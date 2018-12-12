package nl.wvdzwan.timemachine.callgraph;

import java.io.File;
import java.util.Arrays;
import java.util.concurrent.Callable;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ipa.cha.IClassHierarchy;
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

        String mainJar = jars[0];

        String classPath;
        if (jars.length > 1) {
            classPath = Arrays.stream(jars).skip(1).collect(Collectors.joining(":"));
        } else {
            classPath = "";
        }

        WalaAnalysis analysis = new WalaAnalysis(mainJar, classPath, exclusionFile);

        CallGraph cg = analysis.run();

        String localRepoPrefix = (new File(Booter.LOCAL_REPO)).getAbsolutePath();

        makeApplicationScopeGraph(cg, localRepoPrefix, analysis.getExtendedCha());

        return null;
    }



    private boolean makeApplicationScopeGraph(CallGraph cg, String localRepoPrefix, IClassHierarchy extendedCha) {
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

        return applicationScopeCallGraph.makeOutput(cg, extendedCha);
    }

}
