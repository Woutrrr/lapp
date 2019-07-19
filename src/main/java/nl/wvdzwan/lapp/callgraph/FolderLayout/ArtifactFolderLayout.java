package nl.wvdzwan.lapp.callgraph.FolderLayout;

import java.util.jar.JarFile;

public interface ArtifactFolderLayout {

    String artifactFromJarFile(JarFile path);
}
