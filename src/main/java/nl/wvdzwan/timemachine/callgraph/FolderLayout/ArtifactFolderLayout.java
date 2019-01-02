package nl.wvdzwan.timemachine.callgraph.FolderLayout;

import java.util.jar.JarFile;

import nl.wvdzwan.timemachine.callgraph.ArtifactRecord;

public interface ArtifactFolderLayout {

    ArtifactRecord artifactRecordFromJarFile(JarFile path);
}
