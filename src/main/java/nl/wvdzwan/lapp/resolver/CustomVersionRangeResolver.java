package nl.wvdzwan.lapp.resolver;

import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.maven.repository.internal.DefaultVersionRangeResolver;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.resolution.VersionRangeRequest;
import org.eclipse.aether.resolution.VersionRangeResolutionException;
import org.eclipse.aether.resolution.VersionRangeResult;
import org.eclipse.aether.spi.locator.ServiceLocator;
import org.eclipse.aether.version.Version;

import nl.wvdzwan.librariesio.LibrariesIoInterface;
import nl.wvdzwan.librariesio.Project;
import nl.wvdzwan.librariesio.VersionDate;

public class CustomVersionRangeResolver extends DefaultVersionRangeResolver {
    protected static Logger logger = LogManager.getLogger("VersionRangeResolver");

    public static final String CONFIG_LIMIT_DATE = "time-machine.date";

    private LibrariesIoInterface api;

    @Override
    public VersionRangeResult resolveVersionRange(RepositorySystemSession session, VersionRangeRequest request )
            throws VersionRangeResolutionException {

        VersionRangeResult parentResult = super.resolveVersionRange(session, request);

        // Only filter if version was actually a range
        if (parentResult.getVersionConstraint().getRange() != null) {
            logger.info("Resolve version range for {}", () -> request.getArtifact().getArtifactId());

            Artifact artifact = request.getArtifact();
            Project project = api.getProjectInfo(artifact.getGroupId() + ":" + artifact.getArtifactId());

            LocalDateTime date = (LocalDateTime) session.getConfigProperties().get(CONFIG_LIMIT_DATE);

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
