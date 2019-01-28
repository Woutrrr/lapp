package nl.wvdzwan.lapp.analyse;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import picocli.CommandLine;

import nl.wvdzwan.lapp.callgraph.CallGraphMain;
import nl.wvdzwan.lapp.resolver.Main;

@CommandLine.Command(
        name = "analyse",
        description = "Run 'resolve' and 'callgraph' for a package"
)
public class AnalyseCommand implements Callable<Void> {

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

    @CommandLine.Parameters(
            index = "0",
            paramLabel = "package_identifier",
            description = "Maven artifact to analyze (format: [groupId]:[artifactId] eg. com.company.app:my-app)."
    )
    private String packageIdentifier;


    @CommandLine.Parameters(
            index = "1",
            paramLabel = "date",
            description = "date used for resolving packages"
    )
    private String date;


    @Override
    public Void call() throws Exception {

        List<String> resolveArgs = new ArrayList<>();


        resolveArgs.add("-s");   resolveArgs.add(apiBaseUrl);
                resolveArgs.add("-k");   resolveArgs.add(apiKey);

        if (searchByDate) {
            resolveArgs.add("--date");
        }
        if (rateLimit) { resolveArgs.add("--limit"); }

        resolveArgs.add(packageIdentifier);
        resolveArgs.add(date);

        Callable<File> resolveCommand = CommandLine.populateCommand(new Main(), resolveArgs.toArray(new String[0]));

        File outputFolder = resolveCommand.call();

        if (outputFolder == null) {
            logger.error("Resolve command failed!");
            return null;
        }

        Path classpathFile = outputFolder.toPath().resolve("classpath.txt");

        String[] callgraphArgs = {
                "--in-place",
                "--classpath",
                classpathFile.toString()
        };

        Callable<Void> callgraphCommand = CommandLine.populateCommand(new CallGraphMain(), callgraphArgs);
        callgraphCommand.call();

        return null;
    }
}
