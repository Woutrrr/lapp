package nl.wvdzwan.lapp.callgraph.FolderLayout;

import java.util.jar.JarFile;

import nl.wvdzwan.lapp.callgraph.ArtifactRecord;

public interface ArtifactFolderLayout {

    ArtifactRecord artifactRecordFromJarFile(JarFile path);
}
