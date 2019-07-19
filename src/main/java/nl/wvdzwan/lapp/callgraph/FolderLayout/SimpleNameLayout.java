package nl.wvdzwan.lapp.callgraph.FolderLayout;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.jar.JarFile;

public class SimpleNameLayout implements ArtifactFolderLayout {

    @Override
    public String artifactFromJarFile(JarFile jarFile) {

        String path = jarFile.getName();

        return artifactFromPath(path);
    }

    public String artifactFromPath(String path) {

        Path p = Paths.get(path);
        String filename = p.getFileName().toString();

        if (filename.endsWith(".jar")) {
            filename = filename
                    .substring(0, filename.length() -4)
                    .replace('$', ':');
        }

        return filename;
    }
}
