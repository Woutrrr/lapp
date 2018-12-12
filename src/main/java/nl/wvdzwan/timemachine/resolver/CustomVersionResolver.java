package nl.wvdzwan.timemachine.resolver;

import java.time.LocalDateTime;

import org.apache.maven.repository.internal.DefaultVersionResolver;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.resolution.VersionRequest;
import org.eclipse.aether.resolution.VersionResolutionException;
import org.eclipse.aether.resolution.VersionResult;
import org.eclipse.aether.spi.locator.ServiceLocator;

import nl.wvdzwan.librariesio.LibrariesIoInterface;
import nl.wvdzwan.librariesio.Project;
import nl.wvdzwan.librariesio.VersionDate;

public class CustomVersionResolver extends DefaultVersionResolver {

    private LibrariesIoInterface api;

    @Override
    public VersionResult resolveVersion(RepositorySystemSession session, VersionRequest request)
            throws VersionResolutionException {
        VersionResult parentResult = super.resolveVersion(session, request);


        Artifact artifact = request.getArtifact();
        Project project = api.getProjectInfo(artifact.getGroupId() + ":" + artifact.getArtifactId());

        LocalDateTime date = (LocalDateTime) session.getConfigProperties().get(CustomVersionRangeResolver.CONFIG_LIMIT_DATE);


        boolean foundVersion = project.getVersions().stream()
                .filter(v -> v.getPublished_at().isBefore(date)) // Filter on timestamp
                .map(VersionDate::getNumber)
                .anyMatch(s -> s.equals(parentResult.getVersion()));

        if (!foundVersion) {
            throw new VersionResolutionException(parentResult);
        }

        return parentResult;

    }


    @Override
    public void initService(ServiceLocator locator) {
        super.initService(locator);

        this.api = locator.getService(LibrariesIoInterface.class);
    }
}
