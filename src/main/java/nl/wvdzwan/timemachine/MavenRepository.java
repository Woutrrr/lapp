package nl.wvdzwan.timemachine;

import org.apache.maven.artifact.Artifact;

import java.io.File;
import java.util.List;

public interface MavenRepository {
    String getMetaData(String groupId, String artifactId);
    List<String> getVersions(String groupId, String artifactId);

    File getPom(Artifact artifact);

    File getJar(ArtifactRecord artifact);
    File getJar(String groupId, String artifactId, String version);



}


