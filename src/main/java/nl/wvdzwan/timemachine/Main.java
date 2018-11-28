package nl.wvdzwan.timemachine;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.concurrent.Callable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.impl.DefaultServiceLocator;
import picocli.CommandLine;
import soot.*;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Targets;
import soot.util.dot.DotGraph;

import nl.wvdzwan.timemachine.libio.ApiConnectionParameters;
import nl.wvdzwan.timemachine.libio.LibrariesIoInterface;
import nl.wvdzwan.timemachine.resolver.util.Booter;


@CommandLine.Command(
        name = "timemachine",
        description = "Resolve and download dependencies for a maven artifact for a specific date in history.",
        mixinStandardHelpOptions = true,
        version = "timemachine version 1.0"
)
public class Main implements Callable<Void> {
    private static Logger logger = LogManager.getLogger();

    @CommandLine.Option(
            names = {"-k", "--api-key"},
            required = true,
            description = "Libraries.io api key, see https://libraries.io/account"
    )
    private String apiKey;


    @CommandLine.Option(
            names = {"-s", "--api-source"},
            description = "Url to use for custom project version-date source, defaults to Libraries.io"
    )
    private String apiBaseUrl = "https://libraries.io/api/maven/%s?api_key=%s";


    @CommandLine.Option(
            names = {"-d", "--date"},
            description = "Use version as date to determine the version to use"
    )
    private boolean searchByDate = false;


    @CommandLine.Option(
            names = {"-o", "--output"},
            description = "Output folder"
    )
    private File outputDirectory;


    @CommandLine.Parameters(
            index = "0",
            paramLabel = "package_identifier",
            description = "Maven artifact to analyze (format: [groupId]:[artifactId] eg. com.company.app:my-app)."
    )
    private String packageIdentifier;


    @CommandLine.Parameters(
            index = "1",
            paramLabel = "version",
            description = "Package version or if --date is used the date used for finding the latest version."
    )
    private String versionOrDate;


    public static void main(String[] args) {

        logger.debug("Supplied arguments: {}", Arrays.toString(args));
        CommandLine.call(new Main(), args);
    }


    public Void call() throws Exception {
        logger.info("Start analysis of {}", packageIdentifier);

        DefaultServiceLocator locator = Booter.newServiceLocator();
        initLibrariesIoApi(locator, new ApiConnectionParameters(apiBaseUrl, apiKey));

        RepositorySystem system = Booter.newRepositorySystem(locator);

        ResolveDependencies resolver = new ResolveDependencies(system);

        if (searchByDate) {
            logger.debug("Parse date");
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

            LocalDateTime dateStamp;

            try {
                dateStamp = LocalDate.parse(versionOrDate, formatter).atStartOfDay();
            } catch (DateTimeParseException e) {
                CommandLine.usage(new Main(), System.out);
                return null;
            }

            // Dates will be compared to be before or on the same day as the by the user provided date
            LocalDateTime dateTime = dateStamp.plusDays(1);

            ResolveResult result = resolver.resolveFromDate(packageIdentifier, dateTime);

        } else {

            String version = versionOrDate;

            ResolveResult result = resolver.resolveFromVersion(packageIdentifier, version);

            // TODO
            //ResolveDependencies.resolveFromVersion(packageIdentifier, version, apiConnection);
            // Get Date for version
            // Run resolver

        }

        return null;
    }

    private void initLibrariesIoApi(
            DefaultServiceLocator locator,
            ApiConnectionParameters apiConnectionParameters) {

        LibrariesIoInterface api = locator.getService(LibrariesIoInterface.class);
        api.setApiKey(apiConnectionParameters.getApiKey());
        api.setBaseUrl(apiConnectionParameters.getBaseUrl());

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
