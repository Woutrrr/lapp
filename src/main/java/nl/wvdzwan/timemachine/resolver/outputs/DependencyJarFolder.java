package nl.wvdzwan.timemachine.resolver.outputs;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.aether.resolution.ArtifactResult;
import org.eclipse.aether.resolution.DependencyResult;

public class DependencyJarFolder implements ResolverOutput {

    private static Logger logger = LogManager.getLogger();

    private File outputFolder;

    public DependencyJarFolder(File outputFolder) {
        this.outputFolder = outputFolder;
    }

    @Override
    public boolean makeOutput(DependencyResult result) {

        if (!outputFolder.exists()) {
            logger.info("Output folder {} doesn't exist, creating it.");
            outputFolder.mkdirs();
        }

        long total = result.getArtifactResults().size();
        long copyResults = result.getArtifactResults().stream()
                .filter(ArtifactResult::isResolved)
                .map(ArtifactResult::getArtifact)
                .map(artifact -> {
                    File file = artifact.getFile();
                    File dest = new File(outputFolder, file.getName());

                    try {
                        Files.copy(file.toPath(), dest.toPath());
                    } catch (IOException e) {
                        logger.error("Failed copying {} to {}", file, dest);
                        return false;
                    }
                    logger.debug("Copied {}", file);
                    return true;

                }).filter(Boolean::booleanValue).count();


        logger.info("Copied {}/{} files to jar folder (\"{}\")",
                copyResults,
                total,
                outputFolder);

        return copyResults == total;
    }
}
