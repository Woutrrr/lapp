package nl.wvdzwan.timemachine;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.model.resolution.UnresolvableModelException;

import java.io.File;
import java.util.List;

public interface MavenRepository {
    String getMetaData(String groupId, String artifactId);
    List<String> getVersions(String groupId, String artifactId);

    File getPom(Artifact artifact) throws UnresolvableModelException;

    File getJar(ArtifactRecord artifact) throws JarNotFoundException;
    File getJar(String groupId, String artifactId, String version) throws JarNotFoundException;



}


