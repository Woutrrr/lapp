package nl.wvdzwan.timemachine.resolver.outputs;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.resolution.ArtifactResult;
import org.eclipse.aether.resolution.DependencyResult;

/**
 * Create a file with two lines, the main jar on the first line and a dependency classpath on the second line.
 */
public class ClassPathFile implements ResolverOutput {

    private static Logger logger = LogManager.getLogger();

    private File output;

    public ClassPathFile(File outputFolder) {
        this.output = outputFolder;
    }

    @Override
    public boolean makeOutput(DependencyResult result) {

        File classPathFile = new File(output, "classpath.txt");

        logger.info("Generating Classpath file...");

        List<ArtifactResult> artifacts = result.getArtifactResults();


        if (!artifacts.get(0).isResolved()) {
            logger.warn("Main artifact not resolved, aborting writing of classpath file!");
            return false;
        }

        String dependencyClasspath = result.getArtifactResults().stream()
                .skip(1) // Skip first (main application/library) artifact
                .filter(ArtifactResult::isResolved)
                .map(ArtifactResult::getArtifact)
                .map(Artifact::getFile)
                .map(File::getPath)
                .collect(Collectors.joining(":"));

        try {

            if (!classPathFile.exists()) {
                classPathFile.getParentFile().mkdirs();
                classPathFile.createNewFile();
            }

            if (!classPathFile.isFile() || !classPathFile.canWrite()) {
                logger.error("Classpath file not writeable");
                return false;
            }

        } catch (IOException e) {
            logger.error("Error opening {} with message {}", classPathFile, e.getMessage());
        }

        PrintWriter writer;
        try {
            writer = new PrintWriter(new FileOutputStream(classPathFile), true);
        } catch (FileNotFoundException e) {
            // should not happen, just checked for this
            e.printStackTrace();
            return false;
        }

        logger.info("Writing to {}", classPathFile);
        writer.println(artifacts.get(0).getArtifact().getFile().getAbsolutePath());
        writer.println(dependencyClasspath);


        logger.info("Classpath file generated.");
        return true;
    }
}
