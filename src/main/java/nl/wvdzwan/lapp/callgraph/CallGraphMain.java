package nl.wvdzwan.lapp.callgraph;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.concurrent.Callable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import picocli.CommandLine;

import nl.wvdzwan.lapp.callgraph.FolderLayout.ArtifactFolderLayout;
import nl.wvdzwan.lapp.callgraph.FolderLayout.SimpleNameLayout;
import nl.wvdzwan.lapp.callgraph.wala.WalaAnalysisResult;
import nl.wvdzwan.lapp.callgraph.wala.WalaAnalysisTransformer;
import nl.wvdzwan.lapp.core.LappPackage;
import nl.wvdzwan.lapp.core.Util;
import nl.wvdzwan.lapp.protobuf.Lapp;
import nl.wvdzwan.lapp.protobuf.Protobuf;

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
            description = "Output file"
    )
    private File output = new File("lapp.buf");

    @CommandLine.Option(
            names = {"-c", "--classpath"},
            description = "Read first argument as classpath file"
    )
    private boolean isClassPath = false;

    @CommandLine.Parameters(
            index = "0..*",
            arity = "1..*",
            paramLabel = "jar",
            description = "Jar file(s) to generate IR graph of."
    )
    private ArrayList<String> jars = new ArrayList<>();

    private boolean analyseStdLib = false;

    @Override
    public Void call() throws Exception {

        // Setup
        if (isClassPath || jars.get(0).endsWith("classpath.txt")) {

            if (!parseClassPathFile(jars.get(0))) {
                logger.error("Error parsing classpath file \"{}\"", jars);
                return null;
            }
        }

        if (jars.get(0).equals("stdlib")) {
            jars.remove(0);
            this.analyseStdLib = true;
        }

        verifyJarsExist();

        // Analysis
        logger.info("Starting analysis for {}", jars);
        WalaAnalysis analysis = new WalaAnalysis(jars, exclusionFile, this.analyseStdLib);
        WalaAnalysisResult analysisResult = analysis.run();

        // Build Lapp Package
        logger.info("Build LappPackage for {}", jars);
        ArtifactFolderLayout layoutTransformer = new SimpleNameLayout();

        LappPackage lappPackage = WalaAnalysisTransformer.toPackage(analysisResult, layoutTransformer, this.analyseStdLib);


        // Output
        logger.info("Generate output");
        Lapp.Package proto = Protobuf.of(lappPackage);
        Util.saveProtoToFile(proto, output);

        return null;
    }



    private boolean parseClassPathFile(String classpath) {

        ClassPathFile classPathFile;
        try {
            classPathFile = new ClassPathFile(new File(classpath));
        } catch (FileNotFoundException e) {
            return false;
        }


        jars = classPathFile.getJars();

        return true;
    }

    private void verifyJarsExist() throws FileNotFoundException {

        ArrayList<String> missing = new ArrayList<>();

        for (String s : jars) {
            Path p = Paths.get(s);

            if (!Files.isRegularFile(p)) {
                missing.add(p.toString());
            }
        }

        if (missing.size() != 0) {
            throw new FileNotFoundException("Could not find dependencies: " + missing.toString());
        }
    }

}
