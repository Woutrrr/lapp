package nl.wvdzwan.timemachine;


import nl.wvdzwan.timemachine.libio.LibrariesIoClient;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import picocli.CommandLine;
import soot.*;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Targets;
import soot.util.dot.DotGraph;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.stream.Collector;
import java.util.stream.Collectors;

@CommandLine.Command(
        name = "timemachine",
        description = "Creates a callgraph for a maven artifact including dependencies for a specific date in history.",
        mixinStandardHelpOptions = true,
        version = "timemahcine version 1.0"
)
public class Main implements Callable<Void> {
    protected static Logger logger = LogManager.getLogger();

    @CommandLine.Option(
            names = {"-k", "--api-key"},
            required = true,
            description = "Libraries.io api key, see https://libraries.io/account"
    )
    protected String apiKey;

    @CommandLine.Parameters(
            index = "0",
            paramLabel = "package identifier",
            description = "Maven artifact to analyze (format: [groupId]:[artifactId] eg. com.company.app:my-app)."
    )
    protected String packageIdentifier;


    @CommandLine.Parameters(
            index = "1",
            paramLabel = "date",
            description = "Date (format: [YYYY-MM-DD]) to resolve dependency tree for."
    )
    protected LocalDate dateStramp;


    public static void main(String[] args) {
        CommandLine.call(new Main(), args);
    }


    public Void call() throws Exception {
        logger.info("Start analysis of {}", packageIdentifier);

        ArtifactRecord rootArtifact = new ArtifactRecord(packageIdentifier);

        String outputDir = rootArtifact.getArtifactId() + "@" + dateStramp.toString();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDateTime datetime_limit = dateStramp.atTime(23, 59, 59);

        HttpClient httpClient = new HttpClient();
        LibrariesIOClient api = new LibrariesIOClient(apiKey, httpClient);
        ModelFactory modelFactory = new ModelFactory();

        DependencyTreeResolver dependencyResolver = new DependencyTreeResolver(api, modelFactory);

        DependencyResolveResult result = dependencyResolver.resolve(rootArtifact, datetime_limit);

        File destinationDir = new File(new File("output"), outputDir);

        ArrayList<Path> jars = dependencyResolver.downloadJars(result.getProjects().values(), destinationDir);


        String classpath = jars.stream()
                .map(Path::toFile)
                .map(File::getAbsolutePath)
                .collect(Collectors.joining(":"));

        //makeCallGraph(classpath, new File(destinationDir, rootArtifact.getJarName()), outputDir);

        return null;
    }



    protected void makeCallGraph(String classpath, File mainJar, String graphOutputName) throws IOException {

        List<String> argsList = new ArrayList<String>(Arrays.asList("-whole-program",
//                "-pp",
                "-exclude", "java",
                "-v",
//                "-cp", destinationDir.getAbsolutePath() + "/", // classpath,
                "-cp", "/usr/lib/jvm/java-8-openjdk-amd64/jre/lib/rt.jar:" + classpath, // classpath,
                "-allow-phantom-refs",
                //"-p", "cg", "all-reachable:true",

                "-process-dir", mainJar.getAbsolutePath()));

        System.out.println(argsList);


        PackManager.v().getPack("wjtp").add(
                new Transform("wjtp.myTransform", new SceneTransformer() {
                    protected void internalTransform(String phaseName,
                                                     Map options) {
                        System.out.println(Scene.v().getSootClassPath());
                        System.out.println("--- start");

                        CallGraph cg = Scene.v().getCallGraph();


                        Deque<MethodOrMethodContext> queue = new ArrayDeque<>();
                        Set<SootMethod> explored = new HashSet<>();

                        Iterator<MethodOrMethodContext> methodIterator = cg.sourceMethods();
                        DotGraph graph = new DotGraph("test graph");

                        // Seed exploration queue
                        while (methodIterator.hasNext()) {
                            MethodOrMethodContext m = methodIterator.next();
                            if (m.method().isPublic()) {
                                SootClass declaringClass = m.method().getDeclaringClass();
                                if (!declaringClass.isJavaLibraryClass() && !declaringClass.getName().startsWith("jdk")) {
                                    queue.add(m);
                                }
                            }
                        }

                        // Process queue
                        while (!queue.isEmpty()) {
                            SootMethod method = queue.pollFirst().method();

                            logger.debug("Process method: {}", method);
                            graph.drawNode(method.getSignature());

                            Iterator<MethodOrMethodContext> targets = new Targets(cg.edgesOutOf(method));
                            while (targets.hasNext()) {
                                SootMethod child = targets.next().method();

                                if (child.isJavaLibraryMethod()) {
                                    continue;
                                }

                                graph.drawEdge(method.getSignature(), child.getSignature());

                                if (!explored.contains(child)) {
                                    queue.add(child);
                                }
                            }

                            explored.add(method);
                        }

                        graph.plot(graphOutputName + ".dot");


                        System.out.println("--- end");

                    }


                }


                ));


        String[] args = argsList.toArray(new String[0]);

        soot.Main.main(args);

    }

}
