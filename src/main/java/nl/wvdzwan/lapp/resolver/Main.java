package nl.wvdzwan.lapp.resolver;

import nl.wvdzwan.lapp.resolver.outputs.ConsoleOutput;
import nl.wvdzwan.lapp.resolver.outputs.DependencyJarFolder;
import nl.wvdzwan.lapp.resolver.outputs.DependencyTreeWriterOutput;
import nl.wvdzwan.lapp.resolver.outputs.OutputHandler;
import nl.wvdzwan.lapp.resolver.util.Booter;
import nl.wvdzwan.librariesio.ApiConnectionParameters;
import nl.wvdzwan.librariesio.LibrariesIoInterface;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.impl.DefaultServiceLocator;
import org.eclipse.aether.resolution.ArtifactResult;
import org.eclipse.aether.resolution.DependencyResult;
import picocli.CommandLine;

import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

import nl.wvdzwan.lapp.resolver.util.VersionNotFoundException;

@CommandLine.Command(
        name = "resolve",
        description = "Resolve and download dependencies for a maven artifact for a specific date or version in history."
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
    private String apiBaseUrl = "https://libraries.io/api/";


    @CommandLine.Option(
            names = {"-d", "--date"},
            description = "Use version as date to determine the version to use"
    )
    private boolean searchByDate = false;


    @CommandLine.Option(
            names = {"-o", "--output"},
            description = "Output folder"
    )
    private File outputDirectory = new File("output");


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
        ArtifactVersionResolver versionFinder = new ArtifactVersionResolver(system, locator.getService(LibrariesIoInterface.class));


        ResolveDependencies resolver = new ResolveDependencies(system, versionFinder);
        DependencyResult resolveResult;

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

            resolveResult = resolver.resolveFromDate(packageIdentifier, dateTime);


        } else {

            String version = versionOrDate;
            try {
                resolveResult = resolver.resolveFromVersion(packageIdentifier, version);
            } catch (VersionNotFoundException e) {
                logger.error(e.getMessage());
                return null;
            }
        }


        if (resolveResult == null) {
            return null;
            // TODO graceful exit
        }

        List<String> jarPaths = resolveResult.getArtifactResults().stream()
                .filter(ArtifactResult::isResolved)
                .map(ArtifactResult::getArtifact)
                .map(Artifact::getFile)
                .map(File::getAbsolutePath)
                .collect(Collectors.toList());

        System.out.println(jarPaths);

        if (!resolveResult.getArtifactResults().get(0).isResolved()) {
            logger.warn("Main artifact not resolved, abort ");
            return null;
        }
        OutputHandler handler = buildOutputHandler(outputDirectory);
        handler.process(resolveResult);

        return null;
    }

    private OutputHandler buildOutputHandler(File outputDirectory) {
        OutputHandler handler = new OutputHandler();

        handler.add(new ConsoleOutput());
        handler.add(new DependencyTreeWriterOutput(new File(outputDirectory, "dependency_tree.txt")));
        handler.add(new DependencyJarFolder(outputDirectory));

        return handler;
    }

    private void initLibrariesIoApi(
            DefaultServiceLocator locator,
            ApiConnectionParameters apiConnectionParameters) {

        LibrariesIoInterface api = locator.getService(LibrariesIoInterface.class);
        api.setApiKey(apiConnectionParameters.getApiKey());
        api.setBaseUrl(apiConnectionParameters.getBaseUrl());

    }

}
