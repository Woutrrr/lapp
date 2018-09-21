package nl.wvdzwan.timemachine;


import nl.wvdzwan.timemachine.libio.Project;
import nl.wvdzwan.timemachine.libio.VersionDate;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.maven.artifact.repository.layout.ArtifactRepositoryLayout;
import org.apache.maven.artifact.repository.layout.DefaultRepositoryLayout;
import org.apache.maven.artifact.versioning.DefaultArtifactVersion;
import org.apache.maven.artifact.versioning.InvalidVersionSpecificationException;
import org.apache.maven.artifact.versioning.VersionRange;

import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import picocli.CommandLine;
import soot.*;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Targets;
import soot.util.dot.DotGraph;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.Callable;

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


    public static void main(String[] args) throws Exception {
        CommandLine.call(new Main(), args);
    }


    public Void call() throws Exception {

        //String api_key = "notavalidkeyjustaplaceholderkey";
        // identifier = "org.jdtaus.banking:jdtaus-banking-messages";
        //String timestamp = "2018-09-14";

        ArtifactRecord rootArtifact = new ArtifactRecord(packageIdentifier);

        String outputDir = rootArtifact.getArtifactId() + "@" + dateStramp.toString();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDateTime datetime_limit = dateStramp.atTime(23, 59, 59);

        LibrariesIoApi api = new LibrariesIoApi(apiKey);

        Project project = api.getProjectInfo(rootArtifact.getUnversionedIdentifier());

        Optional<VersionDate> optional_version = project.getVersions().stream()
                .filter(v -> v.getPublished_at().isBefore(datetime_limit)) // Filter on timestamp
                .max((vd1, vd2) -> vd1.getPublished_at().compareTo(vd2.getPublished_at()));

        if (!optional_version.isPresent()) {
            throw new Exception("No suitable artifact found!");
        }

        VersionDate versionDate = optional_version.get();
        rootArtifact.setVersion(versionDate.getNumber());
        logger.info("Found version: {} for {} with timestamp {}", versionDate.getNumber(), rootArtifact.getUnversionedIdentifier(), versionDate.getPublished_at().toString());


        Map<String, ArtifactRecord> foundProjects = new LinkedHashMap<>();


        foundProjects.put(rootArtifact.getUnversionedIdentifier(), rootArtifact);

        // TODO include first resolve in loop

        ModelFactory modelFactory = new ModelFactory();

        Model model = modelFactory.getModel(rootArtifact.getGroupId(), rootArtifact.getArtifactId(), rootArtifact.getVersion());

        List<Dependency> deps = model.getDependencies();

        Deque<Dependency> toResolve = new ArrayDeque<>();
        toResolve.addAll(deps);

        while (!toResolve.isEmpty()) {
            Dependency dep = toResolve.removeFirst();
            String dependencyIdentifier = dep.getGroupId() + ":" + dep.getArtifactId();

            if (foundProjects.containsKey(dep.getGroupId() + ":" + dep.getArtifactId())) {
                logger.info("{} already resolved before. Skipping...", dependencyIdentifier);
                continue;
            }

            // Resolve dependency
            String version = dep.getVersion();

            if (isVersionRange(version)) {
                VersionDate dependencyVersionDate = resolveVersionRange(dependencyIdentifier, version, datetime_limit, api);

                logger.info("Resolved  {} {} to {}", dependencyIdentifier, version, dependencyVersionDate.getNumber());

                version = dependencyVersionDate.getNumber();
            }

            logger.info("Add {}:{} to project list", dependencyIdentifier, version);
            foundProjects.put(dependencyIdentifier, new ArtifactRecord(dep.getGroupId(), dep.getArtifactId(), version));

            Model dependencyProject = modelFactory.getModel(dep.getGroupId(), dep.getArtifactId(), version);
            List<Dependency> subDependencies = dependencyProject.getDependencies();

            logger.debug("Dependencies of {}: {}", dependencyIdentifier, subDependencies);
            toResolve.addAll(subDependencies);
            Thread.sleep(500); // TODO Let libio class handle this
        }

        ArtifactRepositoryLayout layout = new DefaultRepositoryLayout();
        CustomRemoteRepository remoteRepository = new CustomRemoteRepository("http://repo1.maven.org/maven2/", layout);


        File destinationDir = new File(outputDir);
        destinationDir.mkdir();

        for (ArtifactRecord foundProject : foundProjects.values()) {
            // Download jar

            File destination = new File(destinationDir, foundProject.getJarName());

            File downloadedJar = remoteRepository.getJar(foundProject);
            Files.move(downloadedJar.toPath(), destination.toPath());
        }

        // make call graph
        FilenameFilter filter = new FilenameFilter() {
            @Override
            public boolean accept(File file, String s) {
                return s.toLowerCase().endsWith(".jar");
            }
        };
        File[] jars = destinationDir.listFiles(filter);
        String[] jarPaths = (String[]) Arrays.stream(jars).map(File::getAbsolutePath).toArray(size -> new String[size]);
        String classpath = String.join(":", jarPaths);

        makeCallGraph(classpath, new File(destinationDir, rootArtifact.getJarName()));

        return null;
    }

    private class VersionRangeNotFulFilledException extends Exception {
        public VersionRangeNotFulFilledException(String message) {
            super(message);
        }
    }

    public boolean isVersionRange(String versionDefinition) {
        final char[] rangeIndicators = {'[', ']', '(', ')', ','};

        for (char character : rangeIndicators) {
            if (versionDefinition.indexOf(character) > -1) {
                return true;
            }
        }

        return false;
    }

    public VersionDate resolveVersionRange(String projectIdentifier, String versionRangeDef, LocalDateTime timestamp, LibrariesIoApi api)
            throws
            InvalidVersionSpecificationException,
            VersionRangeNotFulFilledException {

        Project project = api.getProjectInfo(projectIdentifier);

        VersionRange versionRange = VersionRange.createFromVersionSpec(versionRangeDef);

        Optional<VersionDate> maybeVersion = project.getVersions().stream()
                .filter(v -> v.getPublished_at().isBefore(timestamp)) // Filter on timestamp
                .filter(v -> versionRange.containsVersion(new DefaultArtifactVersion(v.getNumber()))) // Filter on version range definition
                .max(Comparator.comparing(VersionDate::getPublished_at)); // Get last available version

        if (!maybeVersion.isPresent()) {
            throw new VersionRangeNotFulFilledException("No suitable artifact found!");
        }
        return maybeVersion.get();

    }

    protected void makeCallGraph(String classpath, File mainJar) throws IOException {

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

                        graph.plot("graph.dot");


                        System.out.println("--- end");

                    }


                }


                ));


        String[] args = argsList.toArray(new String[0]);

        soot.Main.main(args);

    }

}
