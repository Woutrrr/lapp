package nl.wvdzwan.lapp.resolver.outputs;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.aether.resolution.ArtifactResult;
import org.eclipse.aether.resolution.DependencyResult;

import nl.wvdzwan.lapp.callgraph.ArtifactRecord;

public class DependencyJarFolder implements ResolveOutputTask {

    private static Logger logger = LogManager.getLogger(DependencyJarFolder.class.getSimpleName());

    private File outputFolder;

    public DependencyJarFolder(File outputFolder) {
        this.outputFolder = outputFolder;
    }

    @Override
    public boolean makeOutput(DependencyResult result) {


        File jarOutputFolder = new File(outputFolder, "jars");
        if (!jarOutputFolder.exists()) {
            logger.info("Output folder {} doesn't exist, creating it.", jarOutputFolder);
            jarOutputFolder.mkdirs();
        }


        long total = result.getArtifactResults().size();
        List<Path> copyResults = result.getArtifactResults().stream()
                .filter(ArtifactResult::isResolved)
                .map(ArtifactResult::getArtifact)
                .map(artifact -> {
                    File file = artifact.getFile();
                    String fileName = ArtifactRecord.getIdentifier(artifact).replace(":", "$") + ".jar";
                    Path dest = Paths.get(jarOutputFolder.getPath(), fileName);

                    try {
                        Files.copy(file.toPath(), dest, StandardCopyOption.REPLACE_EXISTING);
                    } catch (IOException e) {
                        logger.error("Failed copying {} to {}", file, dest);
                        return null;
                    }
                    logger.debug("Copied {}", file);
                    return dest;

                }).filter(Objects::nonNull).collect(Collectors.toList());


        logger.info("Copied {}/{} files to jar folder (\"{}\")",
                copyResults,
                total,
                outputFolder);

        makeClassPathFile(copyResults);


        return copyResults.size() == total;
    }

    protected boolean makeClassPathFile(List<Path> jarList) {

        logger.info("Generating Classpath file...");

        String dependencyClasspath = jarList.stream()
                .skip(1) // Skip first (main application/library) artifact
                .map(Path::toString)
                .collect(Collectors.joining(":"));

        File classPathFile = new File(outputFolder, "classpath.txt");

        PrintWriter writer = makeClassFileWriter(classPathFile);
        if (Objects.isNull(writer)) {
            logger.error("Error opening output file for classpath file");
            return false;
        }

        logger.info("Writing to {}", classPathFile);
        writer.println(jarList.get(0).toString());
        writer.println(dependencyClasspath);
        return true;
    }

    private PrintWriter makeClassFileWriter(File classPathFile) {
        try {

            if (!classPathFile.exists()) {
                classPathFile.getParentFile().mkdirs();
                classPathFile.createNewFile();
            }

            if (!classPathFile.isFile() || !classPathFile.canWrite()) {
                logger.error("Classpath file not writable");
                return null;
            }

        } catch (IOException e) {
            logger.error("Error opening {} with message {}", classPathFile, e.getMessage());
            return null;
        }

        PrintWriter writer;
        try {
            writer = new PrintWriter(new FileOutputStream(classPathFile), true);
        } catch (FileNotFoundException e) {
            // should not happen, just checked for this
            return null;
        }

        return writer;
    }

}
