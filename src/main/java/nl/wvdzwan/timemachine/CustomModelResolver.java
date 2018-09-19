package nl.wvdzwan.timemachine;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.artifact.handler.ArtifactHandler;
import org.apache.maven.artifact.handler.DefaultArtifactHandler;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Parent;
import org.apache.maven.model.Repository;
import org.apache.maven.model.building.FileModelSource;
import org.apache.maven.model.building.ModelSource;
import org.apache.maven.model.resolution.InvalidRepositoryException;
import org.apache.maven.model.resolution.ModelResolver;
import org.apache.maven.model.resolution.UnresolvableModelException;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;

public class CustomModelResolver implements ModelResolver {

    protected MavenRepository mavenRepository;
    private Map<String, Repository> repositoryMap = new LinkedHashMap<>();

    public CustomModelResolver(MavenRepository mavenRepository) {
        this.mavenRepository = mavenRepository;
    }

    @Override
    public ModelSource resolveModel(String groupId, String artifactId, String version) throws UnresolvableModelException {

        ArtifactHandler artifactHandler = new DefaultArtifactHandler();
        Artifact artifact = new DefaultArtifact(groupId, artifactId, version, "", "", "", artifactHandler);

        File pomFile = mavenRepository.getPom(artifact);

        return new FileModelSource(pomFile);
    }


    protected String pomXmlPath(Parent parent) {

        return parent.getGroupId().replace('.', '/') + "/" +
                parent.getArtifactId().replace('.', '/') + "/" +
                parent.getVersion() + "/" +
                parent.getArtifactId() + "-" + parent.getVersion() + ".pom";
    }

    @Override
    public ModelSource resolveModel(Parent parent) throws UnresolvableModelException {

        // TODO handle version ranges

        return resolveModel(parent.getGroupId(), parent.getArtifactId(), parent.getVersion());
    }

    @Override
    public ModelSource resolveModel(Dependency dependency) throws UnresolvableModelException {

        // TODO handle version ranges

        return resolveModel(dependency.getGroupId(), dependency.getArtifactId(), dependency.getVersion());
    }

    @Override
    public void addRepository(Repository repository) throws InvalidRepositoryException {
       addRepository(repository, false);
    }

    @Override
    public void addRepository(Repository repository, boolean b) throws InvalidRepositoryException {
        if (!repositoryMap.containsKey(repository.getId()) || b) {
            repositoryMap.put(repository.getId(), repository);
        }
    }

    @Override
    public ModelResolver newCopy() {
        return null;
    }
}
