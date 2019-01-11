package nl.wvdzwan.timemachine.callgraph.FolderLayout;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.jar.JarFile;

import nl.wvdzwan.timemachine.callgraph.ArtifactRecord;

public class DollarSeparatedLayout implements ArtifactFolderLayout {

    /**
     * Build ArtifactRecord from path
     *
     * @param jarFile Jar file
     * @return parsed ArtifactRecord
     */
    @Override
    public ArtifactRecord artifactRecordFromJarFile(JarFile jarFile) {

        String path = jarFile.getName();

        return artifactRecordFromPath(path);
    }

    public ArtifactRecord artifactRecordFromPath(String path) {

        Path p = Paths.get(path);
        String filename = p.getFileName().toString();

        if (filename.endsWith(".jar")) {
            filename = filename
                    .substring(0, filename.length() -4)
                    .replace('$', ':');
        }

        try {
            return new ArtifactRecord(filename);
        } catch (IllegalArgumentException e) {
            // File name probably isn't in the correct format
            return new ArtifactRecord("static", filename, "?");
        }
    }
}
