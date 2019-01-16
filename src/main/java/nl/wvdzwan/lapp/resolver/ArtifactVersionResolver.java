package nl.wvdzwan.lapp.resolver;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.resolution.VersionRangeRequest;
import org.eclipse.aether.resolution.VersionRangeResolutionException;
import org.eclipse.aether.resolution.VersionRangeResult;
import org.eclipse.aether.version.Version;

import nl.wvdzwan.librariesio.LibrariesIoInterface;
import nl.wvdzwan.librariesio.Project;
import nl.wvdzwan.librariesio.VersionDate;
import nl.wvdzwan.lapp.resolver.util.Booter;
import nl.wvdzwan.lapp.resolver.util.VersionNotFoundException;


/**
 * Find latest version of an artifact before a specified date.
 */
public class ArtifactVersionResolver {


    private static final Logger logger = LogManager.getLogger(ArtifactVersionResolver.class);

    private final RepositorySystem system;
    private final LibrariesIoInterface api;

    public ArtifactVersionResolver(RepositorySystem system, LibrariesIoInterface api) {
        this.system = system;
        this.api = api;
    }

    /**
     * Return latest version of an artifact before date.
     *
     * <p>All found versions after `date` are ignored</p>
     *
     * @param packageIdentifier The Artifact identifier, formatted as groupID:artifactID
     * @param dateLimit         The date used to filter newer versions
     * @return found version
     * @throws VersionRangeResolutionException when
     */
    public Version latestBeforeDate(String packageIdentifier, LocalDateTime dateLimit)
            throws VersionRangeResolutionException {
        logger.debug("Find latest version of {} before {}...", packageIdentifier, dateLimit);

        DefaultRepositorySystemSession session = Booter.newRepositorySystemSession(system);
        session.setConfigProperty(CustomVersionRangeResolver.CONFIG_LIMIT_DATE, dateLimit);


        DefaultArtifact artifact = new DefaultArtifact(packageIdentifier + ":[0,)");

        VersionRangeRequest rangeRequest = new VersionRangeRequest();
        rangeRequest.setArtifact(artifact);
        rangeRequest.setRepositories(Booter.newRepositories());


        VersionRangeResult rangeResult = system.resolveVersionRange(session, rangeRequest);

        if (rangeResult.getVersions().size() == 0) {
            logger.error("No suitable artifact found for {} before {}", packageIdentifier, dateLimit);
            throw new VersionRangeResolutionException(
                    rangeResult,
                    String.format("No suitable artifact found for %s before %s", packageIdentifier, dateLimit)
            );
        }

        Version version = rangeResult.getHighestVersion();
        logger.info("Found version {} for {} before {}", version.toString(), packageIdentifier, dateLimit);

        return version;
    }

    public LocalDateTime dateOfVersion(final String packageIdentifier, final String version)
            throws VersionNotFoundException {
        logger.debug("Find release date of {} version {}", packageIdentifier, version);

        Project project = api.getProjectInfo(packageIdentifier);

        if (project == null) {
            throw new VersionNotFoundException(String.format("Package %s not found!", packageIdentifier));
            // TODO make nice exception
        }

        for (VersionDate versionDate : project.getVersions()) {
            if (versionDate.getNumber().equals(version)) {
                return versionDate.getPublished_at();
            }
        }

        String foundVersions = project.getVersions().stream()
                .map(VersionDate::getNumber)
                .collect(Collectors.joining(", "));
        throw new VersionNotFoundException(
                String.format(
                        "Version %s for %s not found! Found versions: %s",
                        version, packageIdentifier,  foundVersions
                ));

    }

}
