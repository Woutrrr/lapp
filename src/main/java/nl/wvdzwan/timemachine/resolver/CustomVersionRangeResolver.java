package nl.wvdzwan.timemachine.resolver;

import nl.wvdzwan.timemachine.HttpClient;
import nl.wvdzwan.timemachine.libio.LibrariesIoClient;
import nl.wvdzwan.timemachine.libio.LibrariesIoInterface;
import nl.wvdzwan.timemachine.libio.Project;
import nl.wvdzwan.timemachine.libio.VersionDate;
import org.apache.maven.repository.internal.DefaultVersionRangeResolver;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.resolution.VersionRangeRequest;
import org.eclipse.aether.resolution.VersionRangeResolutionException;
import org.eclipse.aether.resolution.VersionRangeResult;
import org.eclipse.aether.spi.locator.Service;
import org.eclipse.aether.spi.locator.ServiceLocator;
import org.eclipse.aether.version.Version;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

public class CustomVersionRangeResolver extends DefaultVersionRangeResolver {


    private LibrariesIoInterface api;

    @Override
    public VersionRangeResult resolveVersionRange(RepositorySystemSession session, VersionRangeRequest request )
            throws VersionRangeResolutionException {

        VersionRangeResult parentResult = super.resolveVersionRange(session, request);

        // Only filter if version was actually a range
        if (parentResult.getVersionConstraint().getRange() != null) {

            Artifact artifact = request.getArtifact();
            Project project = api.getProjectInfo(artifact.getGroupId() + ":" + artifact.getArtifactId());

            LocalDateTime date = (LocalDate.parse((String) session.getConfigProperties().get("time-machine.date"))).atTime(23, 59, 59);

            List<String> dateFilteredVersions = project.getVersions().stream()
                    .filter(v -> v.getPublished_at().isBefore(date)) // Filter on timestamp
                    .map(VersionDate::getNumber)
                    .collect(Collectors.toList());


            List<Version> versions = parentResult.getVersions();

            Iterator<Version> it = versions.iterator();
            while (it.hasNext()) {
                Version testVersion = it.next();

                if (!dateFilteredVersions.contains(testVersion.toString())) {
                    it.remove();
                }
            }

        }

        return parentResult;
    }

    @Override
    public void initService( ServiceLocator locator ) {
        super.initService(locator);

        this.api = locator.getService(LibrariesIoInterface.class);
    }
}
