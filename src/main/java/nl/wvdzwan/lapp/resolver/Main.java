package nl.wvdzwan.lapp.resolver;

import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.concurrent.Callable;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.impl.DefaultServiceLocator;
import org.eclipse.aether.resolution.DependencyResolutionException;
import org.eclipse.aether.resolution.DependencyResult;
import picocli.CommandLine;

import nl.wvdzwan.lapp.resolver.outputs.ConsoleOutput;
import nl.wvdzwan.lapp.resolver.outputs.DependencyJarFolder;
import nl.wvdzwan.lapp.resolver.outputs.DependencyTreeWriterOutput;
import nl.wvdzwan.lapp.resolver.outputs.OutputHandler;
import nl.wvdzwan.lapp.resolver.util.Booter;
import nl.wvdzwan.lapp.resolver.util.LibIOVersionResolutionException;
import nl.wvdzwan.lapp.resolver.util.VersionNotFoundException;
import nl.wvdzwan.librariesio.ApiConnectionParameters;
import nl.wvdzwan.librariesio.LibrariesIoInterface;
import nl.wvdzwan.librariesio.RateLimitedClient;


@CommandLine.Command(
        name = "resolve",
        description = "Resolve and download dependencies for a maven artifact for a specific date or version in history."
)
public class Main implements Callable<File> {
    private static Logger logger = LogManager.getLogger();

    @CommandLine.Option(
            names = {"-k", "--api-key"},
            description = "Libraries.io api key, see https://libraries.io/account"
    )
    private String apiKey = "";


    @CommandLine.Option(
            names = {"-s", "--api-source"},
            description = "Url to use for custom project version-date source, defaults to Libraries.io"
    )
    private String apiBaseUrl = "http://libio:4567/api/";

    @CommandLine.Option(
            names = {"--limit"},
            description = "Rate limit requests to libio api"
    )
    private boolean rateLimit = false;


    @CommandLine.Option(
            names = {"-d", "--date"},
            description = "Use version as date to determine the version to use"
    )
    private boolean searchByDate = false;


    @CommandLine.Option(
            names = {"-o", "--output"},
            description = "Output folder, defaults to \"output/[group]_[artifact]_[version]\""
    )
    private File outputDirectoryArgument = null;


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


    public File call() throws Exception {
        logger.info("Start analysis of {}", packageIdentifier);

        DefaultServiceLocator locator = Booter.newServiceLocator();
        if (rateLimit) {
            locator.setService(LibrariesIoInterface.class, RateLimitedClient.class);
        }

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
            } catch (DependencyResolutionException e) {
                Throwable cause = ExceptionUtils.getRootCause(e);
                if (cause instanceof LibIOVersionResolutionException) {
                    logger.error(cause.getMessage());
                    return null;
                } else {
                    throw e;
                }
            }
        }


        if (resolveResult == null) {
            return null;
            // TODO graceful exit
        }

        if (resolveResult.getRoot().getArtifact().getFile() == null) {
            logger.warn("Main artifact not resolved, abort ");
            return null;
        }

        File outputFolder = getOutputFolder(outputDirectoryArgument, resolveResult);

        if (!outputFolder.exists()) {
            outputFolder.mkdirs();
        }

        OutputHandler handler = buildOutputHandler(outputFolder);
        handler.process(resolveResult);

        return outputFolder;
    }

    private File getOutputFolder(File outputDirectory, DependencyResult resolveResult) {

        // If output directory is supplied by the user just return that folder
        if (outputDirectory != null) {
            return outputDirectory;
        }

        Artifact mainArtifact = resolveResult.getRoot().getArtifact();

        String artifactFolderName = String.format("%s_%s_%s", mainArtifact.getGroupId(), mainArtifact.getArtifactId(), mainArtifact.getVersion());

        return new File("output", artifactFolderName);
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
