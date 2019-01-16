package nl.wvdzwan.lapp.callgraph.FolderLayout;

import java.util.jar.JarFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import nl.wvdzwan.lapp.callgraph.ArtifactRecord;

public class MavenFolderLayout implements ArtifactFolderLayout {

    private String repositoryBase;

    private final Pattern pattern = Pattern.compile("/?(?<group>.*)/(?<artifact>[^/]*)/(?<version>[^/]*)/([^/]*).jar");

    public MavenFolderLayout(String repositoryBase) {
        this.repositoryBase = repositoryBase;
    }

    @Override
    public ArtifactRecord artifactRecordFromJarFile(JarFile jarFile) {
        String path = jarFile.getName();
        String name = path.substring(path.lastIndexOf('/')+1);

        if (!path.startsWith(repositoryBase)) {
            return new ArtifactRecord("", name, "");
            // TODO extract version from manifest
        }

        path = path.substring(repositoryBase.length());

        Matcher matcher = pattern.matcher(path);

        if (!matcher.matches()) {
            return new ArtifactRecord("", name, "");
            // TODO extract version from manifest
        }

        String group = matcher.group("group").replace('/', '.');
        String artifact = matcher.group("artifact");
        String version = matcher.group("version");


        return new ArtifactRecord(group, artifact, version);
    }
}
